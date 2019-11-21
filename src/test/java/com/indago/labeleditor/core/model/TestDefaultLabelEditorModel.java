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

		LabelEditorModel<String> model = new DefaultLabelEditorModel<>();
		model.init(labels);

		assertNotNull(model.tagging());
		assertEquals(0, model.tagging().getTags(LABEL1).size());

		model.tagging().addTagToLabel(LabelEditorTag.SELECTED, LABEL1);
		assertEquals(1, model.tagging().getTags(LABEL1).size());

		model.tagging().removeTagFromLabel(LabelEditorTag.SELECTED, LABEL1);
		assertEquals(0, model.tagging().getTags(LABEL1).size());

		model.tagging().addTagToLabel(LabelEditorTag.SELECTED, LABEL1);
		model.tagging().addTagToLabel(LabelEditorTag.FOCUS, LABEL1);

	}

	@Test
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

		DefaultLabelEditorModel<String> model = new DefaultLabelEditorModel<>();
		model.init(labels);

		model.tagging().addTagToLabel(LabelEditorTag.SELECTED, LABEL1);
		model.tagging().addTagToLabel(LabelEditorTag.FOCUS, LABEL2);
		model.tagging().addTagToLabel("mytag", LABEL3);

		List<String> sortedLabels = new ArrayList<>(model.labeling().getMapping().getLabels());
		sortedLabels.sort(model::compareLabels);

		System.out.println(sortedLabels);

		assertEquals(LABEL2, sortedLabels.get(0));
		assertEquals(LABEL1, sortedLabels.get(1));
		assertEquals(LABEL3, sortedLabels.get(2));
		assertEquals(LABEL4, sortedLabels.get(3));

	}

	@Test
	public void testDefaultTagComparator() {
		DefaultLabelEditorModel<String> model = new DefaultLabelEditorModel<>();
		model.initTagOrdering();
		Set<Object> tags = new HashSet<>();
		tags.add(LabelEditorTag.FOCUS);
		tags.add(LabelEditorTag.SELECTED);
		tags.add("a");
		tags.add("b");
		List<Object> sortedTags = new ArrayList<>(tags);
		sortedTags.sort(model::compareTags);
		System.out.println(sortedTags);
		assertEquals(LabelEditorTag.FOCUS, sortedTags.get(0));
		assertEquals(LabelEditorTag.SELECTED, sortedTags.get(1));
		assertEquals("a", sortedTags.get(2));
		assertEquals("b", sortedTags.get(3));

	}

}
