package com.indago.labeleditor.core.model;

import com.indago.labeleditor.core.model.colors.LabelEditorColorset;
import com.indago.labeleditor.core.model.colors.LabelEditorTagColors;
import com.indago.labeleditor.core.model.tagging.DefaultLabelEditorTagging;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import com.indago.labeleditor.core.model.tagging.LabelEditorTagging;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AbstractLabelEditorModel<L> implements LabelEditorModel<L> {

	protected ImgLabeling<L, IntType > labels;
	private Img data;
	private Map<L, LabelRegion<L>> orderedLabels;
	protected LabelEditorTagging<L> tagLabelRelation;
	private Comparator<L> labelComparator;
	private Comparator<Object> tagComparator;

	private List<Object> orderedTags = new ArrayList<>();

	private final LabelEditorTagColors tagColors = new LabelEditorTagColors();

	@Override
	public ImgLabeling<L, IntType> labeling() {
		return labels;
	}

	@Override
	public void init(Img data) {
		this.data = data;
	}

	@Override
	public void init(ImgLabeling<L, IntType> labeling, Img data) {
		this.data = data;
		init(labeling);
	}

	@Override
	public void init(ImgLabeling<L, IntType> labeling) {
		if(labeling != null) {
			this.labels = labeling;
			initLabelOrdering(labeling);
			initTagOrdering();
			initTagging();
		}
	}

	@Override
	public void initFromIndexImage(Img labelMap) {
		init(makeLabeling(labelMap));
	}

	@Override
	public void initFromIndexImage(Img data, Img labelMap) {
		init(makeLabeling(labelMap), data);
	}

	private static <T extends IntegerType<T>> ImgLabeling<IntType, IntType> makeLabeling(Img<T> labelMap) {
		Img<IntType> backing = new ArrayImgFactory<>(new IntType()).create(labelMap);
		ImgLabeling<IntType, IntType> labeling = new ImgLabeling<>(backing);
		Cursor<T> cursor = labelMap.localizingCursor();
		RandomAccess<LabelingType<IntType>> ra = labeling.randomAccess();
		while(cursor.hasNext()) {
			int val = cursor.next().getInteger();
			if(val == 0) continue;
			ra.setPosition(cursor);
			ra.get().add(new IntType(val));
		}
		return labeling;
	}

	protected void initTagging() {
		tagLabelRelation = new DefaultLabelEditorTagging<L>();
	}

	protected void initLabelOrdering(ImgLabeling<L, IntType> labeling) {
		labelComparator = this::compareLabels;
	}

	protected void initTagOrdering() {
		tagComparator = this::compareTags;
		orderedTags.clear();
		orderedTags.add(LabelEditorTag.SELECTED);
		orderedTags.add(LabelEditorTag.FOCUS);
	}

	@Override
	public List<LabelEditorColorset> getVirtualChannels() {
		return new ArrayList<>(tagColors.values());
	}

	/**
	 * This is sorting labels by their tags. If a label has the more important tag,
	 * it should be displayed on top. Not sure the sorting works as intended.
	 */
	int compareLabels(L label1, L label2) {
		Set<Object> tags1 = tagging().getTags(label1);
		Set<Object> tags2 = tagging().getTags(label2);
		if(tags1.size() == 0 && tags2.size() == 0) return label1.toString().compareTo(label2.toString());
		if(tags1.size() == 0) return 1;
		if(tags2.size() == 0) return -1;
		Set<Object> tags1Copy = new HashSet<>(tags1);
		tags1Copy.addAll(tags2);
		List<Object> bothTags = new ArrayList<>(tags1Copy);
		bothTags.sort(getTagComparator());
		Object firstTag = bothTags.get(0);
		if(tags1.contains(firstTag) && !tags2.contains(firstTag)) return -1;
		if(!tags1.contains(firstTag) && tags2.contains(firstTag)) return 1;
		return label1.toString().compareTo(label2.toString());
	}

	int compareTags(Object tag1, Object tag2) {
		int tag1Index = orderedTags.indexOf(tag1);
		int tag2Index = orderedTags.indexOf(tag2);
		if(tag1Index < 0 && tag2Index < 0) {
			return tag1.toString().compareTo(tag2.toString());
		} else {
			if(tag1Index < tag2Index) return 1;
			return -1;
		}
	}

	@Override
	public LabelEditorTagging<L> tagging() {
		return tagLabelRelation;
	}

	public LabelEditorTagColors colors() {
		return tagColors;
	}

	@Override
	public void setTagComparator(Comparator<Object> comparator) {
		this.tagComparator = comparator;
	}

	@Override
	public void setLabelComparator(Comparator<L> comparator) {
		this.labelComparator = comparator;
	}

	@Override
	public Comparator<Object> getTagComparator() {
		return tagComparator;
	}

	@Override
	public Comparator<L> getLabelComparator() {
		return labelComparator;
	}

	@Override
	public Img getData() {
		return data;
	}

	@Override
	public void setData(Img data) {
		this.data = data;
	}

	public List<Object> getOrderedTags() {
		return orderedTags;
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		if(getData() == null) {
			res.append("\t.. no dataset");
		}else {
			res.append("\t.. dataset ")
					.append(Arrays.toString(Intervals.dimensionsAsIntArray(getData())))
					.append(" of type ").append(getData().firstElement().getClass().getName());
		}
		res.append("\n\t.. labeling ")
				.append(Arrays.toString(Intervals.dimensionsAsIntArray(labeling())))
				.append(" of type ").append(getLabelClass().getName());
		res.append("\n\t.. label sets: ").append(labeling().getMapping().numSets());
		res.append("\n\t.. labels: ").append(labeling().getMapping().getLabels().size());
		res.append("\n\t.. tags: ").append(tagging().getAllTags().size()).append("\n");
		return res.toString();
	}

	private Class<?> getLabelClass() {
		Iterator<L> iterator = labeling().getMapping().getLabels().iterator();
		return iterator.hasNext() ? iterator.next().getClass() : Object.class;
	}

}
