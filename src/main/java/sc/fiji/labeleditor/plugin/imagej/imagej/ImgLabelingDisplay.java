package sc.fiji.labeleditor.plugin.imagej.imagej;

import net.imglib2.roi.labeling.ImgLabeling;
import org.scijava.Priority;
import org.scijava.display.AbstractDisplay;
import org.scijava.display.Display;
import org.scijava.plugin.Plugin;

@Plugin(type = Display.class, priority = 1)
public class ImgLabelingDisplay extends AbstractDisplay<ImgLabeling> {

	public ImgLabelingDisplay() {
		super(ImgLabeling.class);
	}
}