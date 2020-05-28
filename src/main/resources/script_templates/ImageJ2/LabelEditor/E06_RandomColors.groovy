#@OUTPUT sc.fiji.labeleditor.core.model.DefaultLabelEditorModel(label="my model") model
#@ OpService ops
#@ IOService io

import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel

input = io.open("https://samples.fiji.sc/blobs.png")

binary = ops.threshold().otsu(input)

labeling = ops.labeling().cca(binary, StructuringElement.EIGHT_CONNECTED)

model = new DefaultLabelEditorModel(labeling)

random = new Random();
for (def label : labeling.getMapping().getLabels()) {
    // assign each label also as a tag to itself (so you can set colors for each label separately)
    model.tagging().addTagToLabel(label, label)
    // add random color to each tag
    model.colors().getBorderColor(label).set(random.nextInt(255), random.nextInt(255), random.nextInt(255), 255)
}
