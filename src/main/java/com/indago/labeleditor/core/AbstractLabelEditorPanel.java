package com.indago.labeleditor.core;

import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractLabelEditorPanel<L> extends JPanel implements LabelEditorPanel<L> {

	protected ImgPlus data;

	protected LabelEditorModel<L> model;

	protected boolean panelBuilt = false;
	protected boolean mode3D = false;
	protected LabelEditorController<L> controller;
	private LabelEditorView<L> view = new LabelEditorView<>();

	public AbstractLabelEditorPanel() {
	}

	@Override
	public void init(ImgPlus data) {
		setData(data);
		buildPanel();
		clearInterface();
		displayData();
	}

	@Override
	public void init(ImgPlus data, ImgLabeling<L, IntType> labels) {
		setData(data);
		init(labels);
	}

	@Override
	public void init(ImgPlus data, LabelEditorModel<L> model) {
		setData(data);
		init(model);
	}

	@Override
	public void init(ImgLabeling<L, IntType> labels) {
		init(new DefaultLabelEditorModel<>(labels));
	}

	@Override
	public void init(LabelEditorModel<L> model) {
		if(model != null) {
			this.model = model;
			view.init(model);
			addRenderings(view);
			buildPanel();
			controller = new LabelEditorController<>();
		}
		clearInterface();
		displayData();
		displayLabeling();
		initController();
	}

	protected void setData(ImgPlus data) {
		if(data == null) return;
		this.data = data;
		if(data.dimensionIndex(Axes.Z) > 0) {
			mode3D = true;
		}
	}

	protected void buildPanel() {
		if(panelBuilt) return;
		panelBuilt = true;
		setLayout( new BorderLayout() );
		final JPanel interfacePanel = new JPanel( new MigLayout("fill, w 500, h 500") );
		interfacePanel.add( buildInterface(), "span, grow, push" );
		this.add( interfacePanel );
	}

	protected abstract void initController();

	protected abstract Component buildInterface();

	protected void addRenderings(LabelEditorView<L> renderingManager) {
		renderingManager.renderers().addDefaultRenderers();
	}

	abstract protected void addActionHandlers(LabelEditorController<L> actionManager);

	protected abstract void displayLabeling();

	protected abstract void displayData();

	protected abstract void clearInterface();

	public abstract Object getViewerHandle();

	@Override
	public LabelEditorView<L> view() {
		return view;
	}

	@Override
	public LabelEditorModel<L> model() {
		return model;
	}

	@Override
	public LabelEditorController<L> control() {
		return controller;
	}

	@Override
	public Container get() {
		return this;
	}

}
