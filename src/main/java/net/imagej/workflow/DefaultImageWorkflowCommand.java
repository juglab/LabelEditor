package net.imagej.workflow;

import net.imagej.ops.OpService;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.module.Module;
import org.scijava.module.ModuleException;
import org.scijava.module.ModuleRunner;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;
import org.scijava.widget.InputHarvester;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * The image processing workflow should be defined in the {@link #run(RandomAccessibleInterval)} method.
 * Any inputs of commands handled via (@link {@link #runCommand} will be harvested by the {@link #commandWorkflow}.
 * When an {@link DefaultImageWorkflowCommand} is called, the {@link #run()} method will first execute the workflow on a small part of the input image
 * and during this procedure harvest the inputs of the relevant commands. It will then execute the same workflow with the whole image,
 * using the input parameters as specified during the first run.
 *
 * @author Deborah Schmidt
 */
public abstract class DefaultImageWorkflowCommand implements ImageWorkflowCommand {

	@Parameter
	protected OpService opService;

	@Parameter
	protected Context context;

	@Parameter
	private PluginService pluginService;

	@Parameter
	private CommandService commandService;

	private ImageWorkflow commandWorkflow;

	private boolean testRun = true;

	private Map<ImageWorkflowStep, Module> modules = new HashMap<>();

	private boolean canceled = false;

	@Override
	public void run() {
		initWorkflow();
		RandomAccessibleInterval testImg = createTestImage();
		harvestInputsDuringTestRun(testImg);
		doMainRun();
	}

	private void initWorkflow() {
		commandWorkflow = new SwingImageWorkflow();
		context.inject(commandWorkflow);
		commandWorkflow.initWorkflow(getTitle(), getSteps());
		modules.clear();
	}

	private RandomAccessibleInterval createTestImage() {
		long[] min = new long[getInput().numDimensions()];
		long[] max = new long[getInput().numDimensions()];
		for (int i = 0; i < min.length; i++) {
			min[i] = 0;
			max[i] = 9;
		}
		Interval interval = new FinalInterval(min, max);
		return opService.copy().rai(Views.interval(getInput(), interval));
	}

	private void harvestInputsDuringTestRun(RandomAccessibleInterval testImg) {
		testRun = true;
		commandWorkflow.startPreviewRun();
		try {
			run(testImg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commandWorkflow.donePreviewRun();
	}

	private void doMainRun() {
		testRun = false;
		commandWorkflow.startMainRun();
		try {
			run(getInput());
		} catch (Exception e) {
			e.printStackTrace();
		}
		commandWorkflow.doneMainRun();
	}

	protected Module runCommand(ImageWorkflowStep step, Class commandClass, Object... args) throws ModuleException, ExecutionException, InterruptedException {
		if(testRun) commandWorkflow.setPreviewStep(step);
		else commandWorkflow.setMainStep(step);
		Module module;
		if(testRun) {
			module = createModule(commandClass, args);
			if(module == null) {
				cancel("Could not create command " + commandClass);
			}
			modules.put(step, module);
			commandWorkflow.addCommand(step, module);
		} else {
			module = modules.get(step);
//			module.getOutputs().forEach((name, o) -> module.unresolveOutput(name));
			setModuleInputs(module, args);
		}
		return commandService.moduleService().run(module, false).get();
	}

	private Module createModule(Class commandClass, Object... args) throws ModuleException {
		Module module = commandService.getCommand(commandClass).createModule();
		context.inject(module);
		preprocessWithoutHarvesting(module);
		setModuleInputs(module, args);
		return module;
	}

	private <M extends Module> void preprocessWithoutHarvesting(M module) {
		ModuleRunner moduleRunner = new ModuleRunner(context, module, preprocessorsWithoutHarvesting(), Collections.emptyList());
		moduleRunner.preProcess();
	}

	private List<? extends PreprocessorPlugin> preprocessorsWithoutHarvesting() {
		//remove input harvesters from preprocessing
		List<PreprocessorPlugin> preprocessors = pluginService.createInstancesOfType(PreprocessorPlugin.class);
		preprocessors.removeIf(preprocessor -> preprocessor instanceof InputHarvester);
		return preprocessors;
	}

	private void setModuleInputs(Module module, Object[] args) {
		assert(args.length % 2 == 0);
		for (int i = 0; i < args.length-1; i+=2) {
			String input = (String) args[i];
			module.setInput(input, args[i+1]);
			module.resolveInput(input);
		}
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void cancel(String reason) {
		canceled = true;
	}

	@Override
	public String getCancelReason() {
		return null;
	}
}
