package com.indago.labeleditor.core.model;

import com.indago.labeleditor.core.LabelEditorOptions;
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
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
	private static int colorMouseOver = ARGBType.rgba(200,200,200,200);
	private static int colorSelected = ARGBType.rgba(0,100,255,200);
	private static int colorDefault = ARGBType.rgba(255,255,255,100);

	private LabelEditorOptions options = new LabelEditorOptions();

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
			addDefaultColorsets();
		}
	}

	protected void addDefaultColorsets() {
		tagColors.get(LabelEditorTag.DEFAULT).put(LabelEditorTargetComponent.FACE, colorDefault);
		tagColors.get(LabelEditorTag.SELECTED).put(LabelEditorTargetComponent.FACE, colorSelected);
		tagColors.get(LabelEditorTag.MOUSE_OVER).put(LabelEditorTargetComponent.FACE, colorMouseOver);
		tagColors.get(LabelEditorTag.DEFAULT).put(LabelEditorTargetComponent.BORDER, colorDefault);
		tagColors.get(LabelEditorTag.SELECTED).put(LabelEditorTargetComponent.BORDER, colorSelected);
		tagColors.get(LabelEditorTag.MOUSE_OVER).put(LabelEditorTargetComponent.BORDER, colorMouseOver);
	}

	private void initLabelOrdering(ImgLabeling<L, IntType> labeling) {
		labelComparator = this::compareLabels;
		//TODO calculating the regions should not be done in the core, but in an addon. By default, the sorting does not need to make sense.
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
		return 0;
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
	public LabelEditorOptions options() {
		return options;
	}

//	public Map<L, LabelRegion <L> > labelRegions() {
//		return orderedLabels;
//	}


	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		if(getData() == null) {
			str.append("\t")
					.append(".. no dataset")
					.append("\n");
		}else {
			str.append("\t")
					.append(".. dataset ")
					.append(Arrays.toString(Intervals.dimensionsAsIntArray(getData())))
					.append(" ")
					.append(getData().firstElement().getClass())
					.append("\n");
		}
		str.append("\t")
				.append(".. labeling ")
				.append(labels())
				.append("\n");
		str.append("\t")
				.append(".. label sets: ")
				.append(labels().getMapping().numSets())
				.append("\n");
		str.append("\t").append(".. labels: ")
				.append(labels().getMapping().getLabels().size())
				.append("\n");
		str.append("\t").append(".. tags: ")
				.append(tagging().getAllTags().size())
				.append("\n");
		return str.toString();
	}
}
