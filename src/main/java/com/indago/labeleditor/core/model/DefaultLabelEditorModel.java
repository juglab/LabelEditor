package com.indago.labeleditor.core.model;

import com.indago.labeleditor.core.model.colors.LabelEditorColorset;
import com.indago.labeleditor.core.model.colors.LabelEditorTagColors;
import com.indago.labeleditor.core.model.tagging.DefaultLabelEditorTagging;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import com.indago.labeleditor.core.model.tagging.LabelEditorTagging;
import com.indago.labeleditor.core.view.LabelEditorTargetComponent;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DefaultLabelEditorModel<L> implements LabelEditorModel<L> {

	private ImgLabeling<L, IntType > labels;
	private Img data;
	private Map<L, LabelRegion<L>> orderedLabels;
	private LabelEditorTagging<L> tagLabelRelation;
	private Comparator<L> labelComparator;
	private Comparator<Object> tagComparator;

	private List<Object> orderedTags = new ArrayList<>();

	private final LabelEditorTagColors tagColors = new LabelEditorTagColors();

	private static int faceMouseOver = ARGBType.rgba(200,200,200,200);
	private static int borderMouseOver = ARGBType.rgba(200,200,200,200);
	private static int faceSelected = ARGBType.rgba(0,100,255,200);
	private static int borderSelected = ARGBType.rgba(0,80,225,200);
	private static int faceDefault = ARGBType.rgba(255,255,255,100);
	private static int borderDefault = ARGBType.rgba(255,255,255,100);

	private int timeDimension = -1;

	public DefaultLabelEditorModel() {
		addDefaultColorsets();
	}

	protected void addDefaultColorsets() {
		tagColors.get(LabelEditorTag.DEFAULT).put(LabelEditorTargetComponent.FACE, faceDefault);
		tagColors.get(LabelEditorTag.DEFAULT).put(LabelEditorTargetComponent.BORDER, borderDefault);
		tagColors.get(LabelEditorTag.SELECTED).put(LabelEditorTargetComponent.FACE, faceSelected);
		tagColors.get(LabelEditorTag.SELECTED).put(LabelEditorTargetComponent.BORDER, borderSelected);
		tagColors.get(LabelEditorTag.MOUSE_OVER).put(LabelEditorTargetComponent.FACE, faceMouseOver);
		tagColors.get(LabelEditorTag.MOUSE_OVER).put(LabelEditorTargetComponent.BORDER, borderMouseOver);
	}

	@Override
	public ImgLabeling<L, IntType> labels() {
		return labels;
	}

	@Override
	public void init(ImgLabeling<L, IntType> labeling, Img data) {
		this.data = data;
		init(labeling);
	}

	@Override
	public void init(Img indexImg) {
		init(new ImgLabeling((indexImg)));
	}

	@Override
	public void init(Img indexImg, Img data) {
		init(new ImgLabeling(indexImg), data);
	}

	@Override
	public void init(ImgLabeling<L, IntType> labeling) {
		if(labeling != null) {
			this.labels = labeling;
			initLabelOrdering(labeling);
			initTagOrdering();
			tagLabelRelation = new DefaultLabelEditorTagging<L>();
		}
	}

	private void initLabelOrdering(ImgLabeling<L, IntType> labeling) {
		labelComparator = this::compareLabels;
//		LabelRegions<L> regions = new LabelRegions<>(labeling);
//		List<LabelRegion<L>> regionSet = new ArrayList<>();
//		labeling.forEach(labels -> labels.stream().map(regions::getLabelRegion).forEach(labelRegion -> {
//			if (!regionSet.contains(labelRegion)) regionSet.add(labelRegion);
//		}));
//		regionSet.sort((r1, r2) -> (int)(r1.size() - r2.size()));
//		orderedLabels = new LinkedHashMap<>();
//		regionSet.forEach(region -> orderedLabels.put(region.getLabel(), region));
	}

	void initTagOrdering() {
		tagComparator = this::compareTags;
		orderedTags.clear();
		orderedTags.add(LabelEditorTag.MOUSE_OVER);
		orderedTags.add(LabelEditorTag.SELECTED);
	}

	@Override
	public List<LabelEditorColorset> getVirtualChannels() {
		return new ArrayList<>(tagColors.values());
	}

	private int compareLabels(L label1, L label2) {
		boolean label1Selected = tagging().getTags(label1).contains(LabelEditorTag.SELECTED);
		boolean label2Selected = tagging().getTags(label2).contains(LabelEditorTag.SELECTED);
		if(label1Selected && !label2Selected) return 1;
		if(!label1Selected && label2Selected) return -1;
		else return 0;
//		for (Map.Entry<L, LabelRegion<L>> entry : orderedLabels.entrySet()) {
//			if(entry.getKey().equals(label1)) return 1;
//			if(entry.getKey().equals(label2)) return -1;
//		}
//		return 0;
	}

	int compareTags(Object tag1, Object tag2) {
		int tag1Index = orderedTags.indexOf(tag1);
		int tag2Index = orderedTags.indexOf(tag2);
		if(tag1Index < 0 && tag2Index < 0) {
			return tag1.toString().compareTo(tag2.toString());
		} else {
			if(tag1Index < tag2Index) return -1;
			return 1;
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

	@Override
	public int getTimeDimension() {
		return timeDimension;
	}

	@Override
	public void setTimeDimension(int dimension) {
		timeDimension = dimension;
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
				.append(Arrays.toString(Intervals.dimensionsAsIntArray(labels())))
				.append(" of type ").append(getLabelClass().getName());
		res.append("\n\t.. label sets: ").append(labels().getMapping().numSets());
		res.append("\n\t.. labels: ").append(labels().getMapping().getLabels().size());
		res.append("\n\t.. tags: ").append(tagging().getAllTags().size()).append("\n");
		return res.toString();
	}

	private Class<?> getLabelClass() {
		Iterator<L> iterator = labels().getMapping().getLabels().iterator();
		return iterator.hasNext() ? iterator.next().getClass() : Object.class;
	}

}
