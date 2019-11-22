#@ OpService ops
#@ IOService io
#@ UIService ui

import net.imglib2.algorithm.labeling.ConnectedComponents
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel

input = io.open("https://samples.fiji.sc/blobs.png")

binary = ops.threshold().otsu(input)

labeling = ops.labeling().cca(binary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED)

model = new DefaultLabelEditorModel(labeling, input)

model.colors().getDefaultBorderColor().set(0, 255, 255)
model.colors().getDefaultFaceColor().set(0,0,0,0)
model.colors().getSelectedBorderColor().set(0,255,0)
model.colors().getSelectedFaceColor().set(255,255,0,100)
model.colors().getFocusBorderColor().set(255,0,0)
model.colors().getFocusFaceColor().set(0,0,0,0)

ui.show(model)