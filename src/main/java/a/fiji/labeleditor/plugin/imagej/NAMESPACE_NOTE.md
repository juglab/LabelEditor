This namespace is chosen because otherwise when calling `ij.ui.show(labeling)`, the DefaultDisplayViewer will be used even though it only pretends to be able to display ImgLabelings. 
Which viewer will be used seems to depend on the alphabetical order of the namespace (sc.fiji.labeleditor did not work).