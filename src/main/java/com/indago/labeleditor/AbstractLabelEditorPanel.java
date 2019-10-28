package com.indago.labeleditor;

import com.indago.labeleditor.action.ActionHandler;
import com.indago.labeleditor.display.DefaultLabelEditorRenderer;
import com.indago.labeleditor.display.LabelEditorRenderer;
import com.indago.labeleditor.model.DefaultLabelEditorModel;
import com.indago.labeleditor.model.LabelEditorModel;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractLabelEditorPanel<L> extends JPanel {

	protected ImgPlus data;

	protected LabelEditorModel<L> model;
	protected LabelEditorRenderer<L> renderer;
	protected ActionHandler<L> actionHandler;

	protected boolean panelBuilt = false;
	protected boolean mode3D = false;

	public AbstractLabelEditorPanel() {
	}

	public AbstractLabelEditorPanel(ImgPlus data) {
		setData(data);
		buildPanel();
	}

	public AbstractLabelEditorPanel(ImgLabeling<L, IntType> labels) {
		init(labels);
	}

	public AbstractLabelEditorPanel(ImgPlus data, ImgLabeling<L, IntType > labels) {
		init(data, labels);
	}

	public AbstractLabelEditorPanel(LabelEditorModel<L> model) {
		init(model);
	}

	public AbstractLabelEditorPanel(ImgPlus data, LabelEditorModel<L> model) {
		setData(data);
		init(model);
	}

	public void init(ImgPlus data, ImgLabeling<L, IntType> labels) {
		setData(data);
		init(labels);
	}

	public void init(ImgLabeling<L, IntType> labels) {
		init(new DefaultLabelEditorModel<>(labels));
	}

	public void init(LabelEditorModel<L> model) {
		if(model != null) {
			this.model = model;
			renderer = initRenderer(model);
		}
		buildPanel();
		if(model != null) {
			actionHandler = initActionHandler(model, renderer);
			actionHandler.set3DViewMode(false);
			actionHandler.init();
		}
	}

	private void setData(ImgPlus data) {
		this.data = data;
		if(data.dimensionIndex(Axes.Z) > 0) {
			mode3D = true;
		}
	}

	private void buildPanel() {
		if(panelBuilt) return;
		panelBuilt = true;
		//this limits the BDV navigation to 2D
		setLayout( new BorderLayout() );
		final JPanel viewer = new JPanel( new MigLayout("fill, w 500, h 500") );
		viewer.add( buildViewer(), "span, grow, push" );
		this.add( viewer );
	}

	protected abstract Component buildViewer();

	protected abstract ActionHandler<L> initActionHandler(LabelEditorModel<L> model, LabelEditorRenderer<L> renderer);

	protected LabelEditorRenderer<L> initRenderer(LabelEditorModel<L> model) {
		return new DefaultLabelEditorRenderer<L>(model);
	}

	public abstract void updateLabelRendering();

	public LabelEditorRenderer<L> getRenderer() {
		return renderer;
	}

	public LabelEditorModel<L> getModel() {
		return model;
	}

	public ActionHandler<L> getActionHandler() {
		return actionHandler;
	}
}
