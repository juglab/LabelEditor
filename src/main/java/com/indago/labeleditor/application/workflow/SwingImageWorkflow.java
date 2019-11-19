package com.indago.labeleditor.application.workflow;

import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.module.Module;
import org.scijava.module.ModuleCanceledException;
import org.scijava.module.ModuleException;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;
import org.scijava.ui.swing.widget.SwingInputHarvester;
import org.scijava.ui.swing.widget.SwingInputPanel;
import org.scijava.widget.InputPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

@Plugin(type = Service.class)
public class SwingImageWorkflow implements ImageWorkflow {

	@Parameter
	CommandService commandService;

	@Parameter
	Context context;

	JFrame frame;
	JTabbedPane contentPanel;
	ArrayBlockingQueue<Boolean> queue;
	private JPanel runPanel;

	private SwingInputHarvester swingInputHarvester;

	private ImageWorkflowStep currentStep;
	private List<ImageWorkflowStep> steps;
	private Map<ImageWorkflowStep, JPanel> stepPanels;
	private JPanel confirmPanel;

	@Override
	public void initWorkflow(String title, List<ImageWorkflowStep> workflowSteps) {
		swingInputHarvester = new SwingInputHarvester();
		context.inject(swingInputHarvester);
		stepPanels = new HashMap<>();
		steps = workflowSteps;
		SwingUtilities.invokeLater(() -> initWorkflowFrame(title));
	}

	private void initWorkflowFrame(String title) {
		if (frame != null) frame.dispose();
		frame = new JFrame(title);
		frame.setContentPane(createMainPanel());
		frame.setMinimumSize(new Dimension(500, 500));
		frame.pack();
	}

	private Container createMainPanel() {
		JPanel mainPanel = new JPanel(new MigLayout());
		mainPanel.add(createTabsPanel(), "push, grow, span");
		mainPanel.add(createConfirmPanel());
		return mainPanel;
	}

	private Component createConfirmPanel() {
		confirmPanel = new JPanel();
		return confirmPanel;
	}

	private Component createTabsPanel() {
		contentPanel = new JTabbedPane();
		for (ImageWorkflowStep step : steps) {
			contentPanel.add(step.getTitle(), createStepPanel(step));
		}
		contentPanel.add("run", initMainRunPanel());
		return contentPanel;
	}

	private Component createStepPanel(ImageWorkflowStep step) {
		JPanel stepPanel = new JPanel(new MigLayout("fill, flowy, gap rel 0", "", "[][]push"));
		stepPanel.add(new JLabel(step.getDescription()));
		stepPanels.put(step, stepPanel);
		return new JScrollPane(stepPanel);
	}

	private Component initMainRunPanel() {
		runPanel = new JPanel();
		runPanel.setLayout(new BoxLayout(runPanel, BoxLayout.PAGE_AXIS));
		for (ImageWorkflowStep step : steps) {
			runPanel.add(new JLabel(step.getTitle() + "..."));
		}
		return runPanel;
	}

	@Override
	public void startPreviewRun() {
		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}

	@Override
	public void donePreviewRun() {
		SwingUtilities.invokeLater(() -> {
			contentPanel.setSelectedIndex(contentPanel.getTabCount()-1);
		});
	}

	private void setSelectedTab(ImageWorkflowStep step) {
		currentStep = step;
		for (int i = 0; i < contentPanel.getTabCount(); i++) {
			contentPanel.setEnabledAt(i, false);
		}
		contentPanel.setEnabledAt(steps.indexOf(step), true);
		contentPanel.setSelectedIndex(steps.indexOf(step));
	}

	@Override
	public boolean waitForOK() throws InterruptedException {
		if (SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("Can't be called from EDT.");
		}
		return queue.take();
	}

	@Override
	public void startMainRun() {
		confirmPanel.getParent().remove(confirmPanel);
	}

	@Override
	public void doneMainRun() {
		SwingUtilities.invokeLater(() -> frame.dispose());
	}

	@Override
	public void addCommand(ImageWorkflowStep step, Module module) throws InterruptedException, ModuleException {
		setSelectedTab(step);
		JPanel modulePanel = createModulePanel(module);
		queue = new ArrayBlockingQueue<>(1);
		SwingUtilities.invokeLater(() -> {
			getCurrentTab().add(modulePanel);
			repopulateConfirmPanel();
			getCurrentTab().revalidate();
		});
		if(!waitForOK()) {
			throw new ModuleCanceledException();
		}
	}

	private JPanel getCurrentTab() {
		return stepPanels.get(currentStep);
	}

	@Override
	public void setTestStep(ImageWorkflowStep step) {
		setSelectedTab(step);
	}

	@Override
	public void setMainStep(ImageWorkflowStep step) {
		currentStep = step;
		SwingUtilities.invokeLater(() -> {
			rewriteMainPanel();
		});
	}

	private void rewriteMainPanel() {
		for (int i = 0; i < steps.size(); i++) {
			ImageWorkflowStep step = steps.get(i);
			if(i < steps.indexOf(currentStep)) {
					((JLabel)runPanel.getComponent(i)).setText("[done] " + step.getTitle() + "...");
			} else {
				if(i == steps.indexOf(currentStep)) {
					((JLabel)runPanel.getComponent(i)).setText("[running] " + step.getTitle() + "...");
				} else {
					((JLabel)runPanel.getComponent(i)).setText(step.getTitle() + "...");
				}
			}
		}
	}

	private void repopulateConfirmPanel() {
		JButton buttonOk = new JButton("Done");
		JButton buttonCancel = new JButton("Cancel");
		buttonOk.addActionListener(e -> queue.offer(true));
		buttonCancel.addActionListener(e -> queue.offer(false));
		confirmPanel.removeAll();
		confirmPanel.add(buttonOk);
		confirmPanel.add(buttonCancel);
		JRootPane rootPane = SwingUtilities.getRootPane(frame);
		rootPane.setDefaultButton(buttonOk);
		confirmPanel.revalidate();
	}

	private JPanel createModulePanel(Module module) throws ModuleException {
		InputPanel<JPanel, JPanel> inputPanel = new SwingInputPanel();
		swingInputHarvester.buildPanel(inputPanel, module);
		return inputPanel.getComponent();
	}
}
