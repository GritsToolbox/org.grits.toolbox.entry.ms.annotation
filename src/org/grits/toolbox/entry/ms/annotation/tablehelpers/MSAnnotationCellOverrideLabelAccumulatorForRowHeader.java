package org.grits.toolbox.entry.ms.annotation.tablehelpers;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.tablehelpers.MassSpecCellOverrideLabelAccumulator;
import org.grits.toolbox.ms.om.data.Feature;

public class MSAnnotationCellOverrideLabelAccumulatorForRowHeader<T>
		extends MassSpecCellOverrideLabelAccumulator<T> {

	private MSAnnotationTable table;
	MSAnnotationMultiPageViewer viewer;

	public MSAnnotationCellOverrideLabelAccumulatorForRowHeader(IRowDataProvider<T> dataProvider, MSAnnotationTable msAnnotationTable, Integer selectedCol) {
		super(dataProvider, selectedCol, null);
		this.table = msAnnotationTable;
	}
	
	@Override
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		if (iSelectedCol != null && columnPosition == 0) { // row header
			// check if the row is locked or not
			Integer iPeakId = null;
			if (((MSAnnotationTableDataObject) table.getGRITSTableDataObject()).getPeakIdCols() != null && 
					!((MSAnnotationTableDataObject) table.getGRITSTableDataObject()).getPeakIdCols().isEmpty()) {
				Integer peakIdIdx = ((MSAnnotationTableDataObject) table.getGRITSTableDataObject()).getPeakIdCols().get(0);
				if (peakIdIdx != null && peakIdIdx != -1) {
					Object peakIdVal = table.getBottomDataLayer().getDataValueByPosition( 
							((MSAnnotationTableDataObject) table.getGRITSTableDataObject()).getPeakIdCols().get(0), rowPosition);
					if (peakIdVal != null && peakIdVal instanceof Integer)
						iPeakId = (Integer) peakIdVal;
				}
			}
			Integer iScanNum = null;
			if (((MSAnnotationTableDataObject) table.getGRITSTableDataObject()).getScanNoCols() != null && 
					!((MSAnnotationTableDataObject) table.getGRITSTableDataObject()).getScanNoCols().isEmpty()) {
				Integer scanNumIdx = ((MSAnnotationTableDataObject) table.getGRITSTableDataObject()).getScanNoCols().get(0);
				if (scanNumIdx != null && scanNumIdx != -1) {
					Object scanNumVal = table.getBottomDataLayer().getDataValueByPosition( 
							((MSAnnotationTableDataObject) table.getGRITSTableDataObject()).getScanNoCols().get(0), rowPosition);
					if (scanNumVal != null && scanNumVal instanceof Integer)
						iScanNum = (Integer) scanNumVal;
				}
			}
			Integer parentScanNo = null;
			MSAnnotationTableDataObject tdo = ((MSAnnotationTableDataObject) table.getGRITSTableDataObject());
			if (table.getParentView().getEntityProperty() != null && table.getParentView().getEntityProperty() instanceof MSAnnotationEntityProperty) {
				if (((MSAnnotationEntityProperty)table.getParentView().getEntityProperty()).getMsLevel() > 2) {
					//MSn - get parent scan number from the table
					if ( tdo.getParentNoCol() != null && ! tdo.getParentNoCol().isEmpty()) {
						if( tdo.getTableData().get(0).getDataRow().get( tdo.getParentNoCol().get(0)) != null ) {
							parentScanNo = (Integer) table.getBottomDataLayer().getDataValueByPosition(
									tdo.getParentNoCol().get(0), rowPosition);
						}
					}
				}
			}
			if (parentScanNo == null)
				parentScanNo = table.getScanNumberForVisibility(table, rowPosition);
			
			if (parentScanNo != null && iPeakId != null) {
				String sRowId = Feature.getRowId(iPeakId, iScanNum, tdo.getUsesComplexRowId());
				boolean isLocked = table.getGRITSTableDataObject().isLockedPeak(parentScanNo, sRowId);
				// if locked, add lockedLabel
				if (isLocked) {
					configLabels.addLabel(MSAnnotationTable.LOCKSELECTION_LABEL);
				} 
			}
		}
		
	}

}
