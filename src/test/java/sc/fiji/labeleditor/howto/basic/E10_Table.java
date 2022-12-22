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
package sc.fiji.labeleditor.howto.basic;

import net.imagej.ImageJ;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import org.jetbrains.annotations.Nullable;
import org.scijava.table.Column;
import org.scijava.table.Table;
import org.scijava.table.io.TableIOOptions;
import org.scijava.table.io.TableIOService;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.colors.LabelEditorValueColor;

import java.io.IOException;
import java.util.Random;
import java.util.function.Function;

/**
 * How to active the 3D mode of the LabelEditor
 */
public class E10_Table {

	public void run() throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();

		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());

		Img<IntType> binary = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(binary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		LabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling, input);

		TableIOOptions options = createDefaultLabelTableOptions();
		Table table = ij.get(TableIOService.class).open(getClass().getResource("/table.csv").getPath(), options);
		assignTableToModel(model, table);

		ij.ui().show(model);
	}

	private void assignTableToModel(LabelEditorModel<Integer> model, Table table) {
		Column labelColumn = table.get(0);
		for (int i = 1; i < table.getColumnCount(); i++) {
			Column column = table.get(i);
			String columnName = table.getColumnHeader(i);
			addColumn(model, labelColumn, column, columnName);
		}
	}

	@Nullable
	private TableIOOptions createDefaultLabelTableOptions() {
		Function<String, ?> parseTable = (value) -> {
			if (value == null) {
				return value;
			}
			try {
				return new DoubleType(Double.parseDouble(value));
			} catch (NumberFormatException nfe) {
				return value;
			}
		};
		TableIOOptions options = new TableIOOptions()
				.readRowHeaders(false)
				.parser(parseTable)
				.columnType(0, Integer.class);
		return options;
	}

	private <T> void addColumn(LabelEditorModel<Integer> model, Column<Integer> labelColumn, Column<T> column, String columnName) {
		T min = null;
		T max = null;
		for (int j = 0; j < labelColumn.size(); j++) {
			T value = column.get(j);
			if(min == null) {
				min = value;
			} else if(((Comparable)value).compareTo(min) < 0) {
				min = value;
			}
			if(max == null) {
				max = value;
			} else if(((Comparable)value).compareTo(max) > 0) {
				max = value;
			}
			model.tagging().addValueToLabel(columnName, value, labelColumn.get(j));
		}
//		if(RealType.class.isAssignableFrom(min.getClass())) {
		addColor(model, columnName, min, max);
//		}
	}

	private <T extends RealType<T>> void addColor(LabelEditorModel<Integer> model, String columnName, Object min, Object max) {
		LabelEditorValueColor<?> indexColor = model.colors().makeValueFaceColor(columnName, (T)min, (T)max);
		// create a color and the min / max values of this tag
		Random random = new Random();
		indexColor.setMinColor(0,0,0,0);
		indexColor.setMaxColor(random.nextInt(200)+50,random.nextInt(200)+50,random.nextInt(200)+50,255);
	}

	public static void main(String... args) throws IOException {
		new E10_Table().run();
	}


}
