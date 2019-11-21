package com.indago.labeleditor.core;

import com.indago.labeleditor.plugin.behaviours.export.ExportBehaviours;
import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImageJ;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestLabelMap {

	@Test
	public void loadIndexImg() throws IOException {
		ImageJ ij = new ImageJ();
		Img input = (Img) ij.io().open(getClass().getResource("/labelmap.png").getPath());
		LabelEditorPanel panel = new LabelEditorBdvPanel();
		panel.initFromLabelMap(input);
		assertNotNull(panel.model());
		assertNotNull(panel.model().labeling());
		ExportBehaviours exportBehaviours = new ExportBehaviours();
		exportBehaviours.init(panel.model(), panel.control(), panel.view());
		IterableInterval<IntType> labelMap = Views.iterable(exportBehaviours.getLabelMap());
		Cursor<IntType> resCursor = labelMap.localizingCursor();
		RandomAccess<UnsignedShortType> origRa = input.randomAccess();
		while(resCursor.hasNext()) {
			int resVal = resCursor.next().get();
			origRa.setPosition(resCursor);
			assertEquals(origRa.get().getInteger(), resVal);
		}
	}

}
