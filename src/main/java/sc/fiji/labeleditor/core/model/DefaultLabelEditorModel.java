/*-
 * #%L
 * UI component for image segmentation label comparison and selection
 * %%
 * Copyright (C) 2019 - 2020 DAIS developers
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package sc.fiji.labeleditor.core.model;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.img.Img;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import org.scijava.listeners.Listeners;
import sc.fiji.labeleditor.core.model.colors.DefaultLabelEditorTagColors;
import sc.fiji.labeleditor.core.model.colors.LabelEditorTagColors;
import sc.fiji.labeleditor.core.model.tagging.DefaultLabelEditorTagging;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTagging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultLabelEditorModel<L> implements LabelEditorModel<L> {

	private ImgLabeling<L, ? extends IntegerType<?> > labels;
	private RandomAccessibleInterval<? extends NumericType<?>> data;
	private LabelEditorTagging<L> tagging;
	private Comparator<L> labelComparator;

	private List<Object> orderedTags = new ArrayList<>();

	private final LabelEditorTagColors tagColors = new DefaultLabelEditorTagColors();
	private String name;
	private Listeners.List<LabelingChangeListener> listeners = new Listeners.SynchronizedList<>();
	private boolean labelingListenersPaused = false;

	public DefaultLabelEditorModel(ImgLabeling<L, ? extends IntegerType<?>> labeling, RandomAccessibleInterval<? extends NumericType<?>> data) {
		this(labeling);
		this.data = data;
	}

	public DefaultLabelEditorModel(ImgLabeling<L, ? extends IntegerType<?>> labeling) {
		if(labeling != null) {
			setName("model " + System.identityHashCode(this));
			this.labels = labeling;
			initLabelOrdering(labeling);
			initTagging();
			addDefaultColorsets();
		}
	}
	public static DefaultLabelEditorModel<IntType> initFromLabelMap(RandomAccessibleInterval<? extends IntegerType<?>> labelMap) {
		return new DefaultLabelEditorModel<>(makeLabeling(labelMap));
	}

	public static DefaultLabelEditorModel<IntType> initFromLabelMap(RandomAccessibleInterval<? extends IntegerType<?>> labelMap, RandomAccessibleInterval<? extends NumericType<?>> data) {
		return new DefaultLabelEditorModel<>(makeLabeling(labelMap), data);
	}

	private static ImgLabeling<IntType, IntType> makeLabeling(RandomAccessibleInterval<? extends IntegerType<?>> labelMap) {
		Img<IntType> backing = new DiskCachedCellImgFactory<>(new IntType()).create(labelMap);
		final ImgLabeling< IntType, IntType > labeling = new ImgLabeling<>( backing );
		AtomicInteger max = new AtomicInteger(0);
		LoopBuilder.setImages(labelMap, backing).multiThreaded().forEachPixel((input, output) -> {
			int intInput = input.getInteger();
			if(intInput > max.get()) max.getAndSet(intInput);
			output.set(intInput);
		});
		final ArrayList<Set<IntType>> labelSets = new ArrayList<>();

		labelSets.add( new HashSet<>() ); // empty 0 label
		for (int label = 1; label <= max.get(); ++label) {
			final HashSet< IntType > set = new HashSet< >();
			set.add( new IntType(label) );
			labelSets.add( set );
		}

		new LabelingMapping.SerialisationAccess<IntType>(labeling.getMapping()) {
			{
				super.setLabelSets(labelSets);
			}
		};

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
	private void initTagging() {
		tagging = new DefaultLabelEditorTagging<>(this);
		tagging.addTag(LabelEditorTag.MOUSE_OVER);
		tagging.addTag(LabelEditorTag.FOCUS);
		tagging.addTag(LabelEditorTag.SELECTED);
	}

	private void initLabelOrdering(ImgLabeling<L, ? extends IntegerType<?>> labeling) {
		labelComparator = this::compareLabels;
	}

	int compareLabels(L label1, L label2) {
		return label1.toString().compareTo(label2.toString());
	}

	@Override
	public LabelEditorTagging<L> tagging() {
		return tagging;
	}

	public LabelEditorTagColors colors() {
		return tagColors;
	}

	public void setLabelComparator(Comparator<L> comparator) {
		this.labelComparator = comparator;
	}

	@Override
	public Comparator<L> getLabelComparator() {
		return labelComparator;
	}

	@Override
	public RandomAccessibleInterval<? extends NumericType<?>> getData() {
		return data;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Listeners<LabelingChangeListener> labelingListeners() {
		return listeners;
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

	@Override
	public void pauseLabelingListeners() {
		labelingListenersPaused = true;
	}

	@Override
	public void resumeLabelingListeners() {
		labelingListenersPaused = false;
	}

	@Override
	public void notifyLabelingListeners() {
		LabelingChangedEvent e = new LabelingChangedEvent();
		listeners.list.forEach(listener -> listener.labelingChanged(e));
	}

}
