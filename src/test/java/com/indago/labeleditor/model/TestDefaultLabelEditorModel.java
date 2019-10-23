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
		VisibleTag visibleTag = new VisibleTag();

		DefaultLabelEditorModel<IntType, String> model = new DefaultLabelEditorModel<>(imgPlus, labels);

		assertNotNull(model.tags);
		assertEquals(1, model.tags.size());
		assertNotNull(model.getTags(0));
		assertEquals(0, model.getTags(0, LABEL1).size());

		model.addTag(0, LABEL1, visibleTag);
		assertEquals(1, model.getTags(0, LABEL1).size());

		//FIXME does not work:
//		model.removeTag(0, LABEL1, new VisibleTag());
		model.removeTag(0, LABEL1, visibleTag);
		assertEquals(0, model.getTags(0, LABEL1).size());

	}
}
