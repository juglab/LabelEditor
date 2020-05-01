package org.scijava.table.interactive;

/**
 * Interface for listeners of a {@link SelectionModel}.
 *
 */
public interface SelectionListener
{
	/**
	 * Notifies when the select has changed.
	 */
	void selectionChanged();

	/**
	 * Notifies when a focus event happened.
	 * Focus events do not necessarily enter the select at all..
	 */
	void focusChanged();

}
