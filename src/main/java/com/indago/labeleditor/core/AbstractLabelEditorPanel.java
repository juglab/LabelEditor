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

	private boolean panelBuilt = false;

	private LabelEditorController<L> controller;
	private LabelEditorModel<L> model;
	private LabelEditorView<L> view = new LabelEditorView<>();

	public AbstractLabelEditorPanel() {
	}

	@Override
	public void init(ImgPlus data) {
		init(data, new DefaultLabelEditorModel<>());
	}

	@Override
	public void init(ImgPlus data, ImgLabeling<L, IntType> labels) {
		init(data, new DefaultLabelEditorModel<>(labels));
	}

	@Override
	public void init(ImgLabeling<L, IntType> labels) {
		init(new DefaultLabelEditorModel<>(labels));
	}

	@Override
	public void init(LabelEditorModel<L> model) {
		init(null, model);
	}

	@Override
	public void init(ImgPlus data, LabelEditorModel<L> model) {
		this.model = model;
		if(data != null) {
			setData(data);
		}
		if(model.labels() != null) {
			view.init(model);
			addRenderings(view);
			controller = new LabelEditorController<>();
		}
		buildPanel();
		clearInterface();
		displayData();
		if(model.labels() != null) {
			displayLabeling();
			initController();
		}
	}

	protected void setData(ImgPlus data) {
		if(data == null) return;
		model.setData(data);
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

	abstract protected void addBehaviours(LabelEditorController<L> controller);

	protected abstract void displayLabeling();

	protected abstract void displayData();

	protected abstract void clearInterface();

	public abstract Object getInterfaceHandle();

	@Override
	public LabelEditorModel<L> model() {
		return model;
	}

	@Override
	public LabelEditorView<L> view() {
		return view;
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
