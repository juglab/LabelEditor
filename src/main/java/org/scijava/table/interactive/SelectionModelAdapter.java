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
