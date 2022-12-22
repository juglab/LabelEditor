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
package sc.fiji.labeleditor.plugin.behaviours.modification;

import net.imagej.ops.OpService;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.plugin.Parameter;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.view.LabelEditorTargetComponent;

import java.util.Random;
import java.util.Set;

public class TagByProperty<L> {

	@Parameter
	OpService ops;

	private final InteractiveLabeling<L> labeling;

	public TagByProperty(InteractiveLabeling<L> labeling) {
		this.labeling = labeling;
	}

	public void circularity() {
		Set<L> labels = labeling.getLabelSetInScope();
		Random random = new Random();
		labels.forEach(label -> {
			labeling.model().tagging().addValueToLabel("circularity", new IntType(random.nextInt(100)), label);
			labeling.model().colors().getColorset("circularity").put(
					LabelEditorTargetComponent.FACE,
					ARGBType.rgba(0,0,255,250),
					ARGBType.rgba(255,0,0,250),
					new IntType(0), new IntType(100)
			);
		});
		//TODO
	}
}
