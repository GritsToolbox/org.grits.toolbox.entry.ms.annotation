 package org.grits.toolbox.entry.ms.annotation.tablehelpers.gui;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnEntry;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.display.control.table.dialog.GRITSTableColumnChooser;
import org.grits.toolbox.entry.ms.tablehelpers.gui.MassSpecColumnChooserDialog;

public class MSAnnotationColumnChooserDialog extends MassSpecColumnChooserDialog {
	
	public MSAnnotationColumnChooserDialog(Shell parentShell,
			String availableLabel, String selectedLabel, GRITSTableColumnChooser colChooser) {
		super(parentShell, availableLabel, selectedLabel, colChooser);
	}
	
	@Override
	public void populateAvailableTree(List<ColumnEntry> columnEntries,
			ColumnGroupModel columnGroupModel) {
		super.populateAvailableTree(columnEntries, columnGroupModel);
	}
}
	