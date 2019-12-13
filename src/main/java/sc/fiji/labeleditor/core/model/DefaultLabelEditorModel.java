package sc.fiji.labeleditor.core.model;

import de.embl.cba.table.select.SelectionModel;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import sc.fiji.labeleditor.plugin.behaviours.select.SelectionBehaviours;

public class DefaultLabelEditorModel<L> extends AbstractLabelEditorModel<L> {

	public DefaultLabelEditorModel(ImgLabeling<L, IntType> labeling, RandomAccessibleInterval data) {
		super(labeling);
		setData(data);
		addDefaultColorsets();
	}

	public DefaultLabelEditorModel(ImgLabeling<L, IntType> labeling) {
		super(labeling);
		addDefaultColorsets();
	}

	public static DefaultLabelEditorModel<IntType> initFromLabelMap(RandomAccessibleInterval labelMap) {
		return new DefaultLabelEditorModel<IntType>(makeLabeling(labelMap));
	}

	public static DefaultLabelEditorModel<IntType> initFromLabelMap(RandomAccessibleInterval labelMap, RandomAccessibleInterval data) {
		return new DefaultLabelEditorModel<IntType>(makeLabeling(labelMap), data);
	}

	protected void addDefaultColorsets() {
		colors().getDefaultFaceColor().set(DefaultColors.defaultFace());
		colors().getDefaultBorderColor().set(DefaultColors.defaultBorder());
		colors().getSelectedFaceColor().set(DefaultColors.selectedFace());
		colors().getSelectedBorderColor().set(DefaultColors.selectedBorder());
		colors().getFocusFaceColor().set(DefaultColors.focusFace());
		colors().getFocusBorderColor().set(DefaultColors.focusBorder());
	}
}
