/*-
 * #%L
 * UI component for image segmentation label comparison and selection
 * %%
 * Copyright (C) 2019 - 2022 Deborah Schmidt
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
package sc.fiji.labeleditor.plugin.table;

import org.scijava.table.interactive.DefaultInteractiveTable;
import org.scijava.table.interactive.SelectionModelAdapter;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import sc.fiji.labeleditor.core.model.tagging.TagChangeListener;
import sc.fiji.labeleditor.core.model.tagging.TagChangedEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO since the DefaultLabelEditorTagging is already a table, it might be better to display it directly
public class LabelEditorTable<L> extends DefaultInteractiveTable<Integer> implements TagChangeListener {

	private final InteractiveLabeling<L> labeling;

	public LabelEditorTable(InteractiveLabeling<L> labeling) {

		this.labeling = labeling;

		populateTable();
		labeling.model().tagging().listeners().add(this);
	}

	private void populateTable() {
		Map<Integer, L> mappingLeft = new HashMap<>();
		Map<L, Integer> mappingRight = new HashMap<>();
		int i = 0;
		List<LabelEditorTag> labelEditorTags = Arrays.asList(LabelEditorTag.values());
		for (L label : labeling.model().labeling().getMapping().getLabels()) {
			appendRow(label.toString());
			for (Object tag : labeling.model().tagging().getTags(label)) {
				if(labelEditorTags.contains(tag)) continue;
				int col = getColumnIndex(tag.toString());
				if(col < 0) {
					appendColumn(tag.toString());
					col = getColumnCount()-1;
				}
				Object value = labeling.model().tagging().getValue(tag, label);
				if(value != null) set(col, i, value.toString());
			}
			mappingLeft.put(i, label);
			mappingRight.put(label, i);
			i++;
		}
		setSelectionModel(new SelectionModelAdapter<>(labeling.getSelectionModel(), mappingRight, mappingLeft));
	}

	@Override
	public void tagChanged(List<TagChangedEvent> e) {
		e.forEach(event -> {
			if(event.model != labeling.model()) return;
			//TODO update table entry
		});
	}
}
