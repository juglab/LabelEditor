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
package sc.fiji.labeleditor.core.model.colors;

import org.scijava.listeners.Listeners;
import sc.fiji.labeleditor.core.view.LabelEditorTargetComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DefaultLabelEditorTagColors extends HashMap<Object, LabelEditorColorset> implements LabelEditorTagColors {

	private final Listeners.List<ColorChangeListener> listeners = new Listeners.SynchronizedList<>();
	private boolean listenersPaused = false;
	private List<ColorChangedEvent> keptEvents = new ArrayList<>();

	public DefaultLabelEditorTagColors() {
	}

	@Override
	public LabelEditorColorset getColorset(Object tag) {
		return get(tag);
	}

	@Override
	public Listeners<ColorChangeListener> listeners() {
		return listeners;
	}

	@Override
	public void pauseListeners() {
		listenersPaused = true;
	}

	@Override
	public void resumeListeners() {
		listenersPaused = false;
		if(keptEvents.size() > 0) {
//			System.out.println(keptEvents);
			listeners.list.forEach(listener -> listener.colorChanged(keptEvents));
			keptEvents.clear();
		}
	}

	@Override
	public void notifyListeners() {
		ColorChangedEvent e = new ColorChangedEvent();
		if(listenersPaused) {
			keptEvents.add(e);
		} else {
			listeners.list.forEach(listener -> listener.colorChanged(Collections.singletonList(e)));
		}
	}

	@Override
	public LabelEditorColor getFaceColor(Object tag) {
		LabelEditorTargetComponent target = LabelEditorTargetComponent.FACE;
		return returnNotNullColor(tag, target);
	}

	@Override
	public LabelEditorColor getBorderColor(Object tag) {
		LabelEditorTargetComponent target = LabelEditorTargetComponent.BORDER;
		return returnNotNullColor(tag, target);
	}

	private LabelEditorColor returnNotNullColor(Object tag, LabelEditorTargetComponent target) {
		LabelEditorColorset colorset = getColorset(tag);
		if(colorset == null) {
			colorset = new DefaultLabelEditorColorset(this);
			colorset.put(target, 0);
			put(tag, colorset);
		}
		LabelEditorColor color = colorset.get(target);
		if(color == null) {
			color = new DefaultLabelEditorColor(colorset, 0);
			colorset.put(target, color);
		}
		return color;
	}

	// convenience methods

}
