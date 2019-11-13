package com.indago.labeleditor.core.model.colors;

import bdv.util.VirtualChannels;
import com.indago.labeleditor.core.model.tagging.LabelEditorTagging;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LabelEditorColorset extends HashMap<Object, Integer> implements VirtualChannels.VirtualChannel {

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

	@Override
	public Integer put(Object o, Integer integer) {
		Integer put = super.put(o, integer);
		update();
		return put;
	}

	private void update() {
		//FIXME
		if(colors != null) colors.notifyListeners();
	}

	@Override
	public void putAll(Map<?, ? extends Integer> map) {
		super.putAll(map);
		update();
	}

	@Override
	public Integer remove(Object o) {
		Integer remove = super.remove(o);
		update();
		return remove;
	}

	@Override
	public void clear() {
		super.clear();
		update();
	}

	@Override
	public Integer putIfAbsent(Object o, Integer integer) {
		Integer integer1 = super.putIfAbsent(o, integer);
		update();
		return integer1;
	}

	@Override
	public boolean remove(Object o, Object o1) {
		boolean remove = super.remove(o, o1);
		update();
		return remove;
	}

	@Override
	public boolean replace(Object o, Integer integer, Integer v1) {
		boolean replace = super.replace(o, integer, v1);
		update();
		return replace;
	}

	@Override
	public Integer replace(Object o, Integer integer) {
		Integer replace = super.replace(o, integer);
		update();
		return replace;
	}

	@Override
	public Integer computeIfAbsent(Object o, Function<? super Object, ? extends Integer> function) {
		Integer integer = super.computeIfAbsent(o, function);
		update();
		return integer;
	}

	@Override
	public Integer computeIfPresent(Object o, BiFunction<? super Object, ? super Integer, ? extends Integer> biFunction) {
		Integer integer = super.computeIfPresent(o, biFunction);
		update();
		return integer;
	}

	@Override
	public Integer compute(Object o, BiFunction<? super Object, ? super Integer, ? extends Integer> biFunction) {
		Integer compute = super.compute(o, biFunction);
		update();
		return compute;
	}

	@Override
	public Integer merge(Object o, Integer integer, BiFunction<? super Integer, ? super Integer, ? extends Integer> biFunction) {
		Integer merge = super.merge(o, integer, biFunction);
		update();
		return merge;
	}

	@Override
	public void forEach(BiConsumer<? super Object, ? super Integer> biConsumer) {
		super.forEach(biConsumer);
		update();
	}

	@Override
	public void replaceAll(BiFunction<? super Object, ? super Integer, ? extends Integer> biFunction) {
		super.replaceAll(biFunction);
		update();
	}
}
