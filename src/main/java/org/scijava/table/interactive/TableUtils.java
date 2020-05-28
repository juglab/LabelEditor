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

		model.addColumn(""); // row header
		for ( int col = 0; col < numCols; ++col )
		{
			model.addColumn( genericTable.getColumnHeader( col ) );
		}

		for ( int row = 0; row < genericTable.getRowCount(); ++row )
		{
			final Object[] rowEntries = new Object[ numCols+1 ];

			rowEntries[0] = genericTable.getRowHeader(row);

			for ( int col = 0; col < numCols; ++col )
			{
				rowEntries[ col+1 ] = genericTable.get( col, row );
			}

			model.addRow( rowEntries );
		}

		return new JTable( model );
	}

}
