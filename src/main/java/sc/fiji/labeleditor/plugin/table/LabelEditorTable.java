package sc.fiji.labeleditor.plugin.table;

import org.scijava.table.interactive.DefaultInteractiveTable;
import org.scijava.table.interactive.SelectionModelAdapter;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelEditorTable<L> extends DefaultInteractiveTable<Integer> {

	public LabelEditorTable(InteractiveLabeling<L> labeling) {

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
}
