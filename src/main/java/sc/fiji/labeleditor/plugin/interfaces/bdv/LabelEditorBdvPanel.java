package sc.fiji.labeleditor.plugin.interfaces.bdv;

import bdv.util.Bdv;
import bdv.util.BdvHandle;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import org.scijava.Disposable;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import sc.fiji.labeleditor.core.controller.DefaultInteractiveLabeling;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.DefaultLabelEditorView;
import sc.fiji.labeleditor.core.view.LabelEditorView;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelEditorBdvPanel extends JPanel implements Disposable {

	@Parameter
	private Context context;

	@Parameter
	private CommandService commandService;

	private final Map<String, InteractiveLabeling> labelings = new HashMap<>();

	private BdvHandlePanel bdvHandlePanel;

	public LabelEditorBdvPanel() {
		this(null, Bdv.options());
	}

	public LabelEditorBdvPanel(Context context) {
		this(context, Bdv.options());
	}

	public LabelEditorBdvPanel(BdvOptions options) {
		this(null, options);
	}

	public LabelEditorBdvPanel(Context context, BdvOptions options) {
		if(context != null) context.inject(this);
		initialize(options);
	}

	private void initialize(BdvOptions options) {
		adjustOptions(options);
		setMinimumSize(new Dimension(100, 100));
		setPreferredSize(new Dimension(500, 500));
		setLayout( new MigLayout("fill") );
		bdvHandlePanel = new BdvHandlePanel(null, options);
		Component viewer = bdvHandlePanel.getViewerPanel();
		this.add( viewer, "span, grow, push" );
	}

	protected void adjustOptions(BdvOptions options) {
		options.accumulateProjectorFactory(LabelEditorAccumulateProjector.factory);
	}
	}

	public <L> InteractiveLabeling<L> add(LabelEditorModel<L> model) {
		LabelEditorView<L> view = createView(model);
		if(context != null) context.inject(view);
		addRenderers(view);
		return initLabeling(model, view);
	}

	protected <L> LabelEditorView<L> createView(LabelEditorModel<L> model) {
		return new DefaultLabelEditorView<>(model);
	}

	protected void addRenderers(LabelEditorView view) {
		view.addDefaultRenderers();
	}

	private <L> InteractiveLabeling<L> initLabeling(LabelEditorModel<L> model, LabelEditorView<L> view) {
		LabelEditorInterface<L> viewerInstance = new BdvInterface<>(bdvHandlePanel);
		InteractiveLabeling<L> labeling = createInteractiveLabeling(model, view);
		if(context != null) context.inject(labeling);
		labeling.init(viewerInstance);
		addBehaviours(labeling.interfaceInstance(), labeling);
		labelings.put(labeling.model().getName(), labeling);
		return labeling;
	}

	protected <L> InteractiveLabeling<L> createInteractiveLabeling(LabelEditorModel<L> model, LabelEditorView<L> view) {
		return new DefaultInteractiveLabeling<>(model, view);
	}

	protected <L> void addBehaviours(LabelEditorInterface<L> interfaceInstance, InteractiveLabeling<L> labeling) {
		interfaceInstance.installBehaviours(labeling);
	}

	@Override
	public void dispose() {
		if(bdvHandlePanel != null) bdvHandlePanel.close();
	}

	public List<BdvSource> getSources() {
		List<BdvSource> res = new ArrayList<>();
		labelings.forEach((name, labeling) -> {
			BdvInterface<?> labelEditorInterface = (BdvInterface) labeling.interfaceInstance();
			labelEditorInterface.getSources().values().forEach(res::addAll);
		});
		return res;
	}
}
