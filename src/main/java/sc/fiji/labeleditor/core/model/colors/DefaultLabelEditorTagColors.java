package sc.fiji.labeleditor.core.model.colors;

import org.scijava.listeners.Listeners;

import java.util.HashMap;

public class DefaultLabelEditorTagColors extends HashMap<Object, LabelEditorColorset> implements LabelEditorTagColors {

	private final Listeners.List<ColorChangeListener> listeners = new Listeners.SynchronizedList<>();
	private boolean listenersPaused = false;

	public DefaultLabelEditorTagColors() {
	}

	@Override
	public LabelEditorColorset getColorset(Object tag) {
		return computeIfAbsent(tag, k -> new DefaultLabelEditorColorset(this));
	}

	@Override
	public Listeners<ColorChangeListener> listeners() {
		return listeners;
	}

	@Override
	public void pauseListeners() {
		listenersPaused = true;
	}

	@Override
	public void resumeListeners() {
		listenersPaused = false;
	}

	@Override
	public void notifyListeners() {
		ColorChangedEvent e = new ColorChangedEvent();
		listeners.list.forEach(listener -> listener.tagChanged(e));
	}

	// convenience methods

}
