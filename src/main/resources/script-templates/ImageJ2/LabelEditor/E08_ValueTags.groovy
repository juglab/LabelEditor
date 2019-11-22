#@ OpService ops
#@ IOService io
#@ UIService ui


import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement
import net.imglib2.type.numeric.integer.IntType
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel
import sc.fiji.labeleditor.core.model.tagging.LabelEditorValueTag

input = io.open("https://samples.fiji.sc/blobs.png")

binary = ops.threshold().otsu(input)

labeling = ops.labeling().cca(binary, StructuringElement.EIGHT_CONNECTED)

// create a model for the LabelEditor, passing the labeling and setting the source data
model = new DefaultLabelEditorModel(labeling, input)

// random is used to generate a random integer value per label which we want to display
random = new Random();

// iterate over all labels
labeling.getMapping().getLabels().each { def label ->

    //for each label, assign a tag with a random value between 0 and 100
    randomValue = new LabelEditorValueTag("random", new IntType(random.nextInt(100)));
    model.tagging().addTagToLabel(randomValue, label);

}

// create a color for this value tag by passing the tag identifier and the min / max values of this tag
color = model.colors().makeValueFaceColor("random", new IntType(0), new IntType(100))
// set the min and max colors for the specified value range
color.setMinColor(0,0,255,250)
color.setMaxColor(255,0,0,250)

// set the face color for selected labels to white
model.colors().getSelectedFaceColor().set(255,255,255)

ui.show(model)