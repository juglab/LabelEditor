package org.scijava.table.interactive;

import org.scijava.table.GenericTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

class TableUtils
{
	static JTable asJTable(GenericTable genericTable)
	{
		final int numCols = genericTable.getColumnCount();

		DefaultTableModel model = new DefaultTableModel() {
			public boolean isCellEditable(int row, int column){
				return false;
			}
		};

		for ( int col = 0; col < numCols; ++col )
		{
			model.addColumn( genericTable.getColumnHeader( col ) );
		}

		for ( int row = 0; row < genericTable.getRowCount(); ++row )
		{
			final Object[] rowEntries = new Object[ numCols ];

			for ( int col = 0; col < numCols; ++col )
			{
				rowEntries[ col ] = genericTable.get( col, row );
			}

			model.addRow( rowEntries );
		}

		return new JTable( model );
	}

}
