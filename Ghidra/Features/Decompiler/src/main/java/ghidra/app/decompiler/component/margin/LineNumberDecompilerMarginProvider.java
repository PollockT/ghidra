/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghidra.app.decompiler.component.margin;

import java.awt.*;
import java.math.BigInteger;

import javax.swing.JPanel;

import docking.util.GraphicsUtils;
import docking.widgets.fieldpanel.LayoutModel;
import docking.widgets.fieldpanel.listener.IndexMapper;
import docking.widgets.fieldpanel.listener.LayoutModelListener;
import ghidra.app.decompiler.DecompileOptions;
import ghidra.program.model.listing.Program;

/**
 * The built-in provider for the Decompiler's line number margin
 */
public class LineNumberDecompilerMarginProvider extends JPanel
		implements DecompilerMarginProvider, LayoutModelListener {

	private LayoutPixelIndexMap pixmap;
	private LayoutModel model;

	@Override
	public void setProgram(Program program, LayoutModel model, LayoutPixelIndexMap pixmap) {
		setLayoutManager(model);
		this.pixmap = pixmap;
		repaint();
	}

	private void setLayoutManager(LayoutModel model) {
		if (this.model == model) {
			return;
		}
		if (this.model != null) {
			this.model.removeLayoutModelListener(this);
		}
		this.model = model;
		this.model.addLayoutModelListener(this);
		setWidthForLastLine();
		if (this.model != null) {
			this.model.addLayoutModelListener(this);
		}
	}

	@Override
	public void setOptions(DecompileOptions options) {
		this.setFont(options.getDefaultFont());
		setWidthForLastLine();
		repaint();
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public void modelSizeChanged(IndexMapper indexMapper) {
		setWidthForLastLine();
		repaint();
	}

	@Override
	public void dataChanged(BigInteger start, BigInteger end) {
		repaint();
	}

	private void setWidthForLastLine() {
		if (model == null) {
			return;
		}
		int lastLine = model.getNumIndexes().intValueExact();
		int width = getFontMetrics(getFont()).stringWidth(Integer.toString(lastLine));
		setPreferredSize(new Dimension(width, 0));
		invalidate();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Rectangle visible = getVisibleRect();
		BigInteger startIdx = pixmap.getIndex(visible.y);
		BigInteger endIdx = pixmap.getIndex(visible.y + visible.height);
		int ascent = g.getFontMetrics().getMaxAscent();
		for (BigInteger i = startIdx; i.compareTo(endIdx) <= 0; i = i.add(BigInteger.ONE)) {
			GraphicsUtils.drawString(this, g, i.add(BigInteger.ONE).toString(), 0,
				pixmap.getPixel(i) + ascent);
		}
	}
}
