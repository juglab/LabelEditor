/*-
 * #%L
 * UI component for image segmentation label comparison and selection
 * %%
 * Copyright (C) 2019 - 2022 Deborah Schmidt
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
package sc.fiji.labeleditor.plugin.mode.timeslice;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;

public class TimeSliceLabelEditorModel<L> extends DefaultLabelEditorModel<L> {

	private final int timeDimension;

	public TimeSliceLabelEditorModel(ImgLabeling<L, IntType> labeling, int timeDimension) {
		super(labeling);
		this.timeDimension = timeDimension;
	}

	public TimeSliceLabelEditorModel(ImgLabeling<L, IntType> labeling, RandomAccessibleInterval data, int timeDimension) {
		super(labeling, data);
		this.timeDimension = timeDimension;
	}

	public int getTimeDimension() {
		return timeDimension;
	}

	public IntervalView<LabelingType<L>> getLabelingAtTime(long currentTimePoint) {
		return Views.hyperSlice(labeling(), getTimeDimension(), currentTimePoint);
	}
}
