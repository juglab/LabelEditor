package sc.fiji.labeleditor.plugin.table;

import org.scijava.table.interactive.DefaultInteractiveTable;
import org.scijava.table.interactive.SelectionModelAdapter;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;

import java.util.HashMap;
import java.util.Map;

public class LabelEditorTable<L> extends DefaultInteractiveTable<Integer> {

	public LabelEditorTable(InteractiveLabeling<L> labeling) {

		Map<Integer, L> mappingLeft = new HashMap<>();
		Map<L, Integer> mappingRight = new HashMap<>();
		int i = 0;
		for (L label : labeling.model().labeling().getMapping().getLabels()) {
			appendRow();
			for (Object tag : labeling.model().tagging().getTags(label)) {
				if(getColumnIndex(tag.toString()) < 0) {
					appendColumn(tag.toString());
				}
				Object value = labeling.model().tagging().getValue(tag, label);
				if(value != null) set(tag.toString(), i, value.toString());
			}
			i++;
			mappingLeft.put(i, label);
			mappingRight.put(label, i);
		}

		setSelectionModel(new SelectionModelAdapter<>(labeling.getSelectionModel(), mappingRight, mappingLeft));
	}
}
