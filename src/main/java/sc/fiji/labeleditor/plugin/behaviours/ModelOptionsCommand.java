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
package sc.fiji.labeleditor.plugin.behaviours;

import net.imglib2.type.numeric.ARGBType;
import org.scijava.command.Command;
import org.scijava.command.InteractiveCommand;
import org.scijava.module.DefaultMutableModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.ColorRGBA;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Plugin(type = Command.class, name = "LabelEditorModel options", initializer = "initRendererList")
public class ModelOptionsCommand extends InteractiveCommand {

	@Parameter
	private Set<InteractiveLabeling<?>> labelings;

	@Parameter
	private LabelEditorInterface labelEditorInterface;

	private Map<String, LabelEditorModel> nameItems;
	private Map<String, LabelEditorModel> faceColorItems;
	private Map<String, LabelEditorModel> borderColorItems;
	private Map<String, LabelEditorRenderer<?>> namedRenderers;
	private Map<String, LabelEditorView<?>> namedRendererViews;

	@Override
	public void run() {
		nameItems.forEach((item, model) -> {
			String name = (String) getInput(item);
			model.setName(name);
		});
		faceColorItems.forEach((item, model) -> {
			int currentColor = model.colors().getDefaultFaceColor().get();
			int newColor = getColor((ColorRGBA) getInput(item));
			if(currentColor != newColor) {
				model.colors().getDefaultFaceColor().set(newColor);
			}
		});
		borderColorItems.forEach((item, model) -> {
			int currentColor = model.colors().getDefaultBorderColor().get();
			int newColor = getColor((ColorRGBA) getInput(item));
			if(currentColor != newColor) {
				model.colors().getDefaultBorderColor().set(newColor);
			}
		});
		namedRenderers.forEach((name, renderer) -> {
			Boolean active = (Boolean) getInput(name);
			namedRendererViews.get(name).setActive(renderer, active);
		});
	}

	protected void initRendererList() {
		nameItems = new HashMap<>();
		faceColorItems = new HashMap<>();
		borderColorItems = new HashMap<>();
		namedRenderers = new HashMap<>();
		namedRendererViews = new HashMap<>();
		labelings.forEach(labeling -> {
			makeNameItem(labeling.model());
			makeFaceColorItem(labeling.model());
			makeBorderColorItem(labeling.model());
			makeRendererItems(labeling.view());
		});
	}

	private void makeNameItem(LabelEditorModel model) {
		final DefaultMutableModuleItem<String> item =
				new DefaultMutableModuleItem<>(this, model.getName() + " name", String.class);
		item.setPersisted(false);
		item.setLabel("Model name");
		item.setValue(this, model.getName());
		nameItems.put(item.getName(), model);
		addInput(item);
	}

	private void makeFaceColorItem(LabelEditorModel model) {
		final DefaultMutableModuleItem<ColorRGBA> item =
				new DefaultMutableModuleItem<>(this, model.getName() + " face color", ColorRGBA.class);
		item.setPersisted(false);
		item.setLabel("Default face color");
		item.setValue(this, getColor(model.colors().getDefaultFaceColor().get()));
		faceColorItems.put(item.getName(), model);
		addInput(item);
	}

	private void makeBorderColorItem(LabelEditorModel model) {
		final DefaultMutableModuleItem<ColorRGBA> item =
				new DefaultMutableModuleItem<>(this, model.getName() + " border color", ColorRGBA.class);
		item.setPersisted(false);
		item.setLabel("Default border color");
		item.setValue(this, getColor(model.colors().getDefaultBorderColor().get()));
		borderColorItems.put(item.getName(), model);
		addInput(item);
	}

	private ColorRGBA getColor(int color) {
		return new ColorRGBA(
				ARGBType.red(color),
				ARGBType.green(color),
				ARGBType.blue(color),
				ARGBType.alpha(color)
		);
	}

	private int getColor(ColorRGBA color) {
		return color.getARGB();
	}

	private void makeRendererItems(LabelEditorView<?> view) {
		view.renderers().forEach(renderer -> {
			String id = view.toString() + " " + renderer.getName();
			namedRenderers.put(id, renderer);
			namedRendererViews.put(id, view);
			final DefaultMutableModuleItem<Boolean> item =
					new DefaultMutableModuleItem<>(this, id, Boolean.class);
			item.setPersisted(false);
			item.setLabel(renderer.getName());
			item.setValue(this, renderer.isActive());
			addInput(item);
		});
	}
}
