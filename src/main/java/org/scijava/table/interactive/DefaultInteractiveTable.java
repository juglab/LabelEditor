package org.scijava.table.interactive;

import org.scijava.table.DefaultGenericTable;

public class DefaultInteractiveTable<T> extends DefaultGenericTable implements InteractiveTable<T> {

	private SelectionModel<T> selectionModel;

	public void setSelectionModel(SelectionModel<T> model) {
		this.selectionModel = model;
	}

	@Override
	public SelectionModel<T> getSelectionModel() {
		return selectionModel;
	}
}
