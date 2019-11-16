package com.indago.labeleditor.plugin.behaviours.export;

import com.indago.labeleditor.core.controller.LabelEditorBehaviours;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorRenderer;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.*;

public class ExportBehaviours extends Behaviours implements LabelEditorBehaviours {

	protected LabelEditorModel model;
	protected LabelEditorController controller;

	@Parameter
	UIService ui;

	public ExportBehaviours() {
		super(new InputTriggerConfig(), "labeleditor-export");
	}

	@Override
	public void init(LabelEditorModel model, LabelEditorController controller) {
		this.model = model;
		this.controller = controller;
	}

	@Override
	public void install(Behaviours behaviours, Component panel) {

	}

	public ClickBehaviour getExportIndexImgBehaviour() {
		return (arg0, arg1) -> showIndexImg();
	}

	public ClickBehaviour getExportSourceImgBehaviour() {
		return (arg0, arg1) -> showData();
	}

	public ClickBehaviour getExportRendererBehaviour(LabelEditorRenderer renderer) {
		return (arg0, arg1) -> showRenderer(renderer);
	}

	public void showIndexImg() {
		show(model.labels().getIndexImg());
	}

	public ImagePlus showRenderer(LabelEditorRenderer renderer) {
		//TODO replace this as soon as SCIFIO can display ARGB
		//ui.show(renderer.getOutput());
		return ImageJFunctions.show(renderer.getOutput());
	}

	public void showData() {
		show(model.getData());
	}

	private void show(RandomAccessibleInterval img) {
		if(ui != null) {
			ui.show(img);
		} else {
			ImageJFunctions.show(img);
		}
	}
}
