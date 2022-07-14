package org.grits.toolbox.entry.ms.annotation.command;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.IMSAnnotationPeaksViewer;


/**
 * Execute ViewRowChooserInTabCommand
 * @author dbrentw
 *
 */
public class ViewRowChooserInTabCommand implements IHandler {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(ViewRowChooserInTabCommand.class);

	private String sSourceParentRowId;
	private int iSourceRowIndex;
	private int iSourceParentScanNum;
	private IMSAnnotationPeaksViewer parentView = null;
	private MSAnnotationTable parentTable = null;
	
	public ViewRowChooserInTabCommand(IMSAnnotationPeaksViewer parentView, 
			MSAnnotationTable parentTable, int iSourceRowIndex, int iSourceParentScanNum, String sSourceParentRowId) {
		this.sSourceParentRowId = sSourceParentRowId;
		this.iSourceRowIndex = iSourceRowIndex;
		this.iSourceParentScanNum = iSourceParentScanNum;
		this.parentView = parentView;
		this.parentTable = parentTable;
	}
	
	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
//			MSAnnotationSelectionView view = new MSAnnotationSelectionView(this.parent);
			if ( this.parentView.getCurrentSelectionView() == null ) {
				this.parentView.initNewSelectionView();
				this.parentView.getCurrentSelectionView().setParams(this.parentTable, iSourceRowIndex, iSourceParentScanNum, sSourceParentRowId);
				this.parentView.getCurrentSelectionView().createPartControl(this.parentView.getSelectionArea());
				this.parentView.getCurrentSelectionView().updateTable();
				this.parentView.getCurrentSelectionView().createView();
			}
			else if ( ! this.parentView.getCurrentSelectionView().isOpen(parentTable, iSourceParentScanNum, sSourceParentRowId) ) {
				this.parentView.getCurrentSelectionView().setParams(parentTable, iSourceRowIndex, iSourceParentScanNum, sSourceParentRowId);
				boolean bNeedCreate = this.parentView.getCurrentSelectionView().updateTable();
			}
			return this.parentView.getCurrentSelectionView();
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			e.printStackTrace();
//			ErrorUtils.createErrorMessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), e.getMessage(),e);
		}
		return null;
	}

	

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isHandled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub
		
	}

}
