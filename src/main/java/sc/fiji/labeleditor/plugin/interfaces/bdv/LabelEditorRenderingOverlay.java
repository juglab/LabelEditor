/*-
 * #%L
 * UI component for image segmentation label comparison and selection
 * %%
 * Copyright (C) 2019 - 2020 DAIS developers
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package sc.fiji.labeleditor.plugin.interfaces.bdv;

import bdv.util.BdvOverlay;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import static net.imglib2.ui.util.GuiUtil.ARGB_COLOR_MODEL;

public class LabelEditorRenderingOverlay extends BdvOverlay
{

	private BufferedImage image;

	@Override
	public synchronized void draw( final Graphics2D g )
	{
		if(image == null) return;
		g.setColor(Color.white);
//		g.setFont( new Font( "Monospaced", Font.PLAIN, 12 ) );
		g.drawImage(image, 0, 0, null);
//		g.drawString("Test, test", 100, 100);

	}

	public void updateContent(RandomAccessibleInterval<ARGBType> rendering) {
		synchronized (rendering)
		{
			int width = (int) rendering.dimension(0);
			int height = (int) rendering.dimension(1);
			int size = width * height;
			int[] data = new int[size];
			RandomAccess<ARGBType> ra = rendering.randomAccess();
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					ra.setPosition(x, 0);
					ra.setPosition(y, 1);
					data[x + y * width] = ra.get().get();
				}
			}
			final SampleModel sampleModel = ARGB_COLOR_MODEL.createCompatibleWritableRaster( 1, 1 ).getSampleModel()
					.createCompatibleSampleModel( width, height );
			final DataBuffer dataBuffer = new DataBufferInt( data, size, 0 );
			final WritableRaster rgbRaster = Raster.createWritableRaster( sampleModel, dataBuffer, null );
			image = new BufferedImage( ARGB_COLOR_MODEL, rgbRaster, false, null );
		}
	}
}
