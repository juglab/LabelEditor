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

import bdv.viewer.SourceAndConverter;
import bdv.viewer.render.AccumulateProjector;
import bdv.viewer.render.AccumulateProjectorFactory;
import bdv.viewer.render.VolatileProjector;

import java.util.List;
import java.util.concurrent.ExecutorService;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.view.LabelEditorOverlayRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;

import java.util.ArrayList;
import java.util.Map;

public class LabelEditorAccumulateProjector extends AccumulateProjector<ARGBType, ARGBType> {
    private final int indexFirstLabeling;

    static AccumulateProjectorFactory<ARGBType> createFactory(BdvInterface bdvInterface) {
        return new AccumulateProjectorFactory<ARGBType>() {

            @Override
            public VolatileProjector createProjector(
                    final List<VolatileProjector> sourceProjectors,
                    final List<SourceAndConverter<?>> sources,
                    final List<? extends RandomAccessible<? extends ARGBType>> sourceScreenImages,
                    final RandomAccessibleInterval<ARGBType> targetScreenImage,
                    final int numThreads,
                    final ExecutorService executorService) {
                return new LabelEditorAccumulateProjector(bdvInterface, sourceProjectors, sources, sourceScreenImages, targetScreenImage, numThreads, executorService);
            }
        };
    }

    private LabelEditorAccumulateProjector(
            final BdvInterface bdvInterface,
            final List<VolatileProjector> sourceProjectors,
            final List<SourceAndConverter<?>> sources,
            final List<? extends RandomAccessible<? extends ARGBType>> sourceScreenImages,
            final RandomAccessibleInterval<ARGBType> target,
            final int numThreads,
            final ExecutorService executorService) {
        super(labelEditorProjectors(bdvInterface, sources, sourceProjectors),
                labelEditorScreenImages(bdvInterface, sources, sourceScreenImages),
                target, numThreads, executorService);
        indexFirstLabeling = firstLabelingIndex(bdvInterface, sources);
    }

    private static ArrayList<? extends RandomAccessible<? extends ARGBType>> labelEditorScreenImages(
            BdvInterface bdvInterface,
            List<SourceAndConverter<?>> sources,
            List<? extends RandomAccessible<? extends ARGBType>> sourceScreenImages) {

        ArrayList<RandomAccessible<? extends ARGBType>> res = new ArrayList<>();
        ArrayList<RandomAccessible<? extends ARGBType>> resLabeling = new ArrayList<>();

        for (int i = 0; i < sources.size(); i++) {
            SourceAndConverter<?> source = sources.get(i);
            RandomAccessibleInterval<? extends ARGBType> screenImage = (RandomAccessibleInterval<? extends ARGBType>) sourceScreenImages.get(i);
            if(bdvInterface.getIndexSources().containsKey(source)) {
                resLabeling.addAll(getRendererSourcesForLabeling(screenImage, bdvInterface.getIndexSources().get(sources.get(i))));
            } else {
                res.add(screenImage);
            }
        }
        res.addAll(resLabeling);
        return res;
    }

    private synchronized static <L> ArrayList<RandomAccessible<? extends ARGBType>> getRendererSourcesForLabeling(
            RandomAccessibleInterval<? extends ARGBType> screenImage,
            InteractiveLabeling<L> labeling) {
        ArrayList<RandomAccessible<? extends ARGBType>> res = new ArrayList<>();
        for (LabelEditorRenderer<L> renderer : labeling.view().renderers()) {
            if (!renderer.isActive()) continue;
            if (LabelEditorOverlayRenderer.class.isAssignableFrom(renderer.getClass())) {
                LabelEditorOverlayRenderer<L> overlayRenderer = (LabelEditorOverlayRenderer<L>) renderer;
                overlayRenderer.init(labeling.model(), screenImage);
                overlayRenderer.updateOnTagChange();
                res.add(overlayRenderer.getOutput());
            }
        }
        return res;
    }

    private static List<VolatileProjector> labelEditorProjectors(
            BdvInterface bdvInterface,
            List<SourceAndConverter<?>> sources,
            List<VolatileProjector> sourceProjectors) {


        // sort out sources belonging to labelings and order the associated projects to the end of the list
        List<VolatileProjector> allProjectors = new ArrayList<>();
        List<VolatileProjector> rendererProjectors = new ArrayList<>();

        for (int i = 0; i < sources.size(); i++) {

            SourceAndConverter<?> source = sources.get(i);
            if(bdvInterface.getIndexSources().containsKey(source)) {
                InteractiveLabeling<?> labeling = bdvInterface.getIndexSources().get(source);
                for (LabelEditorRenderer<?> renderer : labeling.view().renderers()) {
                    if (LabelEditorOverlayRenderer.class.isAssignableFrom(renderer.getClass())) {
                        rendererProjectors.add(sourceProjectors.get(i));
                    }
                }
            } else {
                allProjectors.add(sourceProjectors.get(i));
            }
        }
        allProjectors.addAll(rendererProjectors);
        return allProjectors;
    }

    private static int firstLabelingIndex(
            BdvInterface bdvInterface,
            List<SourceAndConverter<?>> sources) {
        int nonLabelingProjectors = 0;
        for (SourceAndConverter<?> source : sources) {
            if (!bdvInterface.getIndexSources().containsKey(source)) {
                nonLabelingProjectors++;
            }
        }
        return nonLabelingProjectors;
    }

    @Override
    protected void accumulate(final Cursor<? extends ARGBType>[] accesses, final ARGBType target) {
        // accumulate sources additively
        int aSum = 0, rSum = 0, gSum = 0, bSum = 0;
        for (int i = 0; i < indexFirstLabeling; i++) {
            final int value = accesses[i].get().get();
            final int a = ARGBType.alpha(value);
            final int r = ARGBType.red(value);
            final int g = ARGBType.green(value);
            final int b = ARGBType.blue(value);
            aSum += a;
            rSum += r;
            gSum += g;
            bSum += b;
        }
        if (aSum > 255)
            aSum = 255;
        if (rSum > 255)
            rSum = 255;
        if (gSum > 255)
            gSum = 255;
        if (bSum > 255)
            bSum = 255;

        float alpha = 0, red = 0, green = 0, blue = 0;

        // accumulate labeleditor tag colors as overlay
        for (int i = accesses.length - 1; i >= indexFirstLabeling; i--) {
            final int value = accesses[i].get().get();
            final float newalpha = ((float) ARGBType.alpha(value)) / 255.f;
            final float newred = ARGBType.red(value);
            final float newgreen = ARGBType.green(value);
            final float newblue = ARGBType.blue(value);
            if (alpha < 0.0001 && newalpha < 0.0001) continue;
            if (newgreen + newred + newblue < 0.0001) continue;
            red = (red * alpha + newred * newalpha * (1 - alpha)) / (alpha + newalpha * (1 - alpha));
            green = (green * alpha + newgreen * newalpha * (1 - alpha)) / (alpha + newalpha * (1 - alpha));
            blue = (blue * alpha + newblue * newalpha * (1 - alpha)) / (alpha + newalpha * (1 - alpha));
            alpha = alpha + newalpha * (1 - alpha);
        }

        // concatenate additive sources with overlay tag colors
        final float newalpha = ((float) aSum) / 255.f;
        red = (red * alpha + (float) rSum * newalpha * (1 - alpha)) / (alpha + newalpha * (1 - alpha));
        green = (green * alpha + (float) gSum * newalpha * (1 - alpha)) / (alpha + newalpha * (1 - alpha));
        blue = (blue * alpha + (float) bSum * newalpha * (1 - alpha)) / (alpha + newalpha * (1 - alpha));
        alpha = alpha + newalpha * (1 - alpha);

        float perc = alpha;
//        System.out.println(red*perc + ":" + green*perc + ":" + blue*perc + ":" + 255);
        target.set(ARGBType.rgba(red*perc, green*perc, blue*perc, 255));
//        target.set(ARGBType.rgba(red, green, blue, alpha));
    }
}
