package com.indago.labeleditor.core;

import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractLabelEditorPanel<L> extends JPanel implements LabelEditorPanel<L> {

	@Parameter
	protected Context context;

	private boolean panelBuilt = false;

	private LabelEditorController<L> controller;
	private LabelEditorModel<L> model;
	private LabelEditorView<L> view = new LabelEditorView<>();

	public AbstractLabelEditorPanel() {
	}

	@Override
	public void init(Img data) {
		LabelEditorModel<L> model = new DefaultLabelEditorModel<>();
		model.setData(data);
		init(model);
	}

	@Override
	public void init(ImgLabeling<L, IntType> labels, Img data) {
		LabelEditorModel<L> model = new DefaultLabelEditorModel<>();
		model.setData(data);
		model.init(labels);
		init(model);
	}

	@Override
	public void init(ImgLabeling<L, IntType> labels) {
		LabelEditorModel<L> model = new DefaultLabelEditorModel<>();
		model.init(labels);
		init(model);
	}

	@Override
	public void initFromIndexImage(Img labelMap) {
		init(makeLabeling(labelMap));
	}

	@Override
	public void initFromIndexImage(Img data, Img labelMap) {
		init(makeLabeling(labelMap), data);
	}

	private static <T extends IntegerType<T>> ImgLabeling<IntType, IntType> makeLabeling(Img<T> labelMap) {
		Img<IntType> backing = new ArrayImgFactory<>(new IntType()).create(labelMap);
		ImgLabeling<IntType, IntType> labeling = new ImgLabeling<>(backing);
		Cursor<T> cursor = labelMap.localizingCursor();
		RandomAccess<LabelingType<IntType>> ra = labeling.randomAccess();
		while(cursor.hasNext()) {
			int val = cursor.next().getInteger();
			if(val == 0) continue;
			ra.setPosition(cursor);
			ra.get().add(new IntType(val));
		}
		return labeling;
	}

	@Override
	public void init(LabelEditorModel<L> model) {
		this.model = model;
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
			System.out.println("Created LabelEditor BDV panel:\n" + model.toString());
		}
	}

	protected void buildPanel() {
		if(panelBuilt) return;
		panelBuilt = true;
		setLayout( new MigLayout("fill") );
		this.add( buildInterface(), "span, grow, push" );
	}

	protected abstract void initController();

	protected abstract Component buildInterface();

	protected void addRenderings(LabelEditorView<L> renderingManager) {
		if(context() != null) context().inject(renderingManager.renderers());
		renderingManager.renderers().addDefaultRenderers();
	}

	abstract protected void addBehaviours(LabelEditorController<L> controller);

	protected abstract void displayLabeling();

	protected abstract void displayData();

	protected abstract void clearInterface();

	protected Context context() {
		return context;
	}

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
