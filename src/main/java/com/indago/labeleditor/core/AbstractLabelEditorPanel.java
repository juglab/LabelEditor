package com.indago.labeleditor.core;

import com.indago.labeleditor.core.controller.DefaultLabelEditorController;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractLabelEditorPanel extends JPanel implements LabelEditorPanel {

	@Parameter
	protected Context context;

	private boolean panelBuilt = false;

	private LabelEditorModel model;
	private LabelEditorController controller = new DefaultLabelEditorController<>();
	private LabelEditorView view = new LabelEditorView<>();

	public AbstractLabelEditorPanel() {
	}

	@Override
	public void init(Img data) {
		buildPanel();
		clearInterface();
		displayData(data);
	}

	@Override
	public <L> void init(ImgLabeling<L, IntType> labels, Img data) {
		LabelEditorModel<L> model = new DefaultLabelEditorModel<>(labels);
		model.setData(data);
		init(model);
	}

	@Override
	public <L> void init(ImgLabeling<L, IntType> labels) {
		LabelEditorModel<L> model = new DefaultLabelEditorModel<>(labels);
		init(model);
	}

	@Override
	public void initFromLabelMap(Img labelMap) {
		LabelEditorModel<IntType> model = DefaultLabelEditorModel.initFromLabelMap(labelMap);
		init(model);
	}

	@Override
	public void initFromLabelMap(Img data, Img labelMap) {
		LabelEditorModel<IntType> model = DefaultLabelEditorModel.initFromLabelMap(labelMap);
		model.setData(data);
		init(model);
	}

	@Override
	public <L> void init(LabelEditorModel<L> model) {
		//TODO this is not pretty
		if(this.model == null) {
			if(context() != null) {
				context().inject(view().renderers());
				context().inject(control());
			}
		}
		this.model = model;
		if(model.labeling() != null) {
			view().init(model);
			addRenderers(view());
		}
		buildPanel();
		clearInterface();
		displayData(model.getData());
		if(model.labeling() != null) {
			displayLabeling();
			initController();
			System.out.println("Created LabelEditor BDV panel:\n" + model.toString());
		}
	}

	protected void buildPanel() {
		if(panelBuilt) return;
		panelBuilt = true;
		setMinimumSize(new Dimension(100, 100));
		setPreferredSize(new Dimension(500, 500));
		setLayout( new MigLayout("fill") );
		this.add( buildInterface(), "span, grow, push" );
	}

	protected abstract void initController();

	protected abstract Component buildInterface();

	protected void addRenderers(LabelEditorView view) {
		view.renderers().addDefaultRenderers();
	}

	abstract protected void addBehaviours(LabelEditorController controller);

	protected abstract void displayLabeling();

	protected abstract void displayData(Img data);

	protected abstract void clearInterface();

	protected Context context() {
		return context;
	}

	public abstract Object getInterfaceHandle();

	@Override
	public LabelEditorModel model() {
		return model;
	}

	@Override
	public LabelEditorView view() {
		return view;
	}

	@Override
	public LabelEditorController control() {
		return controller;
	}

	@Override
	public Container get() {
		return this;
	}

}
