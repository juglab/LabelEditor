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

import net.imglib2.type.numeric.ARGBType;

import java.util.function.Function;

public class DefaultLabelEditorColor implements LabelEditorColor {
	private Function<Object, Integer> colorMapping;
	private final LabelEditorColorset colorset;

	public DefaultLabelEditorColor(LabelEditorColorset colorset) {
		this.colorset = colorset;
	}

	public DefaultLabelEditorColor(LabelEditorColorset colorset, int color) {
		this.colorset = colorset;
		this.colorMapping = v -> color;
	}

	@Override
	public int get() {
		return colorMapping.apply(true);
	}

	@Override
	public int get(Object value) {
		return colorMapping.apply(value);
	}

	@Override
	public void set(int color) {
		this.colorMapping = v -> color;
		update();
	}

	protected void update() {
		colorset.update();
	}

	@Override
	public void set(int red, int green, int blue, int alpha) {
		set(ARGBType.rgba(red, green, blue, alpha));
	}

	@Override
	public void set(int red, int green, int blue) {
		set(red, green, blue, 255);
	}
}
