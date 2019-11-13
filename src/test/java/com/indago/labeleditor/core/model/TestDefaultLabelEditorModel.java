package com.indago.labeleditor.core.model;

import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
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

		DefaultLabelEditorModel<String> model = new DefaultLabelEditorModel<>(labels);

		assertNotNull(model.tagging());
		assertEquals(0, model.tagging().getTags(LABEL1).size());

		model.tagging().addTag(LabelEditorTag.SELECTED, LABEL1);
		assertEquals(1, model.tagging().getTags(LABEL1).size());

		model.tagging().removeTag(LabelEditorTag.SELECTED, LABEL1);
		assertEquals(0, model.tagging().getTags(LABEL1).size());

	}

	@Test
	public void testDefaultTagComparator() {
		DefaultLabelEditorModel<String> model = new DefaultLabelEditorModel<>();
		model.initTagOrdering();
		Set<Object> tags = new HashSet<>();
		tags.add(LabelEditorTag.SELECTED);
		tags.add(LabelEditorTag.MOUSE_OVER);
		tags.add("a");
		tags.add("b");
		List<Object> sortedTags = new ArrayList<>(tags);
		sortedTags.sort(model::compareTags);
		System.out.println(sortedTags);
		assertEquals("a", sortedTags.get(0));
		assertEquals("b", sortedTags.get(1));
		assertEquals(LabelEditorTag.MOUSE_OVER, sortedTags.get(2));
		assertEquals(LabelEditorTag.SELECTED, sortedTags.get(3));

	}

}
