package sc.fiji.labeleditor.plugin.behaviours.modification;

import net.imagej.ops.OpService;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.plugin.Parameter;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorValueTag;
import sc.fiji.labeleditor.core.view.LabelEditorTargetComponent;

import java.util.Random;
import java.util.Set;

public class TagByProperty<L> {

	@Parameter
	OpService ops;

	private final InteractiveLabeling<L> labeling;

	public TagByProperty(InteractiveLabeling<L> labeling) {
		this.labeling = labeling;
	}

	public void circularity() {
		Set<L> labels = labeling.getLabelSetInScope();
		Random random = new Random();
		labels.forEach(label -> {
			LabelEditorValueTag circularity = new LabelEditorValueTag<>("circularity", new IntType(random.nextInt(100)));
			labeling.model().tagging().addTagToLabel(circularity, label);
			labeling.model().colors().getColorset(circularity.getIdentifier()).put(
					LabelEditorTargetComponent.FACE,
					ARGBType.rgba(0,0,255,250),
					ARGBType.rgba(255,0,0,250),
					new IntType(0), new IntType(100)
			);
		});
		//TODO
	}
}
