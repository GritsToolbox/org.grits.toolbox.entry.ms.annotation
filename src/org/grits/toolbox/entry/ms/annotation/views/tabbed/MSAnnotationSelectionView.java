package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.grits.toolbox.display.control.table.tablecore.GRITSTable;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;

public class MSAnnotationSelectionView  {
	private static final Logger logger = Logger.getLogger(MSAnnotationSelectionView.class);
	protected MSAnnotationTable parentTable = null;
	protected String sParentRowId;
	protected int iParentRowIndex;
	protected int iParentScanNum;
	
	protected MSAnnotationTable subTable = null;
	protected Composite parent = null;
	protected MyPaintListener paintListener = null;

	public MSAnnotationSelectionView( Composite parent ) {
		super();
		this.parent = parent;
	}
	
	@Override
	public String toString() {
		return "MSAnnotationSelectionView (" + parentTable + ")";
	}
	
	public boolean isOpen( GRITSTable parentTable, int iParentScanNum, String sParentRowId ) {
		return this.parentTable != null && 
			   this.parentTable.getID() == parentTable.getID() && 
			   this.sParentRowId.equals(sParentRowId) && 
			   this.iParentScanNum == iParentScanNum;
	}
	
	public int getParentRowIndex() {
		return iParentRowIndex;
	}

	public String getParentPeakId() {
		return sParentRowId;
	}
	
	public int getParentScanNum() {
		return iParentScanNum;
	}
	
	public MSAnnotationTable getSubTable() {
		return subTable;
	}
	
	public void setSubTable(MSAnnotationTable subTable) {
		this.subTable = subTable;
	}
		
	public void setParams( MSAnnotationTable parentTable, int iParentRowIndex, int iParentScanNum, String sParentRowId ) {
		this.parentTable = parentTable;
		this.sParentRowId = sParentRowId;
		this.iParentRowIndex = iParentRowIndex;
		this.iParentScanNum = iParentScanNum;
	}
	
	@PostConstruct
	public void createPartControl(Composite parent) {
		this.parent = parent; // don't actually create the part yet, will be recalled	
		//has to be gridLayout, since it extends TitleAreaDialog
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.verticalSpacing = 10;
		parent.setLayout(gridLayout);	
		// Custom Action for the View's Menu  
		/*
		IAction lCustomAction = new CustomAction();
		lCustomAction.setText("Open Dialog Box");  
		lCustomAction.setImageDescriptor(Activator.getImageDescriptor("icons/Glycan_CFG.PNG"));  
		getViewSite().getActionBars().getToolBarManager().add(lCustomAction);		
		*/
	}

	public void createView() {
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		subTable.setLayoutData(gridData);			
	}
	
	protected void initTable() {
		subTable = new MSAnnotationTable(parent, parentTable, iParentRowIndex, iParentScanNum, sParentRowId);
		subTable.createSubsetTable();
		addListeners();
	}

	protected void addListeners() {
		paintListener = new MyPaintListener();
		subTable.addPaintListener(paintListener);
	}
	
	public boolean updateTable() {
		if ( subTable == null ) {
			initTable();
			return true;
		} else {					
			paintListener.reset();
			subTable.reInit(parent, parentTable, iParentRowIndex, iParentScanNum, sParentRowId);
			subTable.redraw();
			Listener[] listeners = subTable.getListeners(11);
			// bogus event is issued in order to get the scrollbar to reset to the new data
			if ( listeners.length >= 1 ) {
				Event e = new Event();
				listeners[0].handleEvent( e );
			}
 		}
		return false;
	}

	@Focus
	public void setFocus() {
		// TODO Auto-generated method stub
	}	
	
	class MyPaintListener implements PaintListener {
		boolean[] bValues = null;
		public void reset() {
			this.bValues = null;
		}
		
		@Override
		public void paintControl(PaintEvent e) {
			if ( bValues == null ) {
				this.bValues = initValues();
			} else if ( changed() ) {
				this.bValues = initValues();				
				boolean bDirty = parentTable.startUpdateHiddenRowsAfterEdit(subTable);
				parentTable.finishUpdateHiddenRowsAfterEdit(bDirty);
			}	
		}

		private boolean[] initValues() {
			boolean[] bValues = new boolean[ subTable.getBottomDataLayer().getRowCount() ];
			for (int i = 0; i < subTable.getBottomDataLayer().getRowCount(); i++ ) {
				Boolean bObj = (Boolean) subTable.getBottomDataLayer().getDataValueByPosition( 0, i);
				if ( bObj == null ) 
					bValues[i] = false;
				else
					bValues[i] = bObj.booleanValue();
			}
			return bValues;

		}

		private boolean changed() {
			for (int i = 0; i < subTable.getBottomDataLayer().getRowCount(); i++ ) {
				Boolean bObj = (Boolean) subTable.getBottomDataLayer().getDataValueByPosition( 0, i);					
				if ( bObj != null && bValues[i] != bObj.booleanValue() ) {
					return true;
				}
			}
			return false;

		}
	}		
	
	/*class CustomAction extends Action implements IWorkbenchAction {
		private static final String ID = "com.timmolter.helloWorld.CustomAction";  
		  
		public CustomAction(){  
		setId(ID);  
		}  
		  
		public void run() {  
		  
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();  
		String dialogBoxTitle = "Message";  
		String message = "You clicked something!";  
		MessageDialog.openInformation(shell, dialogBoxTitle, message);  
		  
		}  
		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}
		
	}*/

}
