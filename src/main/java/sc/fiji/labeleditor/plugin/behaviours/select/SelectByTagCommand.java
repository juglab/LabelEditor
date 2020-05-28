/*-
 * #%L
 * UI component for image segmentation label comparison and selection
 * %%
 * Copyright (C) 2019 - 2020 DAIS developers
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package sc.fiji.labeleditor.plugin.behaviours.select;

import org.scijava.command.Command;
import org.scijava.command.InteractiveCommand;
import org.scijava.module.DefaultMutableModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;

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
	InteractiveLabeling labeling;

	Map<String, Object> namedTags;

	@Override
	public void run() {
		Set chosenTags = new HashSet();
		namedTags.forEach((name, tag) -> {
			if((Boolean)getInput(name)) {
				chosenTags.add(tag);
			}
		});
		Set selectedLabels = model.tagging().filterLabelsWithTag(labeling.getLabelSetInScope(), LabelEditorTag.SELECTED);
		Set toSelect = model.tagging().filterLabelsWithAnyTag(labeling.getLabelSetInScope(), chosenTags);
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
