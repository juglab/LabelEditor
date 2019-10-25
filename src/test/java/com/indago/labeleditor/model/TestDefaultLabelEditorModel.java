package com.indago.labeleditor.model;

import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestDefaultLabelEditorModel {

	@Test
	public void testTagCreation() {
		ArrayImg<IntType, IntArray> backing = ArrayImgs.ints( 10, 10 );
		ImgLabeling< String, IntType > labels = new ImgLabeling<>( backing );
		String LABEL1 = "label1";
		String LABEL2 = "label2";

		DefaultLabelEditorModel<IntType, String> model = new DefaultLabelEditorModel<>(labels);

		assertNotNull(model.getTags());
		assertEquals(0, model.getTags(LABEL1).size());

		model.addTag(LABEL1, LabelEditorTag.VISIBLE);
		assertEquals(1, model.getTags(LABEL1).size());

		model.removeTag(LABEL1, LabelEditorTag.VISIBLE);
		assertEquals(0, model.getTags(LABEL1).size());

	}
}
