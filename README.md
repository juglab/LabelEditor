[![](https://travis-ci.com/juglab/labeleditor.svg?branch=master)](https://travis-ci.com/juglab/labeleditor)

# LabelEditor

The LabelEditor is a JugLab creation aiming to close the gap between segmentation algorithms and user interaction in ImageJ2 / Fiji.

## Implementation

### core

#### model
The heart of this project is a mechanism to tag any label of an `ImgLabeling` instance with tags. A tag is of type `Object` so it can be anything. 

#### view
A color can be assigned to a tag in `ARGBType` representation. The tag `LabelEditorTag.NO_TAG` can be used to assign a color to all labels with no associated tag.
Renderers implementing `LabelEditorRenderer` will be discovered via the SciJava Plugin mechanism and translate an `ImgLabeling` into a `RandomAccessibleInterval<ARGBType>` with reference to the assigned tags and tag colors.

#### controller
This project uses Scijava `Behaviours` to bundle interaction concepts and connect them to a specific `LabelEditorInterface` instance (e.g. BDV or BVV).

### plugins

#### interfaces
- **`BigDataViewer`**: currently the main target interface
    - `[TODO]` [bigdataviewer-ui-panel](https://github.com/bigdataviewer/bigdataviewer-ui-panel) is used to provide control over BDV and LabelEditor settings  
- **`BigVolumeViewer`**: buggy proof of concept implementation

#### behaviours
- **`SelectionBehaviours`** work like any default selection model and uses the tags `LabelEditorTag.MOUSE_OVER` and `LabelEditorTag.SELECTED`
- **`ConflictSelectionBehaviours`** are deselecting conflicting labels on each selection
- **`ModificationBehaviours`**
    - delete a label (in BDV via right click)
    - naive label division (watershed) (`[TODO]` this needs better options / an interface)
    - `[TODO]` label merge
    - `[TODO]` fill holes
- `[TODO]` export `ImgLabeling` index image
- `[TODO]` export `ImgLabeling` as image with one mask channel per label 
- `[TODO]` compute label regions, show information in table

#### renderer
- **`DefaultLabelEditorRenderer`**: paints each pixel of a label with the color of the tag(s)
- **`BorderLabelEditorRenderer`**: paints only the outer pixels of a label with the color of the tag(s)
- `[TODO]` render numbers next to each label section
- `[TODO]` render bounding box

### applications

The following applications can be tested by installing this update site in Fiji: `[TODO]` 

- **`ImgLabelingViewer`**: Displays a `ImgLabeling` and an optional `ImgPlus` in the LabelEditor
- **`CCAViewer`**: Performs otsu threshold and CCA from imagej-ops on the input image and displays the result in the LabelEditor
- **`WatershedViewer`**: Performs watershed from imagej-ops on the input image and displays the result in the LabelEditor
- **`MaskChannelsViewer`**: Create `ImgLabeling` from mask channels (performing CCA on each channel), user provides channel dimension and channel position of source image

## How to use the API
There is no real documentation yet, also no JavaDoc. Have a look at `src/test/com/indago/labeleditor/howto` to see how to integrate this project into your own implementation. 

## Configuration
- `[TODO]` Make behaviors configurable / come up with config loading / saving 

## Future steps
- ask for community feedback, maybe move / split up repo into core / plugins / applications
- join forces with Labkit
- figure out how to move towards ROI integration

## Integration efforts
- `[ongoing]` [metaseg](https://github.com/juglab/metaseg)
- `[ongoing]` [MoMA](https://github.com/fjug/MoMA)
- `[planned]` [Labkit](https://github.com/maarzt/imglib2-labkit)
- `[planned]` [CLIJ](https://github.com/clij/clij-bdv)