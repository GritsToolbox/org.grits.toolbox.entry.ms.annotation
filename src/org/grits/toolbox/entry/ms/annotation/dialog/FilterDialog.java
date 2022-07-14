package org.grits.toolbox.entry.ms.annotation.dialog;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMPeak;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMPrecursorPeak;
import org.grits.toolbox.entry.ms.annotation.adaptor.MSAnnotationExportFileAdapter;

public class FilterDialog extends TitleAreaDialog {
	
	private static final Logger logger = Logger.getLogger(FilterDialog.class);
	private static final String PARAMS_OK = "Valid parameters";

	Integer numTopHits;
	Double thresholdValue;
	String filterKey;

	private Combo filterCombo;
	private Text txtNumTopHits;
	private Text txtThresholdValue;
	private Label txtOutput;
	protected MSAnnotationExportFileAdapter exportFileAdapter;
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public FilterDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Filtering Settings");
		Composite area = (Composite) super.createDialogArea(parent);
		
		ScrolledComposite sc = new ScrolledComposite(area, SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.BORDER);
		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 400;
		data.widthHint = 700;
		sc.setLayoutData(data);
		
		Composite otherFilterArea = new Composite (sc, SWT.NONE);
		otherFilterArea.setLayout(new GridLayout(4, false));
		
		createFilterTable (otherFilterArea);
		
		this.txtOutput = new Label(otherFilterArea, SWT.READ_ONLY );
		this.txtOutput.setText("\n\nPlease select other Filter Options\n");
		//this.txtOutput.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		GridData gridDataTxtOutput = new GridData(GridData.FILL_HORIZONTAL);
		gridDataTxtOutput.horizontalSpan = 4;
		gridDataTxtOutput.verticalSpan = 2;
		this.txtOutput.setLayoutData(gridDataTxtOutput);
		this.txtOutput.setEnabled(false);
		
		GridData gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.verticalSpan = 1;
		Label lblFilter = new Label(otherFilterArea, SWT.NONE);
		lblFilter.setText("Choose a criterion to use as filter");
		lblFilter.setLayoutData(gridData);
		
		createFilterOptions(otherFilterArea);
		
		Label lblNumTopHits = new Label(otherFilterArea, SWT.NONE);
		lblNumTopHits.setText("Number of Top Hits to Select: ");
		GridData gridData2 = new GridData(GridData.FILL_HORIZONTAL);
		gridData2.horizontalSpan = 1;
		gridData2.verticalSpan = 1;
		lblNumTopHits.setLayoutData(gridData2);
		txtNumTopHits = new Text(otherFilterArea, SWT.BORDER);
		txtNumTopHits.setText("All");
		GridData gridData4 = new GridData(GridData.FILL_HORIZONTAL);
		gridData4.horizontalSpan = 3;
		gridData4.verticalSpan = 1;
		txtNumTopHits.setLayoutData(gridData4);
		if (numTopHits != null) {
			txtNumTopHits.setText(numTopHits + ""); 
		}
		txtNumTopHits.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});
		
		
		Label lblThreshold = new Label (otherFilterArea, SWT.NONE);
		lblThreshold.setText("Threshold Value: ");
		lblThreshold.setToolTipText("Get only those whose selected filter criterion is greater than the threshold");
		GridData gridData5 = new GridData(GridData.FILL_HORIZONTAL);
		gridData5.horizontalSpan = 1;
		gridData5.verticalSpan = 1;
		lblThreshold.setLayoutData(gridData5);
		txtThresholdValue = new Text (otherFilterArea, SWT.BORDER);
		txtThresholdValue.setText("");
		gridData5 = new GridData(GridData.FILL_HORIZONTAL);
		gridData5.horizontalSpan = 3;
		gridData5.verticalSpan = 1;
		txtThresholdValue.setLayoutData(gridData5);
		new Label(otherFilterArea, SWT.NONE);
		new Label(otherFilterArea, SWT.NONE);
		if (thresholdValue != null)
			txtThresholdValue.setText(this.thresholdValue + "");
		txtThresholdValue.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});
		
		sc.setContent(otherFilterArea);
		sc.setMinSize(otherFilterArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return area;
	}
	
	/**
	 * this needs to be overridden by the subclasses to add a filter table 
	 * @param parent
	 */
	protected void createFilterTable (Composite parent) {
	}
	
	private void createFilterOptions(Composite parent) {
		if (this.filterCombo != null && !this.filterCombo.isDisposed())
			this.filterCombo.removeAll();
		else {			
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 3;
			gridData.verticalSpan = 1;
			filterCombo = new Combo(parent, SWT.SINGLE);
			filterCombo.setLayoutData(gridData);
		}
		filterCombo.add("");
		filterCombo.setEnabled(false);
		
		filterCombo.add(DMPeak.peak_intensity.getLabel());
		filterCombo.add(DMPrecursorPeak.precursor_peak_intensity.getLabel());
		if (filterKey != null) {
			filterCombo.select(filterCombo.indexOf(filterKey));
		}
		
		if (filterCombo.getItemCount() > 1) {
			filterCombo.setEnabled(true);
			filterCombo.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					validateInput();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
	}
	
	public int getNumTopHits() throws NumberFormatException {
		if( this.txtNumTopHits.getText().equals("All") ) {
			return -1;
		}
		
		return Integer.parseInt(this.txtNumTopHits.getText());
				
	}
	
	public String getFilterKey() {
		if( ! this.filterCombo.getText().equals("") ) {
			return filterCombo.getText();		
		}
		return null;
	}
	
	public double getThresholdValue() throws NumberFormatException {
		if (this.txtThresholdValue.getText().equals("")) {
			return 0.0;
		}
		double threshold = Double.parseDouble(txtThresholdValue.getText());
		return threshold;
	}
	
	public void validateInput(){
		txtOutput.setText(PARAMS_OK);
		boolean error = false;
		try {
			numTopHits = getNumTopHits();
		} catch (NumberFormatException e) {
			error = true;
			txtOutput.setText("Invalid value for 'Num Top Hits'. Please enter 'All' or an integer greater than 0.");	
		}
		if (!error && numTopHits > 0 && (filterKey = getFilterKey()) == null) {
			txtOutput.setText("Please select a filter criterion");
		} else {
			try {
				thresholdValue = getThresholdValue();
				if (getFilterKey() == null) {
					txtOutput.setText("Please select a filter criterion");
				}
			} catch (NumberFormatException e) {
				txtOutput.setText("Invalid Value for 'Threshold Value'. Please enter a number");
			}
		}
		if (txtOutput.getText().equals(PARAMS_OK)) {
			getButton(IDialogConstants.OK_ID).setEnabled(true);
		} else {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
	}
	
	@Override
	protected void okPressed() {
		if (this.exportFileAdapter != null) {
			this.exportFileAdapter.setFilterColumn(getFilterKey());
			this.exportFileAdapter.setTopHits(getNumTopHits());
			this.exportFileAdapter.setThresholdValue(getThresholdValue());
		}
		super.okPressed();
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(700, 450);
	}

	

	public void setExportFileAdapter(MSAnnotationExportFileAdapter msAnnotationExportFileAdapter) {
		this.exportFileAdapter = msAnnotationExportFileAdapter;
		
	}
}
