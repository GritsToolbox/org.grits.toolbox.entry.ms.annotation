package org.grits.toolbox.entry.ms.annotation.tablehelpers;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
//import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import org.grits.toolbox.display.control.table.tablecore.GRITSImagePainter;
import org.grits.toolbox.utils.image.GlycanImageProvider.GlycanImageObject;

public class AutoResizeImagePainter extends GRITSImagePainter {
	private final boolean calculate;
	private final int spacing;
	//	private String sGlycanId = null;
	private Rectangle imageBounds = null;
	private GlycanImageObject gio = null;

	public AutoResizeImagePainter() {
		super(null);
		this.calculate = false;
		this.spacing = 0;
	}

	public AutoResizeImagePainter(GlycanImageObject gio) {
		super(null, true);
		this.calculate = false;
		this.spacing = 0;
		this.gio = gio;
	}

	public AutoResizeImagePainter(GlycanImageObject gio, boolean paintBg) {
		super(null, paintBg);
		this.calculate = false;
		this.spacing = 0;
		this.gio = gio;
	}

	public AutoResizeImagePainter(GlycanImageObject gio, boolean paintBg, int spacing, boolean calculate) {
		super(null, paintBg);
		this.calculate = calculate;
		this.spacing = spacing;
		this.gio = gio;
	}

	@Override
	public void paintCell(ILayerCell cell, GC gc, Rectangle bounds,
			IConfigRegistry configRegistry) {
		//		this.image = getImage(cell, configRegistry);
		Rectangle newBounds = this.calculate ? getBoundsFromCache(cell, gc, bounds, configRegistry) : bounds;
		newBounds.x = bounds.x;
		newBounds.y = bounds.y;
		if ( bounds.height < newBounds.height && calculate) {
			ILayer layer = cell.getLayer();
			layer.doCommand(
					new RowResizeCommand(
							layer, 
							cell.getRowPosition(), 
							newBounds.height));
		}
		if ( bounds.width < newBounds.width && calculate) {
			ILayer layer = cell.getLayer();
			layer.doCommand(
					new ColumnResizeCommand(
							layer, 
							cell.getColumnPosition(), 
							newBounds.width));
		}
		Rectangle adjustedCellBounds = cell.getLayer().getLayerPainter().adjustCellBounds(cell.getColumnPosition(), cell.getRowPosition(), newBounds);				
		IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
		adjustedCellBounds.x += CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, bounds, adjustedCellBounds.width);
		adjustedCellBounds.y += CellStyleUtil.getVerticalAlignmentPadding(cellStyle, bounds, adjustedCellBounds.height);
		super.paintCell(cell, gc, adjustedCellBounds, configRegistry);
		//		if( image != null && ! image.isDisposed() )
		//			image.dispose();
	}

	protected Rectangle getBoundsFromCache(ILayerCell cell, GC gc, Rectangle bounds,
			IConfigRegistry configRegistry) {
		if( this.imageBounds != null ) 
			return this.imageBounds;

		int iWidth = getPreferredWidth(cell, gc, configRegistry) + this.spacing;
		int iHeight = getPreferredHeight(cell, gc, configRegistry) + this.spacing;

		this.imageBounds = new Rectangle(0, 0, iWidth, iHeight);
		return getBoundsFromCache(cell, gc, bounds, configRegistry);
	}

	@Override
	public ICellPainter getCellPainterAt(int x, int y, ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		Rectangle imageBounds = getImage(cell, configRegistry).getBounds();
		IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
		int x0 = bounds.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, bounds, imageBounds.width);
		int y0 = bounds.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, bounds, imageBounds.height);
		if (	x >= x0 &&
				x < x0 + imageBounds.width &&
				y >= y0 &&
				y < y0 + imageBounds.height) {
			return super.getCellPainterAt(x, y, cell, gc, bounds, configRegistry);
		} else {
			return null;
		}
	}

	public void setImage( Image image ) {
		this.image = image;
	}

	@Override
	protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
		if( gio == null ) {
			this.image = null;
			return this.image;
		}
		image = gio.getSwtImage();
		return this.image;
	}
}
