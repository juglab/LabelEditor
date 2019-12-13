package sc.fiji.labeleditor.plugin.table;

import de.embl.cba.table.color.LazyCategoryColoringModel;
import de.embl.cba.table.lut.GlasbeyARGBLut;
import de.embl.cba.table.select.DefaultSelectionModel;
import de.embl.cba.table.tablerow.ColumnBasedTableRow;
import de.embl.cba.table.tablerow.TableRow;
import de.embl.cba.table.view.TableRowsTableView;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorValueTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelEditorTable<L> {

	private final TableRowsTableView<LabelEditorTableRow> tableView;

	public LabelEditorTable(LabelEditorModel<L> model) {

		final List<LabelEditorTableRow> columnBasedTableRows = new ArrayList<>();

		Map<LabelEditorTableRow, L> mappingLeft = new HashMap<>();
		Map<L, LabelEditorTableRow> mappingRight = new HashMap<>();
		int i = 0;
		for (L label : model.labeling().getMapping().getLabels()) {
			Map<String, Object> columnNamesToColumns = new HashMap<>();
			for (Object tag : model.tagging().getTags(label)) {
				try {
					LabelEditorValueTag valueTag = (LabelEditorValueTag) tag;
					columnNamesToColumns.put(valueTag.getName(), valueTag.getValue());
				} catch(ClassCastException ignored){}
			}
			final LabelEditorTableRow tableRow = new LabelEditorTableRow( i++, columnNamesToColumns );
			columnBasedTableRows.add( tableRow );
			mappingLeft.put(tableRow, label);
			mappingRight.put(label, tableRow);
		}

		final LazyCategoryColoringModel< LabelEditorTableRow > coloringModel = new LazyCategoryColoringModel< >( new GlasbeyARGBLut( 255 ) );
//		final DefaultSelectionModel< LabelEditorTableRow > selectionModel = new DefaultSelectionModel<>();


		tableView = new TableRowsTableView<>(
						columnBasedTableRows,
						new SelectionModelAdapter<>(model.getSelectionModel(), mappingRight, mappingLeft),
						coloringModel
				);

	}

	public void show() {
		tableView.showTableAndMenu();
	}
}
