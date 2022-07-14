package org.grits.toolbox.entry.ms.annotation.adaptor;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.utilShare.CopyUtils;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.widgets.processDialog.ProgressDialog;

import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.entry.ms.annotation.dialog.MSAnnotationExportDialog.ExportTypes;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.io.ms.annotation.process.export.MSAnnotationExportByonicProcess;
import org.grits.toolbox.io.ms.annotation.process.export.MSAnnotationExportDatabaseProcess;
import org.grits.toolbox.io.ms.annotation.process.export.MSAnnotationExportProcess;

/**
 * MSAnnotation File copy 
 * @author kitaemyoung
 *
 */
public class MSAnnotationExportFileAdapter extends SelectionAdapter {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSAnnotationExportFileAdapter.class);

	protected Shell shell = null;
	protected String fileExtension = null;
	protected Entry msAnnotationEntry = null;
	protected String sOutputFile = null;
	protected ExportTypes exportType;
	protected MSAnnotationTableDataObject tableDataObject = null;
	protected int iMasterParentScan = -1; // if set, use this when determining if a row is hidden
	protected int m_lastVisibleColInx = -1;

	protected String filterColumn;
	protected int numTopHits;
	protected double thresholdValue;
	
	protected MSAnnotationProperty getProperty() {
		MSAnnotationEntityProperty entityProp = (MSAnnotationEntityProperty)this.msAnnotationEntry.getProperty();
		MSAnnotationProperty property = (MSAnnotationProperty) entityProp.getParentProperty();
		return property;
	}
	
	// This is needed because of the way we create dialog titles
	protected String getValidFileName( String _sFileName ) {
		String sNewString = _sFileName.replaceAll("\\:", "");
		sNewString = sNewString.replaceAll("\\[", "(");
		sNewString = sNewString.replaceAll("\\]", ")");
		sNewString = sNewString.replaceAll("\\>", "-");
		return sNewString;
	}

	protected String getId() {
		MSAnnotationProperty property = getProperty();
		String id = property.getMSAnnotationMetaData().getAnnotationId();
		return id;		
	}

	public String getArchiveFolder() {
		MSAnnotationProperty property = getProperty();
		String folder = property.getArchiveFolder();
		return folder;		
	}
	
	public String getArchiveExtension() {
		MSAnnotationProperty property = getProperty();
		String folder = property.getArchiveExtension();
		return folder;		
	}
	
	protected String getFullyQualifiedArchivePath() {
		String workspaceLocation = PropertyHandler.getVariable("workspace_location");
		String projectName = DataModelSearch.findParentByType(msAnnotationEntry, ProjectProperty.TYPE).getDisplayName();
		String id = getId();
		String from = workspaceLocation+projectName + File.separator + getArchiveFolder() + File.separator + id +fileExtension;
		return from;
	}
	
	@Override
	public void widgetSelected(SelectionEvent event) 
	{
		FileDialog dlg = new FileDialog(shell,SWT.SAVE);
		String sFileName = getValidFileName(msAnnotationEntry.getDisplayName()+fileExtension);
		dlg.setFileName(sFileName);
		dlg.setFilterExtensions(new String[] {"*" + fileExtension});
		dlg.setText("File Explorer");

		boolean bDone = false;
		while( ! bDone ) {
			sOutputFile = dlg.open();
			try {
				if (sOutputFile != null) {
					int iRes = SWT.OK;
					File f = new File(sOutputFile);
					if( f.exists() ) {
						String sEMsg = (exportType == ExportTypes.Archive) ? 
								"The selected export project path exists." : "The selected export file exists.";
						iRes = ErrorUtils.createMultiConfirmationMessageBoxReturn(
								Display.getCurrent().getActiveShell(), 
								sEMsg, "Overwrite?", false);

					}

					if( iRes == SWT.OK ) {
						if(exportType == ExportTypes.Archive)
						{
							String sPath = getFullyQualifiedArchivePath();
							CopyUtils.copyFilesFromTo(sPath,sOutputFile);
						}
						else if ( exportType == ExportTypes.Excel )
						{
							exportExcel();
						} else if (exportType == ExportTypes.Byonic) {
							exportByonic();
						} else if (exportType == ExportTypes.Database){
							exportDatabase();
						}
					}
					//close
					if( ! (iRes == SWT.NO) ) {
						bDone = true;
						shell.close();
					}
				} else {
					bDone = true;					
				}
			} catch (NullPointerException e)
			{
				//delete files that were created!
				logger.error(e.getMessage(),e);
				ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Unable to save file",e);

			} catch (IOException e) {
				//delete files that were created!
				logger.error(e.getMessage(),e);
				ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Unable to save file",e);
			} catch (JAXBException e) {
				//delete files that were created!
				logger.error(e.getMessage(),e);
				ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Unable to save file",e);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e.getMessage(),e);
				ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Unable to save file",e);
			} 
		}
	}

	private void exportDatabase() {
		// open dialog to get name, description and version for the database
		DatabaseDialog databaseDialog = new DatabaseDialog(this.shell);
		if (databaseDialog.open() == Window.OK) {
			//create progress dialog for processing export
			ProgressDialog t_dialog = new ProgressDialog(this.shell);
			//fill parameter
			MSAnnotationExportDatabaseProcess t_worker = getNewExportDatabaseProcess();
			t_worker.setOutputFile(sOutputFile);
			t_worker.setDbName(databaseDialog.getName());
			t_worker.setDescription(databaseDialog.getDescription());
			t_worker.setVersion(databaseDialog.getVersion());
			t_worker.setTableDataObject(getTableDataObject());
			t_worker.setMasterParentScan(getMasterParentScan());
			t_worker.setLastVisibleColInx(getLastVisibleColInx());
			//set the worker
			t_dialog.setWorker(t_worker);
	
			//check Cancel
			if(t_dialog.open() != SWT.OK)
			{
				//delete the file
				new File(sOutputFile).delete();
			}
		}
	}

	protected MSAnnotationExportDatabaseProcess getNewExportDatabaseProcess() {
		return new MSAnnotationExportDatabaseProcess();
	}

	protected void exportByonic() throws IOException, Exception {
		//create progress dialog for processing export
		ProgressDialog t_dialog = new ProgressDialog(this.shell);
		//fill parameter
		MSAnnotationExportByonicProcess t_worker = getNewExportByonicProcess();
		t_worker.setOutputFile(sOutputFile);
		t_worker.setTableDataObject(getTableDataObject());
		t_worker.setMasterParentScan(getMasterParentScan());
		t_worker.setLastVisibleColInx(getLastVisibleColInx());
		//set the worker
		t_dialog.setWorker(t_worker);

		//check Cancel
		if(t_dialog.open() != SWT.OK)
		{
			//delete the file
			new File(sOutputFile).delete();
		}
	}

	protected MSAnnotationExportByonicProcess getNewExportByonicProcess() {
		return new MSAnnotationExportByonicProcess();
	}

	protected MSAnnotationExportProcess getNewExportProcess() {
		return new MSAnnotationExportProcess();
	}

	protected void exportExcel() throws IOException, Exception {
		//create progress dialog for copying files
		ProgressDialog t_dialog = new ProgressDialog(this.shell);
		//fill parameter
		MSAnnotationExportProcess t_worker = getNewExportProcess();
		t_worker.setOutputFile(sOutputFile);
		t_worker.setTableDataObject(getTableDataObject());
		t_worker.setMasterParentScan(getMasterParentScan());
		t_worker.setLastVisibleColInx(getLastVisibleColInx());
		//set the worker
		t_dialog.setWorker(t_worker);

		//check Cancel
		if(t_dialog.open() != SWT.OK)
		{
			//delete the file
			new File(sOutputFile).delete();
		}
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

	public void setLastVisibleColInx(int m_lastVisibleColInx) {
		this.m_lastVisibleColInx = m_lastVisibleColInx;
	}
	
	public int getLastVisibleColInx() {
		return m_lastVisibleColInx;
	}
	
	public Shell getShell() {
		return shell;
	}

	public void setShell(Shell shell) {
		this.shell = shell;
	}

	public void setMSAnnotationEntry(Entry msannotationEntry) {
		this.msAnnotationEntry = msannotationEntry;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}
	public String getDir() {
		return sOutputFile;
	}

	public void setExportType(ExportTypes exportType) {
		this.exportType = exportType;
	}

	public void setFilterColumn(String filterKey) {
		this.filterColumn = filterKey;
		
	}

	public void setTopHits(int numTopHits) {
		this.numTopHits = numTopHits;
		
	}

	public void setThresholdValue(double thresholdValue) {
		this.thresholdValue = thresholdValue;
	}
}
