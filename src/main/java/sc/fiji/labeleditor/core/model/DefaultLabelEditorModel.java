package sc.fiji.labeleditor.core.model;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.table.interactive.SelectionModel;
import sc.fiji.labeleditor.core.model.colors.DefaultLabelEditorTagColors;
import sc.fiji.labeleditor.core.model.colors.LabelEditorTagColors;
import sc.fiji.labeleditor.core.model.tagging.DefaultLabelEditorTagging;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTagging;
import sc.fiji.labeleditor.plugin.behaviours.select.SelectionBehaviours;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DefaultLabelEditorModel<L> implements LabelEditorModel<L> {

	private ImgLabeling<L, ? extends IntegerType<?> > labels;
	private RandomAccessibleInterval<?> data;
	private LabelEditorTagging<L> tagging;
	private Comparator<L> labelComparator;
	private Comparator<Object> tagComparator;

	private SelectionModel<L> selectionModel;

	private List<Object> orderedTags = new ArrayList<>();

	private final LabelEditorTagColors tagColors = new DefaultLabelEditorTagColors();
	private String name;

	public DefaultLabelEditorModel(ImgLabeling<L, ? extends IntegerType<?>> labeling, RandomAccessibleInterval<?> data) {
		this(labeling);
		this.data = data;
	}

	public DefaultLabelEditorModel(ImgLabeling<L, ? extends IntegerType<?>> labeling) {
		if(labeling != null) {
			setName("model " + System.identityHashCode(this));
			this.labels = labeling;
			initLabelOrdering(labeling);
			initTagOrdering();
			initTagging();
			initSelectionModel();
			addDefaultColorsets();
		}
	}

	private void initSelectionModel() {
		selectionModel = new SelectionBehaviours<>();
	}

	public static DefaultLabelEditorModel<IntType> initFromLabelMap(RandomAccessibleInterval<? extends IntegerType<?>> labelMap) {
		return new DefaultLabelEditorModel<>(makeLabeling(labelMap));
	}

	public static DefaultLabelEditorModel<IntType> initFromLabelMap(RandomAccessibleInterval<? extends IntegerType<?>> labelMap, RandomAccessibleInterval<?> data) {
		return new DefaultLabelEditorModel<>(makeLabeling(labelMap), data);
	}

	private static ImgLabeling<IntType, IntType> makeLabeling(RandomAccessibleInterval<? extends IntegerType<?>> labelMap) {
		Img<IntType> backing = new ArrayImgFactory<>(new IntType()).create(labelMap);
		ImgLabeling<IntType, IntType> labeling = new ImgLabeling<>(backing);
		Cursor<? extends IntegerType<?>> cursor = Views.iterable(labelMap).localizingCursor();
		RandomAccess<LabelingType<IntType>> ra = labeling.randomAccess();
		while(cursor.hasNext()) {
			int val = cursor.next().getInteger();
			if(val == 0) continue;
			ra.setPosition(cursor);
			ra.get().add(new IntType(val));
		}
		return labeling;
	}

	protected void addDefaultColorsets() {
		colors().getDefaultFaceColor().set(DefaultColors.defaultFace());
		colors().getDefaultBorderColor().set(DefaultColors.defaultBorder());
		colors().getSelectedFaceColor().set(DefaultColors.selectedFace());
		colors().getSelectedBorderColor().set(DefaultColors.selectedBorder());
		colors().getFocusFaceColor().set(DefaultColors.focusFace());
		colors().getFocusBorderColor().set(DefaultColors.focusBorder());
	}

	@Override
	public ImgLabeling<L, ? extends IntegerType<?>> labeling() {
		return labels;
	}

	// TODO: Consider using setters instead of protected methods.
	protected void initTagging() {
		tagging = new DefaultLabelEditorTagging<>();
	}

	protected void initLabelOrdering(ImgLabeling<L, ? extends IntegerType<?>> labeling) {
		labelComparator = this::compareLabels;
	}

	protected void initTagOrdering() {
		tagComparator = this::compareTags;
		orderedTags.clear();
		orderedTags.add(LabelEditorTag.SELECTED);
		orderedTags.add(LabelEditorTag.MOUSE_OVER);
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
			return Integer.compare(tag2Index, tag1Index);
		}
	}

	@Override
	public LabelEditorTagging<L> tagging() {
		return tagging;
	}

	public LabelEditorTagColors colors() {
		return tagColors;
	}

	public void setTagComparator(Comparator<Object> comparator) {
		this.tagComparator = comparator;
	}

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
	public RandomAccessibleInterval<?> getData() {
		return data;
	}

	@Override
	public SelectionModel<L> getSelectionModel() {
		return selectionModel;
	}

	@Override
	public void setSelectionModel(SelectionModel<L> model) {
		this.selectionModel = model;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	protected void setData(RandomAccessibleInterval data) {
		this.data = data;
	}

	public List<Object> getOrderedTags() {
		return orderedTags;
	}

	@Override
	public String toString() {
		if(name == null) return getInfo();
		return name;
	}

	public String getInfo() {
		StringBuilder res = new StringBuilder();
		res.append("\t.. of type ").append(getClass().getName());
		if(getData() == null) {
			res.append("\n\t.. no dataset");
		}else {
			res.append("\n\t.. dataset ")
					.append(Arrays.toString(Intervals.dimensionsAsIntArray(getData())))
					.append(" of type ").append(getData().randomAccess().get().getClass().getName());
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
