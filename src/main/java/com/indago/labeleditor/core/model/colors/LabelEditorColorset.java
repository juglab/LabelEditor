package com.indago.labeleditor.core.model.colors;

import bdv.util.VirtualChannels;
import net.imglib2.type.numeric.RealType;

import java.util.HashMap;

public class LabelEditorColorset extends HashMap<Object, LabelEditorColor> implements VirtualChannels.VirtualChannel {

	private final LabelEditorTagColors colors;

	public LabelEditorColorset(LabelEditorTagColors colors) {
		this.colors = colors;
	}

	@Override
	public void updateVisibility() {
	}

	@Override
	public void updateSetupParameters() {
	}

	public LabelEditorColor put(Object o, int color) {
		LabelEditorColor put = super.put(o, new LabelEditorColor(color));
		update();
		return put;
	}

	public <T extends RealType<T>> LabelEditorColor put(Object o, int minColor, int maxColor, T min, T max) {
		LabelEditorColor put = super.put(o, new LabelEditorValueColor<>(minColor, maxColor, min, max));
		update();
		return put;
	}

	private void update() {
		if(colors != null) colors.notifyListeners();
	}

	@Override
	public LabelEditorColor remove(Object o) {
		LabelEditorColor remove = super.remove(o);
		update();
		return remove;
	}

	@Override
	public void clear() {
		super.clear();
		update();
	}

	@Override
	public boolean remove(Object o, Object o1) {
		boolean remove = super.remove(o, o1);
		update();
		return remove;
	}

	@Override
	public LabelEditorColor get(Object o) {
		return computeIfAbsent(o, k -> new LabelEditorColor(0));
	}

}
