package org.grits.toolbox.entry.ms.annotation.handler;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.adaptor.MSAnnotationExportFileAdapter;
import org.grits.toolbox.entry.ms.annotation.dialog.MSAnnotationExportDialog;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;

/**
 * Export command. call SimianExportDialog.
 * 
 * @author kitaemyoung
 * 
 */
public class MSAnnotationExportCommand {
	private static final Logger logger = Logger.getLogger(MSAnnotationExportCommand.class);

	private Entry entry = null;
	private MSAnnotationTableDataObject tableDataObject = null;
	private int iMasterParentScan = -1; // if set, use this when determining if a row is hidden
	private int m_lastVisibleColInx = -1;
	

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_PART) MPart part,
			@Named (IServiceConstants.ACTIVE_SHELL) Shell shell,
			EPartService partService) {

		if (initialize(part, partService)) {
			// need to show a dialog which contains three elements in a list:
			// SimGlycanCSV, XML, and load data into XML to export
			createSimianExportDialog(shell);
		} else {
			logger.warn("A valid MS Annotation entry must be open and active in order to export.");
			// need to show dialog saying please choose a simGlycanEntry
			ErrorUtils.createWarningMessageBox(
					shell, "Invalid Entry",	"An appropriate MS Annotation entry must be open and active in order to export.");
		}
	}

	protected MSAnnotationExportFileAdapter getNewExportAdapter() {
		MSAnnotationExportFileAdapter adapter = new MSAnnotationExportFileAdapter();
		return adapter;
	}
	
	protected boolean initialize(MPart part, EPartService partService) {
		try {
			MSAnnotationMultiPageViewer viewer = null;
			if (part != null && part.getObject() instanceof MSAnnotationMultiPageViewer) {
				viewer = (MSAnnotationMultiPageViewer) part.getObject();
			} else { // try to find an open part of the required type
				for (MPart mPart: partService.getParts()) {
					if (mPart.getObject() instanceof MSAnnotationMultiPageViewer) {
						if (mPart.equals(mPart.getParent().getSelectedElement())) {
							viewer = (MSAnnotationMultiPageViewer) mPart.getObject();
							if (!viewer.getPeaksView().isEmpty())
								break;
						}
					}
				}
			}
			if (viewer != null) {
				if( viewer.getPeaksView().isEmpty() ) {
					return false;
				}
				setEntry(viewer.getEntry());
				
				// for ms2/ms3 tables, peaksview is null, we cannot export them indivudally!
				if (viewer.getPeaksView().get(0) == null || viewer.getPeaksView().get(0).getViewBase() == null) return false;
				
				MSAnnotationTableDataObject tdo = (MSAnnotationTableDataObject) viewer.getPeaksView().get(0).getViewBase().getNatTable().getGRITSTableDataObject();
				setTableDataObject(tdo);
				
				int iMasterParentScan = ((MSAnnotationTable) viewer.getPeaksView().get(0).getViewBase().getNatTable()).getScanNumberForVisibility(tdo, -1);
				setMasterParentScan(iMasterParentScan);
				
				TableDataProcessor processor = viewer.getPeaksView().get(0).getViewBase().getNatTable().getTableDataProcessor();
				setLastVisibleColInx( processor.getLastVisibleCol() );
				return true;
			} else {
				return false;
			}
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);
			return false;
		}		
	}
	
	protected void createSimianExportDialog(Shell activeShell) {
		MSAnnotationExportFileAdapter adapter = getNewExportAdapter();
		MSAnnotationExportDialog dialog = getNewExportDialog (activeShell, adapter);
		// set parent entry
		dialog.setMSAnnotationEntry(entry);
		dialog.setTableDataObject(getTableDataObject());
		dialog.setMasterParentScan(getMasterParentScan());
		dialog.setLastVisibleColInx(getLastVisibleColInx());
		if (dialog.open() == Window.OK) {
			// to do something..
		}
	}
	
	protected MSAnnotationExportDialog getNewExportDialog (Shell activeShell, MSAnnotationExportFileAdapter adapter) {
		return new MSAnnotationExportDialog(PropertyHandler.getModalDialog(activeShell), adapter);
	}
	
	public Entry getEntry() {
		return entry;
	}
	
	public void setEntry(Entry entry) {
		this.entry = entry;
	}
	

	public MSAnnotationTableDataObject getTableDataObject() {
		return tableDataObject;
	}
	
	public void setTableDataObject(MSAnnotationTableDataObject tableDataObject) {
		this.tableDataObject = tableDataObject;
	}
	
	public int getMasterParentScan() {
		return iMasterParentScan;
	}
	
	public void setMasterParentScan(int iMasterParentScan) {
		this.iMasterParentScan = iMasterParentScan;
	}	
	
	public int getLastVisibleColInx() {
		return m_lastVisibleColInx;
	}
	
	public void setLastVisibleColInx(int m_lastVisibleColInx) {
		this.m_lastVisibleColInx = m_lastVisibleColInx;
	}
	
	@CanExecute
	public boolean isEnabled(@Named(IServiceConstants.ACTIVE_PART) MPart part, EPartService partService) {
		return initialize(part, partService);
	}
}
