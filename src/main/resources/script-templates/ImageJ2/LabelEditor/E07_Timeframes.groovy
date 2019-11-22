#@ UIService ui

import net.imglib2.img.array.ArrayImgs
import net.imglib2.roi.labeling.ImgLabeling
import net.imglib2.util.Intervals
import net.imglib2.view.Views
import sc.fiji.labeleditor.plugin.mode.timeslice.TimeSliceLabelEditorModel

LABEL1 = "label1";
LABEL2 = "label2";
LABEL3 = "label3";
LABEL4 = "label4";

TAG1 = "tag1";
TAG2 = "tag2";

backing = ArrayImgs.ints( 500, 500, 2 )
labels = new ImgLabeling<>( backing )

def addLabel1 = {pixel -> pixel.add( LABEL1 )}
def addLabel2 = {pixel -> pixel.add( LABEL2 )}
def addLabel3 = {pixel -> pixel.add( LABEL3 )}
def addLabel4 = {pixel -> pixel.add( LABEL4 )}

//draw labels to time point 0
Views.interval( labels, Intervals.createMinSize( 220, 220, 0, 100, 100, 1 ) ).forEach( addLabel1 )
Views.interval( labels, Intervals.createMinSize( 220, 280, 0, 100, 100, 1 ) ).forEach( addLabel2 )
Views.interval( labels, Intervals.createMinSize( 280, 280, 0, 100, 100, 1 ) ).forEach( addLabel3 )
Views.interval( labels, Intervals.createMinSize( 280, 220, 0, 100, 100, 1 ) ).forEach( addLabel4 )

//draw labels to time point 1
Views.interval( labels, Intervals.createMinSize( 320, 320, 1, 100, 100, 1 ) ).forEach( addLabel1 )
Views.interval( labels, Intervals.createMinSize( 300, 300, 1, 100, 100, 1 ) ).forEach( addLabel2 )

// create model which can handle time sliced datasets
model = new TimeSliceLabelEditorModel<>(labels, 2)

model.tagging().addTagToLabel(TAG1, LABEL1)
model.tagging().addTagToLabel(TAG2, LABEL2)
model.tagging().addTagToLabel(TAG1, LABEL3)
model.tagging().addTagToLabel(TAG2, LABEL4)

model.colors().getFaceColor(TAG1).set(0,255,255,100)
model.colors().getFaceColor(TAG2).set(255,0,255,100)

ui.show(model)