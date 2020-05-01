package org.scijava.table.interactive;

import org.scijava.listeners.Listeners;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SelectionModelAdapter< L, R> implements SelectionModel<R>
{

	private final SelectionModel<L> selection;
	private final Map<R, L> mappingRight;
	private final Map<L, R> mappingLeft;
	private final ForwardedListeners.SynchronizedList<SelectionListener> listeners;

	public SelectionModelAdapter(final SelectionModel< L > selection,
	                             final Map<L, R> mappingLeft, final Map<R, L> mappingRight)
	{
		this.selection = selection;
		this.mappingLeft = mappingLeft;
		this.mappingRight = mappingRight;
		this.listeners = new ForwardedListeners.SynchronizedList<>( selection.listeners() );
	}

	@Override
	public boolean isSelected(R object) {
		return selection.isSelected(mappingRight.get(object));
	}

	@Override
	public void setSelected(R object, boolean select) {
		selection.setSelected(mappingRight.get(object), select);
	}

	@Override
	public void toggle(R object) {
		selection.toggle(mappingRight.get(object));
	}

	@Override
	public void focus(R object) {
		selection.focus(mappingRight.get(object));
	}

	@Override
	public boolean isFocused(R object) {
		return selection.isFocused(mappingRight.get(object));
	}

	@Override
	public boolean setSelected(Collection<R> objects, boolean select) {
		Set<L> left = new HashSet<>();
		for (R object : objects) {
			left.add(mappingRight.get(object));
		}
		return selection.setSelected(left, select);
	}

	@Override
	public boolean clearSelection() {
		return selection.clearSelection();
	}

	@Override
	public Set<R> getSelected() {
		Set<R> selected = new HashSet<>();
		for (L left : selection.getSelected()) {
			selected.add(mappingLeft.get(left));
		}
		return selected;
	}

	@Override
	public R getFocused() {
		return mappingLeft.get(selection.getFocused());
	}

	@Override
	public boolean isEmpty() {
		return selection.isEmpty();
	}

	@Override
	public Listeners<SelectionListener> listeners() {
		return listeners;
	}

	@Override
	public void resumeListeners() {
		selection.resumeListeners();
	}

	@Override
	public void pauseListeners() {
		selection.pauseListeners();
	}
}
