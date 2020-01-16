package sc.fiji.labeleditor.plugin.interfaces.bvv;

import net.imglib2.roi.labeling.LabelingType;
import sc.fiji.labeleditor.plugin.behaviours.FocusBehaviours;

import java.util.List;

public class BvvFocusBehaviours<L> extends FocusBehaviours<L> {

	@Override
	public synchronized void focusFirstLabelAtPosition(int x, int y) {
		BvvInterface<L> bvvInterface = (BvvInterface<L>) labeling.interfaceInstance();
		List<LabelingType<L>> allSets = bvvInterface.getAllLabelsAtMousePosition(x, y, labeling.model());
		if(allSets == null || allSets.size() == 0) {
			labeling.model().tagging().pauseListeners();
			defocus();
			labeling.model().tagging().resumeListeners();
			return;
		}
		LabelingType<L> labelset = allSets.get(0);
		int intIndex;
		try {
			intIndex = labelset.getIndex().getInteger();
		} catch(ArrayIndexOutOfBoundsException exc) {return;}
		if(intIndex == currentSegment) return;
		currentSegment = intIndex;
		new Thread(() -> {
			labeling.model().tagging().pauseListeners();
			defocus();
			focus(labelset);
			labeling.model().tagging().resumeListeners();
		}).start();
	}
}
