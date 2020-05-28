#@OUTPUT net.imglib2.roi.labeling.ImgLabeling(label="my labeling") labeling
#@ OpService ops
#@ IOService io

import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement

input = io.open("https://samples.fiji.sc/blobs.png")

binary = ops.threshold().otsu(input)

labeling = ops.labeling().cca(binary, StructuringElement.EIGHT_CONNECTED)
