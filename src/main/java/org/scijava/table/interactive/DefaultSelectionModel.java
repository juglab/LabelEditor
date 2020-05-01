package org.scijava.table.interactive;

import org.scijava.listeners.Listeners;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DefaultSelectionModel< T > implements SelectionModel< T >
{
	private final Listeners.SynchronizedList<SelectionListener> listeners;
	private final Set< T > selected;
	private T focusObject;

	public DefaultSelectionModel()
	{
		listeners = new Listeners.SynchronizedList<>(  );
		selected = new HashSet<>();
	}

	@Override
	public boolean isSelected( T object )
	{
		return selected.contains( object );
	}

	@Override
	public void setSelected( T object, boolean select )
	{
		setSelected( object, select, true );
	}

	public void setSelected( T object, boolean select, boolean notify )
	{
		if ( select )
			add( object, notify );
		else
			remove( object, notify );
	}

	private void remove( T object, boolean notify )
	{
		if ( selected.contains( object ) )
		{
			selected.remove( object );
			if ( notify )
				notifySelectionListeners();
			notifySelectionListeners();
		}
	}

	private void add( T object, boolean notify )
	{
		if ( ! selected.contains( object ) )
		{
			selected.add( object );
			if ( notify )
				notifySelectionListeners();
		}
	}

	private void notifySelectionListeners()
	{
		for ( SelectionListener listener : listeners.list )
			new Thread(listener::selectionChanged).start();
	}

	@Override
	public void toggle( T object )
	{
		if ( selected.contains( object ) )
			remove( object, true );
		else
			add( object, true );
	}

	@Override
	public void focus( T object )
	{
		focusObject = object;

		for ( SelectionListener listener : listeners.list )
			new Thread(listener::focusChanged).start();
	}

	@Override
	public boolean isFocused( T object )
	{
		return focusObject != null && focusObject.equals(object);
	}

	@Override
	public boolean setSelected( Collection< T > objects, boolean select )
	{
		for( T object : objects )
			setSelected( object, select, false );

		notifySelectionListeners();

		return true;
	}

	@Override
	public boolean clearSelection()
	{
		if ( selected.size() == 0 )
			return false;
		else
		{
			selected.clear();
			notifySelectionListeners();
			return true;
		}
	}

	@Override
	public Set< T > getSelected()
	{
		return selected;
	}

	@Override
	public T getFocused() {
		return focusObject;
	}

	@Override
	public boolean isEmpty()
	{
		return selected.isEmpty();
	}

	@Override
	public Listeners<SelectionListener> listeners()
	{
		return listeners;
	}

	@Override
	public void resumeListeners()
	{

	}

	@Override
	public void pauseListeners()
	{

	}

}
