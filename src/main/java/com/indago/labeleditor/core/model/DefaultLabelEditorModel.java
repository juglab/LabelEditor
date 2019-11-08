package com.indago.labeleditor.core.model;

import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.integer.IntType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultLabelEditorModel<L> implements LabelEditorModel<L> {

	private ImgLabeling<L, IntType > labels;
	private Map<L, LabelRegion<L>> orderedLabels;
	private TagLabelRelation<L> tagLabelRelation;
	private Comparator<L> labelComparator;
	private Comparator<Object> tagComparator;

	private List<Object> orderedTags = new ArrayList<>();

	public DefaultLabelEditorModel() {
		init(null);
	}

	public DefaultLabelEditorModel(ImgLabeling<L, IntType> labels) {
		init(labels);
	}

	@Override
	public ImgLabeling<L, IntType> labels() {
		return labels;
	}

	@Override
	public void init(ImgLabeling<L, IntType> labeling) {
		if(labeling != null) {
			this.labels = labeling;
			createOrderedLabels(labeling);
			tagLabelRelation = new DefaultTagLabelRelation<L>(new TagChangeListenerManager());
			labelComparator = this::compareLabels;
			tagComparator = this::compareTags;
			orderedTags.add(LabelEditorTag.MOUSE_OVER);
			orderedTags.add(LabelEditorTag.SELECTED);
		}

	}

	private void createOrderedLabels(ImgLabeling<L, IntType> labeling) {
		LabelRegions<L> regions = new LabelRegions<>(labeling);
		List<LabelRegion<L>> regionSet = new ArrayList<>();
		labeling.forEach(labels -> labels.stream().map(regions::getLabelRegion).forEach(labelRegion -> {
			if (!regionSet.contains(labelRegion)) regionSet.add(labelRegion);
		}));
		//TODO check if this is correct - sort by region size?! probably does not work this way
		regionSet.sort((r1, r2) -> (int)(r1.size() - r2.size()));
		orderedLabels = new LinkedHashMap<>();
		regionSet.forEach(region -> orderedLabels.put(region.getLabel(), region));
	}

	private int compareLabels(L label1, L label2) {
		for (Map.Entry<L, LabelRegion<L>> entry : orderedLabels.entrySet()) {
			if(entry.getKey().equals(label1)) return 1;
			if(entry.getKey().equals(label2)) return -1;
		}
		return 0;
	}

	private int compareTags(Object tag1, Object tag2) {
		int tag1Index = orderedTags.indexOf(tag1);
		int tag2Index = orderedTags.indexOf(tag2);
		if(tag1Index < 0 && tag2Index < 0) {
			return tag1.toString().compareTo(tag2.toString());
		}
		return tag1Index - tag2Index;
	}

	@Override
	public TagLabelRelation<L> tagging() {
		return tagLabelRelation;
	}

	@Override
	public TagChangeListenerManager listener() {
		return null;
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

	public Map<L, LabelRegion <L> > labelRegions() {
		return orderedLabels;
	}

}
