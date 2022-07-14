package org.grits.toolbox.entry.ms.annotation.tablehelpers.gui;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnEntry;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.display.control.table.dialog.ColumnChooserDialog;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.display.control.table.tablecore.IGritsTable;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.tablehelpers.gui.MassSpecTableColumnChooser;

public class MSAnnotationTableColumnChooser extends MassSpecTableColumnChooser {

	public MSAnnotationTableColumnChooser(Shell shell,
			boolean sortAvailableColumns,
			boolean asGlobalPreference, 
			IGritsTable gritsTable) {
		super(shell, sortAvailableColumns, asGlobalPreference, gritsTable);
	}

	protected ColumnChooserDialog getNewColumnChooserDialog(Shell shell) {
		if ( asGlobalPreference ) 		
			columnChooserDialog = new ColumnChooserDialog(shell, Messages.getString("ColumnChooser.availableColumns"), Messages.getString("ColumnChooser.selectedColumns")); //$NON-NLS-1$ //$NON-NLS-2$
		else
			columnChooserDialog = new MSAnnotationColumnChooserDialog(shell, 
					Messages.getString("ColumnChooser.availableColumns"), 
					Messages.getString("ColumnChooser.selectedColumns"), this); //$NON-NLS-1$ //$NON-NLS-2$	
		
		return columnChooserDialog;
	}

	@Override
	protected TableViewerColumnSettings getDefaultSettings() {
		MSAnnotationTableDataProcessor proc = (MSAnnotationTableDataProcessor) getGRITSTable().getTableDataProcessor();
		TableViewerPreference newPref = proc.initializePreferences();
		TableViewerColumnSettings newSettings = newPref.getPreferenceSettings();
		MSAnnotationTableDataProcessor.setDefaultColumnViewSettings(proc.getSimianTableDataObject().getFillType(), newSettings);
		return  newSettings;
	}
	
	@Override
	public List<ColumnEntry> getVisibleColumnEntries() {
		List<ColumnEntry> columnEntries = super.getVisibleColumnEntries();
		if( columnEntries.get(0).getLabel().equals("Selected" ) ) {
			columnEntries.remove(0);	
		}
		return columnEntries;
	}
	
}
