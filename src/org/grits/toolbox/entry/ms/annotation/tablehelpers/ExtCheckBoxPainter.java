package org.grits.toolbox.entry.ms.annotation.tablehelpers;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.LineBorderDecorator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import org.grits.toolbox.display.control.table.tablecore.GRITSImagePainter;

public class ExtCheckBoxPainter extends LineBorderDecorator {
	private final Image checkedImg;
	private final Image uncheckedImg;
	protected boolean bCurStatus = false;
	protected String sText = null;
	protected Image curImage = null;
	
	public ExtCheckBoxPainter(String sText, boolean _bCurStatus) {
		super(new GRITSImagePainter());
		checkedImg = GUIHelper.getImage("checked"); //$NON-NLS-1$
		uncheckedImg = GUIHelper.getImage("unchecked"); //$NON-NLS-1$
		this.sText = sText;
		setCurStatus(_bCurStatus);
	}
	
	public void setCurStatus(boolean bCurStatus) {
		this.bCurStatus = bCurStatus;
		curImage = bCurStatus ? checkedImg : uncheckedImg;
	}
	
	public boolean getCurStatus() {
		return this.bCurStatus;
	}
	
	public int getPreferredWidth(boolean checked) {
		return checked ? checkedImg.getBounds().width : uncheckedImg.getBounds().width;
	}

	public int getPreferredHeight(boolean checked) {
		return checked ? checkedImg.getBounds().height : uncheckedImg.getBounds().height;
	}

	@Override
	public void paintCell(ILayerCell cell, GC gc, Rectangle bounds,
			IConfigRegistry configRegistry) {
		super.paintCell(cell, gc, bounds, configRegistry);
		Image image = getImage(cell, configRegistry);
		if (image != null) {
			Point p = gc.stringExtent(this.sText);
			Rectangle imageBounds = image.getBounds();
			IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
			gc.setFont(GUIHelper.DEFAULT_FONT);
			gc.setForeground(GUIHelper.COLOR_BLACK);
			
			int xOffset = ( bounds.width -(p.x + imageBounds.width)) / 2;
			if ( xOffset < 0 )
				xOffset = 0;
			int imageX = bounds.x + xOffset;
			int imageY = bounds.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, bounds, imageBounds.height);
			
			gc.drawImage(image, imageX, imageY );
			gc.drawText(this.sText, imageX + imageBounds.width, 
					imageY - CellStyleUtil.getVerticalAlignmentPadding(cellStyle, bounds, imageBounds.height), true);
		}
	}
		
	public void paintIconImage(GC gc, Rectangle rectangle, int yOffset, boolean checked) {
		Image checkBoxImage = checked ? checkedImg : uncheckedImg;
		// Center image
		int x = rectangle.x + (rectangle.width / 2) - (checkBoxImage.getBounds().width/2);
		gc.drawImage(checkBoxImage, x, rectangle.y + yOffset);
	}

	protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
		return curImage;
	}

	public Image getCurCheckboxImage() {
		return curImage;
	}
	
	protected boolean isChecked(ILayerCell cell, IConfigRegistry configRegistry) {
		return convertDataType(cell, configRegistry).booleanValue();
	}

	protected Boolean convertDataType(ILayerCell cell,
			IConfigRegistry configRegistry) {
		return Boolean.valueOf(getCurStatus());
	}
	
}
