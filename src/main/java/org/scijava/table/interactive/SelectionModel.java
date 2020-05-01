package org.scijava.table.interactive;

import org.scijava.listeners.Listeners;

import java.util.Collection;
import java.util.Set;

public interface SelectionModel< T >
{
	/**
	 * Get the selected state of a object.
	 *
	 * @param object
	 *            a object.
	 * @return {@code true} if specified object is selected.
	 */
	boolean isSelected(final T object);

	/**
	 * Sets the selected state of a object.
	 *
	 * @param object
	 *            a object.
	 * @param select
	 *            selected state to set for specified object.
	 */
	void setSelected(final T object, final boolean select);

	/**
	 * Toggles the selected state of a object.
	 *
	 * @param object
	 *            a object.
	 */
	void toggle(final T object);

	/**
	 * Focus on an object without changing its selection state.
	 *
	 * @param object
	 *            a object.
	 */
	void focus(final T object);

	/**
	 * Get the focus state of an object
	 *
	 * @param object
	 *            a object.
	 */
	boolean isFocused(final T object);

	/**
	 * Sets the selected state of a collection of imagesegment.
	 *
	 * @param objects
	 *            the object collection.
	 * @param select
	 *            selected state to set for specified object collection.
	 * @return {@code true} if the select was changed by this call.
	 */
	boolean setSelected(final Collection<T> objects, final boolean select);

	/**
	 * Clears this select.
	 *
	 * @return {@code true} if this select was not empty prior to
	 *         calling this method.
	 */
	boolean clearSelection();

	/**
	 * Get the selected imagesegment.
	 **
	 * @return a <b>new</b> {@link Set} containing all the selected imagesegment.
	 */
	Set< T > getSelected();

	T getFocused();

	boolean isEmpty();

	/**
	 * Get the list of select listeners. Add a {@link SelectionListener} to
	 * this list, for being notified when the object/edge select changes.
	 *
	 * @return the list of listeners
	 */
	Listeners<SelectionListener> listeners();

	void resumeListeners();

	void pauseListeners();
}

