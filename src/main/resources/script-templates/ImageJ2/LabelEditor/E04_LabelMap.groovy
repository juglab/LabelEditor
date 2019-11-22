#@ Img input
#@ IOService io
#@ UIService ui

import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel

model = DefaultLabelEditorModel.initFromLabelMap(input)

ui.show(model)