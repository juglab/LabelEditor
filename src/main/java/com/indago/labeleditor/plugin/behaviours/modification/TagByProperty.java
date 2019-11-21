package com.indago.labeleditor.plugin.behaviours.modification;

import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.tagging.LabelEditorValueTag;
import com.indago.labeleditor.core.view.LabelEditorTargetComponent;
import net.imagej.ops.OpService;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.plugin.Parameter;

import java.util.Random;
import java.util.Set;

public class TagByProperty<L> {

	@Parameter
	OpService ops;

	private final LabelEditorController controller;
	private final LabelEditorModel<L> model;

	public TagByProperty(LabelEditorModel<L> model, LabelEditorController controller) {
		this.model = model;
		this.controller = controller;
	}

	public void circularity() {
		Set<L> labels = controller.labelSetInScope();
		Random random = new Random();
		labels.forEach(label -> {
			LabelEditorValueTag circularity = new LabelEditorValueTag<>("circularity", new IntType(random.nextInt(100)));
			model.tagging().addTagToLabel(circularity, label);
			model.colors().get(circularity.getIdentifier()).put(
					LabelEditorTargetComponent.FACE,
					ARGBType.rgba(0,0,255,250),
					ARGBType.rgba(255,0,0,250),
					new IntType(0), new IntType(100)
			);
		});
		//TODO
	}
}
