package sc.fiji.labeleditor.plugin.behaviours.select;

import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import org.scijava.command.Command;
import org.scijava.command.InteractiveCommand;
import org.scijava.module.DefaultMutableModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Plugin(type = Command.class, name = "Select labels by tag", initializer = "initTagList")
public class SelectByTagCommand extends InteractiveCommand {

	@Parameter
	LabelEditorModel model;

	@Parameter
	LabelEditorController control;

	Map<String, Object> namedTags;

	@Override
	public void run() {
		Set chosenTags = new HashSet();
		namedTags.forEach((name, tag) -> {
			if((Boolean)getInput(name)) {
				chosenTags.add(tag);
			}
		});
		Set selectedLabels = model.tagging().filterLabelsWithTag(control.getLabelSetInScope(), LabelEditorTag.SELECTED);
		Set toSelect = model.tagging().filterLabelsWithAnyTag(control.getLabelSetInScope(), chosenTags);
		Set toUnselect = new HashSet(selectedLabels);
		toUnselect.removeAll(toSelect);
		toSelect.remove(selectedLabels);

		model.tagging().pauseListeners();
		toUnselect.forEach(label -> model.tagging().removeTagFromLabel(LabelEditorTag.SELECTED, label));
		toSelect.forEach(label -> model.tagging().addTagToLabel(LabelEditorTag.SELECTED, label));
		model.tagging().resumeListeners();
	}

	protected void initTagList() {
		Set tags = model.tagging().getAllTags();
		tags.removeAll(Arrays.asList(LabelEditorTag.values()));
		if(tags.size() == 0) {
			cancel("No tags assigned");
		}
		namedTags = new HashMap<>();
		tags.forEach(tag -> namedTags.put(tag.toString(), tag));
		tags.forEach(tag -> {
			final DefaultMutableModuleItem<Boolean> tagItem =
					new DefaultMutableModuleItem<>(this, tag.toString(), Boolean.class);
			tagItem.setPersisted(false);
			tagItem.setValue(this, false);
			addInput(tagItem);
		});
	}

}
