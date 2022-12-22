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
package sc.fiji.labeleditor.plugin.interfaces.bdv;

import bdv.util.BdvOverlay;
import net.imglib2.type.numeric.ARGBType;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class LabelEditorOverlay extends BdvOverlay
{
	class ModelOverlay {
		String name;
		LabelEditorModel model;
		List<LabelOverlay> labels = new ArrayList<>();
	}

	class LabelOverlay {
		String name;
		List<String> tags = new ArrayList<>();
	}

	List<ModelOverlay> data = new ArrayList<>();

	@Override
	public synchronized void draw( final Graphics2D g )
	{

		g.setColor(Color.white);
		g.setFont( new Font( "Monospaced", Font.PLAIN, 12 ) );
		int x = (int) (g.getClipBounds().getWidth()-13);
		int y = 50;
		int height = 15;
		for (ModelOverlay model : data) {
			drawWithBox(g, x, y, model.name, new Color(0xdddddd), new Color(0x000000));
			y += height*1.5;
			for (LabelOverlay label : model.labels) {
				drawWithBox(g, x, y, label.name, new Color(0xaaaaaa), new Color(0x000000));
				y+= height;
				for (String tag : label.tags) {
					drawWithBox(g, x, y, tag, new Color(0x000000), new Color(0xffffff));
					y+= height;
				}
				y += height*0.3;
			}
			y += height*0.5;
		}
	}

	private void drawWithBox(Graphics2D g, int x, int y, String text, Color bg, Color fg) {
		int width = g.getFontMetrics().stringWidth(text);
		x = x - width;
		int padding = 2;
		int height = 11;
		g.setColor(bg);
		g.fillRect(x-padding, y-height-padding+1, width+padding*2, height+padding*2);
		g.setColor(fg);
		g.drawString(text, x, y);
		g.setColor(Color.white);
	}

	private void draw(Graphics2D g, int x, int y, String text) {
		x = x - g.getFontMetrics().stringWidth(text);
		g.drawString(text, x, y);
	}

	public synchronized void removeContent(final LabelEditorModel model) {
		for (ModelOverlay overlay : data) {
			if (overlay.model.equals(model)) {
				data.remove(overlay);
				break;
			}
		}
	}

	/**
	 * Update data to show in the overlay.
	 */
	public synchronized void updateContent(final Set<LabelEditorModel> models)
	{
		synchronized ( models )
		{
			List<LabelEditorTag> labelEditorTags = Arrays.asList(LabelEditorTag.values());
			for (LabelEditorModel model : models) {
				for (ModelOverlay overlay : data) {
					if (overlay.model.equals(model)) {
						data.remove(overlay);
						break;
					}
				}
				ModelOverlay modelOverlay = new ModelOverlay();
				modelOverlay.name = model.getName();
				modelOverlay.model = model;
				for (Object label : model.labeling().getMapping().getLabels()) {
					LabelOverlay labelOverlay = new LabelOverlay();
					labelOverlay.name = "label " + label.toString();
					boolean selected = false;
					for (Object tag : model.tagging().getTags(label)) {
						if(tag.equals(LabelEditorTag.SELECTED)) selected = true;
						if(labelEditorTags.contains(tag)) continue;
						Object value = model.tagging().getValue(tag, label);
						if(value != null) {
							if(value.equals(true)) {
								labelOverlay.tags.add(tag.toString());
							} else {
								labelOverlay.tags.add(tag.toString() + ": " + value);
							}
						}
					}
					if(selected) modelOverlay.labels.add(labelOverlay);
				}
				if(modelOverlay.labels.size() > 0) data.add(modelOverlay);
			}
		}
	}

}
