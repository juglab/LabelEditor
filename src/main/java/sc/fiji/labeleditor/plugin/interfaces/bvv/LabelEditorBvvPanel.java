package sc.fiji.labeleditor.plugin.interfaces.bvv;

import bdv.util.Bdv;
import bdv.util.BdvOptions;
import bvv.util.Bvv;
import bvv.util.BvvFunctions;
import bvv.util.BvvHandle;
import bvv.util.BvvStackSource;
import net.imagej.ImgPlus;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.type.numeric.ARGBType;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import org.scijava.Disposable;
import org.scijava.Initializable;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import sc.fiji.labeleditor.core.controller.DefaultInteractiveLabeling;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.DefaultLabelEditorView;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.plugin.interfaces.bdv.LabelEditorAccumulateProjector;
import tpietzsch.example2.VolumeViewerPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelEditorBvvPanel extends JPanel implements Initializable, Disposable {


	@Parameter
	private Context context;

	@Parameter
	private CommandService commandService;

	private final Map<String, InteractiveLabeling> labelings = new HashMap<>();

	private BvvHandle bvvHandle;
	private VolumeViewerPanel bvvPanel;
	private final List<BvvStackSource> bvvSources = new ArrayList<>();

	public LabelEditorBvvPanel() {
		this(null, Bdv.options());
	}

	public LabelEditorBvvPanel(Context context) {
		this(context, Bdv.options());
	}

	public LabelEditorBvvPanel(BdvOptions options) {
		this(null, options);
	}

	public LabelEditorBvvPanel(Context context, BdvOptions options) {
		if(context != null) context.inject(this);
		initialize(options);
	}

	private void initialize(BdvOptions options) {
		adjustOptions(options);
		setMinimumSize(new Dimension(100, 100));
		setPreferredSize(new Dimension(500, 500));
		setLayout( new MigLayout("fill") );
		BvvStackSource<ARGBType> source1 = BvvFunctions.show(fakeImg(), "", Bvv.options());
		bvvSources.add(source1);
		bvvHandle = source1.getBvvHandle();
		bvvPanel = bvvHandle.getViewerPanel();
		Component viewer = bvvPanel;
		this.add( viewer, "span, grow, push" );
	}

	protected void adjustOptions(BdvOptions options) {
		options.accumulateProjectorFactory(LabelEditorAccumulateProjector.factory);
	}

	public <L> InteractiveLabeling<L> add(LabelEditorModel<L> model) {
		LabelEditorView<L> view = new DefaultLabelEditorView<>(model);
		if(context != null) context.inject(view);
		view.addDefaultRenderers();
		return add(model, view);
	}

	public <L> InteractiveLabeling<L> add(LabelEditorModel<L> model, LabelEditorView<L> view) {
		LabelEditorInterface<L> interfaceInstance = new BvvInterface<>(bvvHandle);
		return add(model, view, interfaceInstance);
	}

	public <L> InteractiveLabeling<L> add(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorInterface<L> interfaceInstance) {
		DefaultInteractiveLabeling<L> interactiveLabeling = new DefaultInteractiveLabeling<>(model, view, interfaceInstance);
		if(context != null) context.inject(interactiveLabeling);
		interactiveLabeling.initialize();
		return interactiveLabeling;
	}

	@Override
	public void dispose() {
		if(bvvHandle != null) bvvHandle.close();
	}

	protected Context context() {
		return context;
	}

	private ImgPlus<ARGBType> fakeImg() {
		return new ImgPlus<>(new DiskCachedCellImgFactory<>(new ARGBType()).create(10, 10));
	}

}
