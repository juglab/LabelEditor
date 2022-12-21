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
package sc.fiji.labeleditor.plugin.renderers;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.colors.LabelEditorColor;
import sc.fiji.labeleditor.core.model.colors.LabelEditorColorset;
import sc.fiji.labeleditor.core.model.colors.LabelEditorTagColors;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTagging;
import sc.fiji.labeleditor.core.view.LabelEditorOverlayRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorTargetComponent;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

public abstract class AbstractLabelEditorRenderer<L> implements LabelEditorOverlayRenderer<L> {

	private int[] lut;
	private boolean debug = false;
	private boolean active = true;
	private LabelEditorModel<L> model;
	private RandomAccessibleInterval<? extends IntegerType<?>> screenImg;

	@Override
	public void init(LabelEditorModel<L> model, RandomAccessibleInterval<? extends ARGBType> screenImg) {
		init(model);
		updateScreenImage(Converters.convert(screenImg, (input, output) -> output.set(input.get()), new IntType()));
	}

	protected void init(LabelEditorModel<L> model) {
		this.model = model;
		lut = new int[model.labeling().getMapping().numSets()];
		updateOnTagChange();
	}

	@Override
	public LabelEditorModel<L> model() {
		return model;
	}

	protected synchronized <I extends IntegerType<I>> void updateScreenImage(RandomAccessibleInterval<I> screenImage) {
		this.screenImg = screenImage;
	}

	@Override
	public synchronized void updateOnTagChange() {
		updateLUT(LabelEditorTargetComponent.FACE);
	}

	protected synchronized void updateLUT(LabelEditorTargetComponent targetComponent) {

		if(model == null) return;

		LabelEditorTagColors tagColors = model.colors();
		LabelingMapping<L> mapping = model.labeling().getMapping();

		Arrays.fill(lut, 0);

		if(tagColors == null) return;

		for (int i = 0; i < lut.length; i++) {

			Set<L> labels = mapping.labelsAtIndex(i);

			if(labels.size() == 0) continue;

			lut[i] = getMixColor(tagColors, targetComponent, labels);

		}

		if(debug) {
			printLUT(targetComponent, mapping, lut);
		}
	}

	protected int getMixColor(LabelEditorTagColors tagColors, Object targetComponent, Set<L> labels) {
		if(labels.size() > 1) {
			Stream<Integer> labelColors = labels.stream().sorted(model.getLabelComparator()).map(label -> getLabelColor(tagColors, targetComponent, label));
			return ColorMixingUtils.mixColorsOverlay(labelColors);
		} else {
			return getLabelColor(tagColors, targetComponent, labels.iterator().next());
		}
	}

	private int getLabelColor(LabelEditorTagColors tagColors, Object targetComponent, L label) {
		Stream<Object> labelTags = Stream.concat(model.tagging().getTags(label).stream(), Stream.of(LabelEditorTag.DEFAULT));
		return mixColorsOverlay(label, labelTags, tagColors, targetComponent, model.tagging());
	}

	private void printLUT(LabelEditorTargetComponent target, LabelingMapping<L> mapping, int[] lut) {
		StringBuilder str = new StringBuilder();
		str.append("target: ");
		str.append(target);
		for (int i = 0; i < lut.length; i++) {
			str.append("{");
			mapping.labelsAtIndex(i).forEach(label -> str.append(label).append(" "));
			str.append("} : rgba(");
			str.append(ARGBType.red(lut[i]));
			str.append(", ");
			str.append(ARGBType.green(lut[i]));
			str.append(", ");
			str.append(ARGBType.blue(lut[i]));
			str.append(", ");
			str.append(ARGBType.alpha(lut[i]));
			str.append(")\n");
		}
		System.out.println(str.toString());
	}

	@Override
	public void updateOnLabelingChange() {
	}

	@Override
	public <T extends RandomAccessible<? extends ARGBType>> T getOutput() {
		int[] lut = getLUT();
		return (T) convert(lut, (RandomAccessibleInterval)screenImg);
	}

	private static <I extends IntegerType<I>> RandomAccessibleInterval<ARGBType> convert(int[] lut, RandomAccessibleInterval<I> screenImg) {
		Converter<I, ARGBType> converter = (i, o) -> {
			try {
				o.set(lut[i.getInteger()]);
			} catch(ArrayIndexOutOfBoundsException e) {
				//FIXME I cannot figure out why this is happening. to reproduce, remove this catch and run the AddToExistingBDV example.
			}
		};
		return Converters.convert(screenImg, converter, new ARGBType());
	}

	@Override
	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	protected int[] getLUT() {
		return lut;
	}

	public static <L> int mixColorsAdditive(L label, Stream<Object> tags, LabelEditorTagColors tagColors, Object targetComponent, LabelEditorTagging tagging) {

		Stream<Integer> colors = getTagColors(label, tags, tagColors, targetComponent, tagging);
		return ColorMixingUtils.mixColorsAdditive(colors);
	}

	public static <L> int mixColorsOverlay(L label, Stream<Object> tags, LabelEditorTagColors tagColors, Object targetComponent, LabelEditorTagging tagging) {

		Stream<Integer> colors = getTagColors(label, tags, tagColors, targetComponent, tagging);
		return ColorMixingUtils.mixColorsOverlay(colors);
	}

	protected static <L> Stream<Integer> getTagColors(L label, Stream< Object > tags,
													LabelEditorTagColors tagColors, Object targetComponent, LabelEditorTagging tagging)
	{
		return tags.map(tag -> {
			LabelEditorColorset colorset = tagColors.getColorset(tag);
			if(colorset == null) return 0;
			LabelEditorColor leColor = colorset.get(targetComponent);
			if(leColor == null) return 0;
			Object value = tagging.getValue(tag, label);
			return leColor.get(value);
		});
	}

	void printLUT() {
		printLUT(null, model.labeling().getMapping(), lut);
	}
}
