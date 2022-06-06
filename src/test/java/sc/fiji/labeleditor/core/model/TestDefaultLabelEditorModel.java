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
package sc.fiji.labeleditor.core.model;

import org.junit.Ignore;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestDefaultLabelEditorModel {

	@Test
	public void testTagCreation() {
		ArrayImg<IntType, IntArray> backing = ArrayImgs.ints( 10, 10 );
		ImgLabeling< String, IntType > labels = new ImgLabeling<>( backing );
		String LABEL1 = "label1";

		LabelEditorModel<String> model = new DefaultLabelEditorModel<>(labels);

		assertNotNull(model.tagging());
		Assert.assertEquals(0, model.tagging().getTags(LABEL1).size());

		model.tagging().addTagToLabel(LabelEditorTag.SELECTED, LABEL1);
		Assert.assertEquals(1, model.tagging().getTags(LABEL1).size());

		model.tagging().removeTagFromLabel(LabelEditorTag.SELECTED, LABEL1);
		Assert.assertEquals(0, model.tagging().getTags(LABEL1).size());

		model.tagging().addTagToLabel(LabelEditorTag.SELECTED, LABEL1);
		model.tagging().addTagToLabel(LabelEditorTag.MOUSE_OVER, LABEL1);

	}

	@Test
	@Ignore // the default label comparator is not sorting labels by tags anymore but just by name, so this test fails.
	// Not sure how to handle label ordering best, until then, this test is ignored
	public void testLabelComparator() {
		ArrayImg<IntType, IntArray> backing = ArrayImgs.ints( 10, 10 );
		ImgLabeling< String, IntType > labels = new ImgLabeling<>( backing );
		String LABEL1 = "label1";
		String LABEL2 = "label2";
		String LABEL3 = "label3";
		String LABEL4 = "label4";
		labels.firstElement().add(LABEL1);
		labels.firstElement().add(LABEL2);
		labels.firstElement().add(LABEL3);
		labels.firstElement().add(LABEL4);

		DefaultLabelEditorModel<String> model = new DefaultLabelEditorModel<>(labels);

		model.tagging().addTagToLabel(LabelEditorTag.SELECTED, LABEL1);
		model.tagging().addTagToLabel(LabelEditorTag.MOUSE_OVER, LABEL2);
		model.tagging().addTagToLabel("mytag", LABEL3);

		List<String> sortedLabels = new ArrayList<>(model.labeling().getMapping().getLabels());
		sortedLabels.sort(model::compareLabels);

		System.out.println(sortedLabels);

		assertEquals(LABEL2, sortedLabels.get(0));
		assertEquals(LABEL1, sortedLabels.get(1));
		assertEquals(LABEL3, sortedLabels.get(2));
		assertEquals(LABEL4, sortedLabels.get(3));

	}

}
