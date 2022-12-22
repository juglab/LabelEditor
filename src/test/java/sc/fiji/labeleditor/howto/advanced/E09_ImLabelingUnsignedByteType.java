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
package sc.fiji.labeleditor.howto.advanced;

import net.imagej.ImageJ;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;

/**
 * How to use label editor with ImgLabeling<..., UnsignedByteType>
 */
public class E09_ImLabelingUnsignedByteType {

	public static void main(String... args) {
		ImageJ ij = new ImageJ();
		Img< UnsignedByteType > indexImage = ArrayImgs.unsignedBytes(2, 2);
		ImgLabeling< String, UnsignedByteType > labeling = new ImgLabeling<>(indexImage);
		RandomAccess< LabelingType< String > > ra = labeling.randomAccess();
		ra.setPosition(new long[]{0,0});
		ra.get().add("A");
		ra.setPosition(new long[]{0,1});
		ra.get().add("A");
		ra.get().add("B");
		ra.setPosition(new long[]{1,1});
		ra.get().add("B");
		LabelEditorModel<String> model = new DefaultLabelEditorModel<>(labeling);
		model.tagging().addTagToLabel("A", "A");
		model.tagging().addTagToLabel("B", "B");
		model.colors().getFaceColor("A").set(255, 0, 0);
		model.colors().getFaceColor("B").set(0, 255, 0);
		ij.ui().show(model);
	}
}
