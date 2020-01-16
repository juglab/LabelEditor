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
import sc.fiji.labeleditor.core.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.DefaultLabelEditorController;
import sc.fiji.labeleditor.core.controller.LabelEditorController;
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
		return initControl(model, view);
	}

	protected <L> LabelEditorView<L> createView(LabelEditorModel<L> model) {
		return new DefaultLabelEditorView<>(model);
	}

	protected void addRenderers(LabelEditorView view) {
		view.addDefaultRenderers();
	}

	private InteractiveLabeling initControl(LabelEditorModel model, LabelEditorView view) {
		LabelEditorController control = createController();
		if(context != null) context.inject(control);
		LabelEditorInterface viewerInstance = new BdvInterface<>(bdvHandlePanel, view);
		InteractiveLabeling labeling = control.init(model, view, viewerInstance);
		addBehaviours(control.interfaceInstance(), labeling);
		return labeling;
	}

	protected LabelEditorController createController() {
		return new DefaultLabelEditorController<>();
	}

	protected void addBehaviours(LabelEditorInterface interfaceInstance, InteractiveLabeling labeling) {
		interfaceInstance.installBehaviours(labeling);
	}

	@Override
	public void dispose() {
		labelings.forEach((name,  labeling) -> labeling.view().dispose());
		if(bdvHandlePanel != null) bdvHandlePanel.close();
	}

	public List<BdvSource> getSources() {
		List<BdvSource> res = new ArrayList<>();
		labelings.forEach((name, labeling) -> {
			BdvInterface<?> labelEditorInterface = (BdvInterface) labeling.control().interfaceInstance();
			labelEditorInterface.getSources().values().forEach(res::addAll);
		});
		return res;
	}
}
