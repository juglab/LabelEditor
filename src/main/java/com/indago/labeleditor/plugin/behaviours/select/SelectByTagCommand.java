package com.indago.labeleditor.plugin.behaviours.select;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import org.scijava.command.Command;
import org.scijava.command.InteractiveCommand;
import org.scijava.module.DefaultMutableModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Plugin(type = Command.class, name = "Select labels by tag", initializer = "initTagList")
public class SelectByTagCommand extends InteractiveCommand {

	@Parameter
	LabelEditorModel model;

	Map<String, Object> namedTags;

	@Override
	public void run() {
		List chosenTags = new ArrayList<>();
		namedTags.forEach((name, tag) -> {
			if((Boolean)getInput(name)) {
				chosenTags.add(tag);
			}
		});
		model.tagging().removeTag(LabelEditorTag.SELECTED);
		for (Object tag : chosenTags) {
			Set labelsWithTag = model.tagging().getLabels(tag);
			labelsWithTag.forEach(label -> {
				model.tagging().addTag(LabelEditorTag.SELECTED, label);
			});
		}
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
