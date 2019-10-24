package com.indago.labeleditor.model;

import net.imagej.ImgPlus;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
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
		ArrayImg<IntType, ?> img = new ArrayImgFactory<>(new IntType()).create(10, 10);
		ImgPlus<IntType> imgPlus = new ImgPlus<>(img);
		ArrayImg<IntType, IntArray> backing = ArrayImgs.ints( img.dimension(0), img.dimension(1) );
		ImgLabeling< String, IntType > labels = new ImgLabeling<>( backing );
		String LABEL1 = "label1";
		String LABEL2 = "label2";

		DefaultLabelEditorModel<IntType, String> model = new DefaultLabelEditorModel<>(imgPlus, labels);

		assertNotNull(model.getTags());
		assertEquals(0, model.getTags(LABEL1).size());

		model.addTag(LABEL1, LabelEditorTag.VISIBLE);
		assertEquals(1, model.getTags(LABEL1).size());

		model.removeTag(LABEL1, LabelEditorTag.VISIBLE);
		assertEquals(0, model.getTags(LABEL1).size());

	}
}
