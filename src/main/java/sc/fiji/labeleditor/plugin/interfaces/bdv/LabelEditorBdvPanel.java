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

import bdv.util.Bdv;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import org.scijava.Disposable;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import sc.fiji.labeleditor.core.controller.DefaultInteractiveLabeling;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.DefaultLabelEditorView;
import sc.fiji.labeleditor.core.view.LabelEditorView;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelEditorBdvPanel extends JPanel implements Disposable {

	@Parameter
	private Context context;

	@Parameter
	private CommandService commandService;

	private final Map<String, InteractiveLabeling> labelings = new HashMap<>();

	private BdvHandlePanel bdvHandlePanel;
	private BdvInterface interfaceInstance;

	public LabelEditorBdvPanel() {
		this(null, Bdv.options());
	}

	public LabelEditorBdvPanel(Context context) {
		this(context, Bdv.options());
	}

	public LabelEditorBdvPanel(BdvOptions options) {
		this(null, options);
	}

	public LabelEditorBdvPanel(Context context, BdvOptions options) {
		if(context != null) context.inject(this);
		initialize(options);
	}

	private void initialize(BdvOptions options) {
		setMinimumSize(new Dimension(100, 100));
		setPreferredSize(new Dimension(500, 500));
		setLayout( new MigLayout("fill") );
		interfaceInstance = new BdvInterface(context);
		adjustOptions(options);
		bdvHandlePanel = new BdvHandlePanel(null, options);
		interfaceInstance.setup(bdvHandlePanel);
		Component viewer = bdvHandlePanel.getSplitPanel();
		this.add( viewer, "span, grow, push" );
	}

	protected void adjustOptions(BdvOptions options) {
		options.accumulateProjectorFactory(interfaceInstance.projector());
	}

	public <L> InteractiveLabeling<L> add(LabelEditorModel<L> model) {
		return add(model, new BdvOptions());
	}

	public <L> InteractiveLabeling<L> add(LabelEditorModel<L> model, BdvOptions options) {
		LabelEditorView<L> view = new DefaultLabelEditorView<>(model);
		if(context != null) context.inject(view);
		view.addDefaultRenderers();
		return add(model, view, options);
	}

	public <L> InteractiveLabeling<L> add(LabelEditorModel<L> model, LabelEditorView<L> view) {
		return add(model, view, new BdvOptions());
	}

	public <L> InteractiveLabeling<L> add(LabelEditorModel<L> model, LabelEditorView<L> view, BdvOptions options) {
		DefaultInteractiveLabeling<L> interactiveLabeling = interfaceInstance.control(model, view, options);
		labelings.put(model.getName(), interactiveLabeling);
		return interactiveLabeling;
	}

	public List<BdvSource> getSources() {
		return interfaceInstance.getDataSources();
	}

	protected Context context() {
		return context;
	}

	public BdvHandlePanel getBdvHandlePanel() {
		return bdvHandlePanel;
	}

	protected BdvInterface getInterfaceInstance() {
		return interfaceInstance;
	}

	public void removeModels() {
		labelings.forEach((name, labeling) -> {
			interfaceInstance.remove(labeling);
		});
	}

	@Override
	public void dispose() {
		if(bdvHandlePanel != null) bdvHandlePanel.close();
	}
}
