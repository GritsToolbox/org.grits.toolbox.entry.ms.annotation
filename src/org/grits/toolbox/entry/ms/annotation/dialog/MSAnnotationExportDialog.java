package org.grits.toolbox.entry.ms.annotation.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.dialog.ModalDialog;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.entry.ms.annotation.adaptor.MSAnnotationExportFileAdapter;

/**
 * Enable to a user to download files related to Simian Entry
 * @author kitaemyoung
 *
 */
public class MSAnnotationExportDialog extends ModalDialog {

	protected String[] downloadOptions = {  "Export GRITS Archive file",
											"Export Excel file",
											"Export into Byonic database",
											"Export into a new GELATO database"};
	public enum ExportTypes {Excel, TSV, Archive, Byonic, Database};
	
	private Button OKbutton;
	private Entry msAnnotationEntry;
	protected List downloadlist;
	
	//private java.util.List<Filter> filterList = new ArrayList<>();
	private String sSelected;

	private MSAnnotationExportFileAdapter msAnnotationExportFileAdapter;
	private MSAnnotationTableDataObject tableDataObject = null;
	private int iMasterParentScan = -1; // if set, use this when determining if a row is hidden
	private int m_lastVisibleColInx = -1;

	protected Text txtOutput;
	//private Button hideUnannotatedRows;
	FilterDialog filtering;
	private Button btnFilteringSettings;
	
	public MSAnnotationExportDialog(Shell parentShell, MSAnnotationExportFileAdapter msAnnotationExportFileAdapter) {
		super(parentShell);
		this.msAnnotationExportFileAdapter = msAnnotationExportFileAdapter;
	}

	@Override
	public void create()
	{
		super.create();
		setTitle("Export");
		setMessage("Export an archive or excel file");
	}

	@Override
	protected Control createDialogArea(final Composite parent) 
	{
		//has to be gridLayout, since it extends TitleAreaDialog
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		gridLayout.verticalSpacing = 10;
		parent.setLayout(gridLayout);
		
		this.txtOutput = new Text(parent, SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY );
		this.txtOutput.setText("Please select export type and specify filter parameters.\nFilter parameters apply only to " + ExportTypes.Excel + "Export\n");
		this.txtOutput.setFont(boldFont);
		this.txtOutput.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		GridData gridDataTxtOutput = new GridData(GridData.FILL_HORIZONTAL);
		gridDataTxtOutput.horizontalSpan = 4;
		gridDataTxtOutput.verticalSpan = 2;
		this.txtOutput.setLayoutData(gridDataTxtOutput);

		/*
		 * First row starts:download list
		 */
		createList(parent);
		
		createFilters(parent);
		
		createButtonCancel(parent);
		
		createButtonOK(parent);

		return parent;
	}

	protected SelectionListener downloadlistListener = new SelectionListener() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (downloadlist.getSelectionIndex() != -1) {
				//enables the export button
				OKbutton.setEnabled(true);
				sSelected = downloadlist.getItem(downloadlist.getSelectionIndex()).toString();
	//			if(sSelected.equals(downloadOptions[0]))
	//			{
	//				msAnnotationExportFileAdapter.setFileExtension(".tsv");
	//				msAnnotationExportFileAdapter.setExportType(ExportTypes.TSV);
	//			}
				if(sSelected.equals(downloadOptions[0]))
				{
					msAnnotationExportFileAdapter.setFileExtension(msAnnotationExportFileAdapter.getArchiveExtension());
					msAnnotationExportFileAdapter.setExportType(ExportTypes.Archive);
					if (btnFilteringSettings != null)  // it may not exist if the filtering is not supported
						btnFilteringSettings.setEnabled(false);
				}
				else if(sSelected.equals(downloadOptions[1]))
				{
					msAnnotationExportFileAdapter.setFileExtension(".xls");
					msAnnotationExportFileAdapter.setExportType(ExportTypes.Excel);
					if (btnFilteringSettings != null) // it may not exist if the filtering is not supported
						btnFilteringSettings.setEnabled(true);
				}
				else if (sSelected.equals(downloadOptions[2])) {
					msAnnotationExportFileAdapter.setFileExtension(".txt");
					msAnnotationExportFileAdapter.setExportType(ExportTypes.Byonic);
					if (btnFilteringSettings != null) // it may not exist if the filtering is not supported
						btnFilteringSettings.setEnabled(false);
				}
				else if (sSelected.equals(downloadOptions[3])) {
					msAnnotationExportFileAdapter.setFileExtension(".xml");
					msAnnotationExportFileAdapter.setExportType(ExportTypes.Database);
					if (btnFilteringSettings != null) // it may not exist if the filtering is not supported
						btnFilteringSettings.setEnabled(false);
				}
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};	
	
	protected void createList(Composite parent2) {
		downloadlist = new List(parent2, SWT.SINGLE);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 4;
		gridData.verticalSpan = 1;
		downloadlist.setLayoutData(gridData);
		//add data to list
		setDownloadOptions(downloadlist);
		//add listener
		downloadlist.addSelectionListener(downloadlistListener);
	}

	protected void setDownloadOptions(List downloadlist) {
		downloadlist.add(downloadOptions[0]);
		downloadlist.add(downloadOptions[1]);
		downloadlist.add(downloadOptions[2]);
		downloadlist.add(downloadOptions[3]);
	}

	protected void createFilters(Composite parent) {
	/*	hideUnannotatedRows = new Button (parent, SWT.CHECK);
		hideUnannotatedRows.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, false, false, 1, 1));
		MSAnnotationViewerPreference settings = (MSAnnotationViewerPreference) getTableDataObject().getTablePreferences();
		hideUnannotatedRows.setSelection(settings.getHideUnannotatedPeaks());
	
		Label hideUnAnnotatedLabel = new Label (parent, SWT.NONE);
		hideUnAnnotatedLabel.setText("Hide UnAnnotated Peaks");
		hideUnAnnotatedLabel.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, false, false, 3, 1));*/
		
		
		
		btnFilteringSettings = new Button(parent, SWT.NONE);
		btnFilteringSettings.setEnabled(false);
		filtering = getNewFilterDialog();
        btnFilteringSettings.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (filtering != null) {
                	filtering.open();                
                } 
            }
        });
        btnFilteringSettings.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
        btnFilteringSettings.setText("Filtering Settings");
	}
	
	protected FilterDialog getNewFilterDialog () {
		return new FilterDialog(getParentShell());
	}
	

	@Override
	protected Button createButtonOK(final Composite parent2) {
		//create a gridData for OKButton
		GridData okData = new GridData(SWT.END);
		//okData.grabExcessHorizontalSpace = true;
		okData.horizontalSpan = 2;
		okData.widthHint = 100;
		OKbutton = new Button(parent2, SWT.PUSH);
		OKbutton.setText("Export");
		//add export file adaptor
		msAnnotationExportFileAdapter.setShell(parent2.getShell());
		msAnnotationExportFileAdapter.setMSAnnotationEntry(this.msAnnotationEntry);
		msAnnotationExportFileAdapter.setTableDataObject(this.tableDataObject);
		msAnnotationExportFileAdapter.setMasterParentScan(getMasterParentScan());
		msAnnotationExportFileAdapter.setLastVisibleColInx(this.m_lastVisibleColInx);
		msAnnotationExportFileAdapter.setFileExtension("");
		OKbutton.addSelectionListener(msAnnotationExportFileAdapter);
		OKbutton.setLayoutData(okData);
		OKbutton.setEnabled(false);
		
		if (filtering != null) filtering.setExportFileAdapter (msAnnotationExportFileAdapter);
		return OKbutton;
	}

	@Override
	protected boolean isValidInput() {
		return true;
	}

	@Override
	protected Entry createEntry() {
		return msAnnotationEntry;
	}

	public void setMSAnnotationEntry(Entry msAnnotationEntry) {
		this.msAnnotationEntry = msAnnotationEntry;
	}

	public void setTableDataObject( MSAnnotationTableDataObject tableDataObject ) {
		this.tableDataObject = tableDataObject;
	}
	
	public MSAnnotationTableDataObject getTableDataObject() {
		return tableDataObject;
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
}
