#@OUTPUT sc.fiji.labeleditor.core.model.DefaultLabelEditorModel(label="my model") model
#@ Img input
#@ IOService io

import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel

model = DefaultLabelEditorModel.initFromLabelMap(input)
