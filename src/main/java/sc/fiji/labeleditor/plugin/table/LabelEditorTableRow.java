package sc.fiji.labeleditor.plugin.table;

import de.embl.cba.table.tablerow.TableRow;

import java.util.Map;
import java.util.Set;

public class LabelEditorTableRow implements TableRow
{
	private final int row;
	private final Map< String, Object > columns;

	public LabelEditorTableRow(int row, Map< String, Object > columns )
	{
		this.row = row;
		this.columns = columns;
	}

	@Override
	public String getCell( String columnName )
	{
		return String.valueOf(columns.get( columnName ));
	}

	@Override
	public void setCell( String columnName, String value )
	{
		columns.put( columnName, value );
	}

	@Override
	public Set< String > getColumnNames()
	{
		return columns.keySet();
	}

	@Override
	public int rowIndex()
	{
		return row;
	}
}
