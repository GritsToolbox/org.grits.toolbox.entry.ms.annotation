package org.grits.toolbox.entry.ms.annotation.tablehelpers;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.dmtranslate.DMAnnotation;
import org.grits.toolbox.display.control.table.datamodel.GRITSListDataProvider;
import org.grits.toolbox.display.control.table.datamodel.GRITSListDataRow;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.display.control.table.tablecore.DoubleFormat;
import org.grits.toolbox.display.control.table.tablecore.GRITSTable;
import org.grits.toolbox.entry.ms.tablehelpers.MassSpecCellOverrideLabelAccumulator;
import org.grits.toolbox.entry.ms.tablehelpers.MassSpecTable;

public class MSAnnotationCellOverrideLabelAccumulator<T> extends
		MassSpecCellOverrideLabelAccumulator<T> {

	protected Integer filterCol = null;
	protected Integer commentCol = null;
	protected Integer ratioCol = null;
	
	public MSAnnotationCellOverrideLabelAccumulator(IRowDataProvider<T> dataProvider) {
		super(dataProvider);
		this.iSelectedCol = null;
		this.filterCol = null;
		this.commentCol = null;
		this.ratioCol = null;
	}

	public MSAnnotationCellOverrideLabelAccumulator(IRowDataProvider<T> dataProvider, Integer iSelectedCol, Integer filterCol, Integer commentCol, Integer ratioCol, List<Integer> intensityCols) {
		super(dataProvider, iSelectedCol, intensityCols);
		this.filterCol= filterCol;
		this.commentCol = commentCol;
		this.ratioCol = ratioCol;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		
		GRITSListDataRow rowObj =  ((GRITSListDataProvider) dataProvider).getGRITSListDataRow(rowPosition);
		if ( rowObj == null ) 
			return;
		
		if (filterCol != null) {
			Integer filterVal = (Integer) rowObj.getDataRow().get(filterCol);
			if (filterVal != null && filterVal > 1) { // MATCH and selected
				configLabels.addLabel(GRITSTable.FILTEREDSELECTED);
			} else if (filterVal != null && filterVal.equals(1)) { // there is a MATCH in candidates
				configLabels.addLabel(GRITSTable.FILTEREDNOTSELECTED);
			}
		}
		
		if (commentCol != null && columnPosition == commentCol) {
			configLabels.addLabel(MSAnnotationTable.EDITORCONFIG_LABEL + TableDataProcessor.commentColHeader.getKeyValue());
		}
		
		if (ratioCol != null && columnPosition == ratioCol) {
			configLabels.addLabel(MSAnnotationTable.EDITORCONFIG_LABEL + DMAnnotation.annotation_ratio.name());
		}
		
		if ( iSelectedCol != null && columnPosition == iSelectedCol ) {
			configLabels.addLabel( TableDataProcessor.selColHeader.getLabel() );
		}
		
		if (intensityCols != null && intensityCols.contains(columnPosition)) {
			configLabels.addLabel(DoubleFormat.SCIENTIFIC_NOTATION.name());
		}
		
		if (polarityCol != null && polarityCol == columnPosition) 
			configLabels.addLabel(MassSpecTable.POLARITYLABEL);
	}
}
