package com.indago.labeleditor;

import bdv.util.PlaceHolderOverlayInfo;
import bdv.util.VirtualChannels;
import bdv.viewer.ViewerPanel;
import net.imglib2.type.numeric.ARGBType;

public class LUTChannel implements VirtualChannels.VirtualChannel {
	private final int argb;

	public LUTChannel(int argb) {
		this.argb = argb;
	}

	@Override
	public void updateVisibility() {
		update();
	}

	@Override
	public void updateSetupParameters() {
		update();
	}

	private void update() {

	}

	public void setPlaceHolderOverlayInfo(PlaceHolderOverlayInfo placeHolderOverlayInfo) {
	}

	public void setViewerPanel(ViewerPanel viewerPanel) {
	}

	public int getColor() {
		return argb;
	}
}
