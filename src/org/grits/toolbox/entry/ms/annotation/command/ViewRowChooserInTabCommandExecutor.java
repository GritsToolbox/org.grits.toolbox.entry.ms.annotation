package org.grits.toolbox.entry.ms.annotation.command;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.IMSAnnotationPeaksViewer;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationSelectionView;

public class ViewRowChooserInTabCommandExecutor  {
	public static MSAnnotationSelectionView showRowChooser( IMSAnnotationPeaksViewer parentView, 
													MSAnnotationTable parentTable, int iRowIndex, int iParentScanNum, String sRowId ) {
		
		ViewRowChooserInTabCommand command = new ViewRowChooserInTabCommand(parentView, parentTable, iRowIndex, iParentScanNum, sRowId);
		
		try {
			return (MSAnnotationSelectionView) command.execute(new ExecutionEvent());
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return null;
	}

}
