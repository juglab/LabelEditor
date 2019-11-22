#@ OpService ops
#@ IOService io
#@ UIService ui

import net.imglib2.algorithm.labeling.ConnectedComponents
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel

input = io.open("https://samples.fiji.sc/blobs.png")

binary = ops.threshold().otsu(input)

labeling = ops.labeling().cca(binary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED)

model = new DefaultLabelEditorModel(labeling, input)

TAG1 = "tag1"
TAG2 = "tag2"
TAG3 = "tag3"

model.tagging().addTagToLabel(TAG1, new Integer(1))
model.tagging().addTagToLabel(TAG1, new Integer(7))
model.tagging().addTagToLabel(TAG1, new Integer(14))

model.tagging().addTagToLabel(TAG2, new Integer(3))
model.tagging().addTagToLabel(TAG2, new Integer(13))
model.tagging().addTagToLabel(TAG2, new Integer(28))

model.tagging().addTagToLabel(TAG3, new Integer(5))
model.tagging().addTagToLabel(TAG3, new Integer(18))
model.tagging().addTagToLabel(TAG3, new Integer(25))

model.colors().getFaceColor(TAG1).set(255,50, 0)
model.colors().getFaceColor(TAG2).set(0,50, 255)
model.colors().getFaceColor(TAG3).set(50,255, 0)

ui.show(model)