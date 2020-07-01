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
import org.scijava.listeners.Listeners;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import sc.fiji.labeleditor.core.view.LabelEditorTargetComponent;

public interface LabelEditorTagColors {
	LabelEditorColorset getColorset(Object tag);

	Listeners<ColorChangeListener> listeners();

	void pauseListeners();

	void resumeListeners();

	void notifyListeners();

	LabelEditorColor getFaceColor(Object tag);

	LabelEditorColor getBorderColor(Object tag);

	default LabelEditorColor getFocusFaceColor() {
		return getFaceColor(LabelEditorTag.MOUSE_OVER);
	}

	default LabelEditorColor getSelectedFaceColor() {
		return getFaceColor(LabelEditorTag.SELECTED);
	}

	default LabelEditorColor getDefaultFaceColor() {
		return getFaceColor(LabelEditorTag.DEFAULT);
	}

	default LabelEditorColor getFocusBorderColor() {
		return getBorderColor(LabelEditorTag.MOUSE_OVER);
	}

	default LabelEditorColor getSelectedBorderColor() {
		return getBorderColor(LabelEditorTag.SELECTED);
	}

	default LabelEditorColor getDefaultBorderColor() {
		return getBorderColor(LabelEditorTag.DEFAULT);
	}

	default <T extends RealType<T>> LabelEditorValueColor<T> makeValueBorderColor(Object tag) {
		LabelEditorColorset colorset = getColorset(tag);
		LabelEditorValueColor<T> color = new LabelEditorValueColor<>(colorset);
		colorset.put(LabelEditorTargetComponent.BORDER, color);
		return color;
	}

	default <T extends RealType<T>> LabelEditorValueColor<T> makeValueFaceColor(Object tag, T minVal, T maxVal) {
		LabelEditorColorset colorset = getColorset(tag);
		LabelEditorValueColor<T> color = new LabelEditorValueColor<>(colorset, minVal, maxVal);
		colorset.put(LabelEditorTargetComponent.FACE, color);
		return color;

	}
}
