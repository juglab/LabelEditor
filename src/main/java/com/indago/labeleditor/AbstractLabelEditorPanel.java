package com.indago.labeleditor;

import com.indago.labeleditor.action.ActionHandler;
import com.indago.labeleditor.action.ActionManager;
import com.indago.labeleditor.display.DefaultLabelEditorRenderer;
import com.indago.labeleditor.display.LabelEditorRenderer;
import com.indago.labeleditor.display.RenderingManager;
import com.indago.labeleditor.model.DefaultLabelEditorModel;
import com.indago.labeleditor.model.LabelEditorModel;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLabelEditorPanel<L> extends JPanel implements LabelEditorPanel<L> {

	protected ImgPlus data;

	protected LabelEditorModel<L> model;

	protected boolean panelBuilt = false;
	protected boolean mode3D = false;
	private ActionManager<L> actionManager = new ActionManager<>();
	private RenderingManager<L> renderingManager = new RenderingManager<>();

	public AbstractLabelEditorPanel() {
	}

	@Override
	public void init(ImgPlus data) {
		setData(data);
		buildPanel();
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
		actionManager.clear();
		if(model != null) {
			this.model = model;
			renderingManager.init(model);
			renderingManager.addAll(initRenderers());
			buildPanel();
			ActionHandler<L> actionHandler = initActionHandler(model, renderingManager);
			actionHandler.set3DViewMode(mode3D);
			actionHandler.init();
			actionManager.add(actionHandler);
		}
	}

	protected void setData(ImgPlus data) {
		if(data == null) return;
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

	protected abstract ActionHandler<L> initActionHandler(LabelEditorModel<L> model, RenderingManager<L> renderer);

	protected List<LabelEditorRenderer<L>> initRenderers() {
		List<LabelEditorRenderer<L>> res = new ArrayList<>();
		res.add(new DefaultLabelEditorRenderer<L>());
		return res;
	}

	@Override
	public abstract void updateLabelRendering();

	@Override
	public RenderingManager<L> rendering() {
		return renderingManager;
	}

	@Override
	public LabelEditorModel<L> model() {
		return model;
	}

	@Override
	public ActionManager<L> action() {
		return actionManager;
	}

	@Override
	public Container get() {
		return this;
	}

}
