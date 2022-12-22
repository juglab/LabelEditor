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
package org.scijava.table.interactive;

import org.scijava.table.GenericTable;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

public class InteractiveTableDisplayViewer extends JPanel
{
	private final GenericTable genericTable;
	private final SelectionModel< Integer > selectionModel;
	private final String tableName;

	private JFrame frame;

	private JTable table;
	private int recentlySelectedRowInView;
	private Component parentComponent;
	private ListSelectionListener tableSelectionListener;
	private SelectionListener modelSelectionListener;

	public InteractiveTableDisplayViewer(
			final InteractiveTable<Integer> table )
	{
		super( new GridLayout(1, 0 ) );
		this.selectionModel = table.getSelectionModel();
		this.tableName = "";
		this.table = TableUtils.asJTable(table);
		this.genericTable = table;

		recentlySelectedRowInView = -1;

		createModelSelectionListener();
		registerModelSelectionListener( selectionModel );
	}

	public void display()
	{
		configureJTable();
		createTableSelectionListener();
		registerTableSelectionListener();
		makeFrame();
	}

	private void createTableSelectionListener() {
		tableSelectionListener = e ->
				SwingUtilities.invokeLater(() ->
				{
					if (e.getValueIsAdjusting()) return;

					final int selectedRowInView = table.getSelectedRow();

					if (selectedRowInView == -1) return;

					if (selectedRowInView == recentlySelectedRowInView) return;

					recentlySelectedRowInView = selectedRowInView;

					final int row = table.convertRowIndexToModel(recentlySelectedRowInView);

					unregisterModelSelectionListener( selectionModel );

					selectionModel.clearSelection();
					selectionModel.toggle(row);
					if (selectionModel.isSelected(row))
						selectionModel.focus(row);

					registerModelSelectionListener( selectionModel );

					table.repaint();
				});
	}

	private void createModelSelectionListener() {
		modelSelectionListener = new SelectionListener() {
			@Override
			public synchronized void selectionChanged() {
				if (selectionModel.isEmpty()) {
					recentlySelectedRowInView = -1;
					unregisterTableSelectionListener();
					table.getSelectionModel().clearSelection();
					registerTableSelectionListener();
				} else {
					unregisterTableSelectionListener();
					Integer selected = selectionModel.getSelected().iterator().next();
					table.getSelectionModel().clearSelection();
					table.getSelectionModel().setSelectionInterval(selected, selected);
					registerTableSelectionListener();
				}
				SwingUtilities.invokeLater(() -> table.repaint());
			}

			@Override
			public synchronized void focusChanged() {
				SwingUtilities.invokeLater(() -> {
					moveToSelectedTableRow(selectionModel.getFocused());
				});
			}

		};
	}


	private void configureJTable()
	{
		table = TableUtils.asJTable( genericTable );
		table.setPreferredScrollableViewportSize( new Dimension(500, 200) );
		table.setFillsViewportHeight( true );
		table.setAutoCreateRowSorter( true );
		table.setRowSelectionAllowed( true );
		table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

		JScrollPane scrollPane = new JScrollPane(
				table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		add(scrollPane);

		table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

		updateUI();
	}

	private void makeFrame()
	{
		frame = new JFrame( tableName );

		//Show the model
		//frame.add( scrollPane );

		//Create and set up the content pane.
		this.setOpaque( true ); //content panes must be opaque
		frame.setContentPane( this );

		if ( parentComponent != null )
		{
			frame.setLocation(
					parentComponent.getLocationOnScreen().x,
					parentComponent.getLocationOnScreen().y + parentComponent.getHeight() + 10
			);

			frame.setPreferredSize( new Dimension(
					parentComponent.getWidth(),
					parentComponent.getHeight() / 3  ) );
		}


		//Display the window.
		frame.pack();
		frame.setVisible( true );
	}

	public JTable getTable()
	{
		return table;
	}

	public void moveToRowInView( int rowInView )
	{
		recentlySelectedRowInView = rowInView;
		//model.getSelectionModel().setSelectionInterval( rowInView, rowInView );
		final Rectangle visibleRect = table.getVisibleRect();
		final Rectangle cellRect = table.getCellRect( rowInView, 0, true );
		visibleRect.y = cellRect.y;
		table.scrollRectToVisible( visibleRect );
		table.repaint();
	}

	private void registerTableSelectionListener()
	{
		table.getSelectionModel().addListSelectionListener(tableSelectionListener);
	}

	private void unregisterTableSelectionListener()
	{
		table.getSelectionModel().removeListSelectionListener(tableSelectionListener);
	}

	private void registerModelSelectionListener(SelectionModel<Integer> selectionModel)
	{
		selectionModel.listeners().add(modelSelectionListener);
	}

	private void unregisterModelSelectionListener(SelectionModel<Integer> selectionModel)
	{
		selectionModel.listeners().remove(modelSelectionListener);
	}

	private void moveToSelectedTableRow(Integer selection)
	{
		final int rowInView = table.convertRowIndexToView( selection );

		if ( rowInView == recentlySelectedRowInView ) return;

		moveToRowInView( rowInView );
	}

	public void close()
	{
		frame.dispose();
	}

	public void setParentComponent( Component component )
	{
		this.parentComponent = component;
	}
}
