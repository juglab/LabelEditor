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

import net.imglib2.type.numeric.RealType;

import java.util.HashMap;

public class DefaultLabelEditorColorset extends HashMap<Object, LabelEditorColor> implements LabelEditorColorset {

	private final LabelEditorTagColors colors;

	public DefaultLabelEditorColorset(LabelEditorTagColors colors) {
		this.colors = colors;
	}

	@Override
	public LabelEditorColor put(Object o, int color) {
		LabelEditorColor put = super.put(o, new DefaultLabelEditorColor(this, color));
		update();
		return put;
	}

	@Override
	public LabelEditorColor put(Object o, LabelEditorColor color) {
		LabelEditorColor put = super.put(o, color);
		update();
		return put;
	}

	@Override
	public <T extends RealType<T>> LabelEditorColor put(Object o, int minColor, int maxColor, T min, T max) {
		LabelEditorValueColor<T> color = new LabelEditorValueColor<>(this, min, max);
		color.setMinColor(minColor).setMaxColor(maxColor);
		LabelEditorColor put = super.put(o, color);
		update();
		return put;
	}

	@Override
	public void update() {
		if(colors != null) colors.notifyListeners();
	}

	@Override
	public LabelEditorColor remove(Object o) {
		LabelEditorColor remove = super.remove(o);
		update();
		return remove;
	}

	@Override
	public void clear() {
		super.clear();
		update();
	}

	@Override
	public boolean remove(Object o, Object o1) {
		boolean remove = super.remove(o, o1);
		update();
		return remove;
	}

	@Override
	public LabelEditorColor get(Object o) {
		return computeIfAbsent(o, k -> new DefaultLabelEditorColor(this, 0));
	}

}
