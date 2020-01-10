package sc.fiji.labeleditor.core;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import sc.fiji.labeleditor.core.controller.DefaultLabelEditorController;
import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.DefaultLabelEditorView;
import sc.fiji.labeleditor.core.view.LabelEditorView;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLabelEditorPanel extends JPanel implements LabelEditorPanel {

	@Parameter
	protected Context context;

	@Parameter
	protected ObjectService objectService;

	private boolean panelBuilt = false;

	private List<InteractiveLabeling> labelings = new ArrayList<>();

	public AbstractLabelEditorPanel() {
		setMinimumSize(new Dimension(100, 100));
		setPreferredSize(new Dimension(500, 500));
		setLayout( new MigLayout("fill") );
		this.add( buildInterface(), "span, grow, push" );
	}

	@Override
	public <L> InteractiveLabeling add(ImgLabeling<L, IntType> labels, Img data) {
		LabelEditorModel<L> model = new DefaultLabelEditorModel<>(labels, data);
		return add(model);
	}

	@Override
	public <L> InteractiveLabeling add(ImgLabeling<L, IntType> labels) {
		LabelEditorModel<L> model = new DefaultLabelEditorModel<>(labels);
		return add(model);
	}

	@Override
	public InteractiveLabeling addFromLabelMap(Img labelMap) {
		LabelEditorModel<IntType> model = DefaultLabelEditorModel.initFromLabelMap(labelMap);
		return add(model);
	}

	@Override
	public InteractiveLabeling addFromLabelMap(Img data, Img labelMap) {
		LabelEditorModel<IntType> model = DefaultLabelEditorModel.initFromLabelMap(labelMap, data);
		return add(model);
	}

	@Override
	public <L> InteractiveLabeling add(LabelEditorModel<L> model) {
		LabelEditorView<L> view = createView();
		view.init(model);
		addRenderers(view);
		view.renderers().addDefaultRenderers();
		LabelEditorController<L> control = createController();
		if(context() != null) {
			context().inject(view.renderers());
			context().inject(control);
		}
		initController(model, view, control);
		display(model.getData());
		display(view);
		DefaultInteractiveLabeling labeling = new DefaultInteractiveLabeling(model, view, control);
		labelings.add(labeling);
		if(objectService != null) {
			objectService.addObject(labeling);
		}
		System.out.println("Created LabelEditor panel of type " + getClass().getName() + " with model:\n" + model.toString());
		return labeling;
	}

	protected <L> LabelEditorView<L> createView() {
		return new DefaultLabelEditorView<>();
	}

	protected <L> LabelEditorController<L> createController() {
		return new DefaultLabelEditorController<>();
	}

	protected abstract <L> void initController(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorController<L> controller);

	protected abstract Component buildInterface();

	protected void addRenderers(LabelEditorView view) {
		view.renderers().addDefaultRenderers();
	}

	abstract protected void addBehaviours(LabelEditorController controller);

	protected abstract void display(RandomAccessibleInterval data);

	protected abstract void display(LabelEditorView view);

	protected Context context() {
		return context;
	}

	public abstract Object getInterfaceHandle();

	@Override
	public void dispose() {
		labelings.forEach(labeling -> labeling.view().dispose());
	}

	@Override
	public Container get() {
		return this;
	}

}
