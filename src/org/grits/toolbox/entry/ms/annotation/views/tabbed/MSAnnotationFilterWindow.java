package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.ms.om.data.CustomExtraData;

public class MSAnnotationFilterWindow extends Dialog {
	private static final Logger logger = Logger.getLogger(MSAnnotationFilterWindow.class);
	protected final Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT); 
	protected Text txtOutput;
	protected static final String PARAMS_OK = "Valid parameters";
	protected MPart part;
	
	private Text txtMSGlyResult;
	private Entry msAnnotationEntry;

	private Label lblFilter;
	private Combo filterList;

	private Label lblNumTopHits;
	private Text txtNumTopHits;
	protected final static String ALL = "All";
	private Button btnOverrideManualAnnotations;
	
	private List<CustomExtraData> featureCustomExtraData;
	
	public MSAnnotationFilterWindow(Shell parentShell, Entry entry, MPart part) {
		super(parentShell);
		this.msAnnotationEntry = entry;
		this.part= part;
		setShellStyle(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);  // make it resizable
	}
	
	public void setFeatureCustomExtraData( List<CustomExtraData> featureCustomExtraData ) {
		this.featureCustomExtraData = featureCustomExtraData;
	}
	
	public List<CustomExtraData> getFeatureCustomExtraData() {
		return featureCustomExtraData;
	}
			
	@Override
	public int open() 
	{
		if( msAnnotationEntry == null ) {
			return -1;
		}
		setFeatureCustomExtraData( MSAnnotationTableDataProcessor.getMSAnnotationFeatureCustomExtraData(getMSAnnotationEntry()) );
		super.create();

		getShell().open();
		getShell().setText(getTitle());

		getShell().layout();
		getShell().pack();
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		return getReturnCode();
	}	

	protected String getTitle() {
		return "MS Annotation Filter";
	}
		
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite content = (Composite) super.createDialogArea(parent);
		//find the center of a main monitor
		Monitor primary = getShell().getDisplay().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = getShell().getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		getShell().setLocation(x, y);
		content.setLayout(new GridLayout(1, false));

        ScrolledComposite sc = new ScrolledComposite(content, SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.BORDER);
		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 400;
		data.widthHint = 700;
		sc.setLayoutData(data);
		
		Composite container = new Composite(sc, SWT.NONE);
		container.setLayout(new GridLayout(4, false));
		
		this.txtOutput = new Text(container, SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY );
		this.txtOutput.setText("Please specify filter parameters.\n\n");
		this.txtOutput.setFont(boldFont);
		this.txtOutput.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		GridData gridDataTxtOutput = new GridData(GridData.FILL_HORIZONTAL);
		gridDataTxtOutput.horizontalSpan = 4;
		gridDataTxtOutput.verticalSpan = 2;
		this.txtOutput.setLayoutData(gridDataTxtOutput);
		
		Label lblMsExperiment = new Label(container, SWT.NONE);
		lblMsExperiment.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		lblMsExperiment.setText("MS Annotation");
		
		txtMSGlyResult = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		txtMSGlyResult.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		if(getMSAnnotationEntry() != null){
			txtMSGlyResult.setText(getMSAnnotationEntry().getDisplayName());
		}				
		
		createFilterSection(container);	
		createScoreFilterSection(container);
		
		sc.setContent(container);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return parent;
	}

	protected String getAnnotationLabelText() {
		return "MS Annotation";
	}
	
	public int getNumTopHits() {
		if( this.txtNumTopHits != null && this.txtNumTopHits.getText().equals(ALL) ) {
			return -1;
		} else if (this.txtNumTopHits == null)  // treat as default, ALL
			return -1;
		try {
			return Integer.parseInt(this.txtNumTopHits.getText());
		} catch( NumberFormatException ex ) {	
			return -2;  // this will warn the user to enter a number
		}
	}

	public boolean getOverrideManualAnnotations() {
		if (btnOverrideManualAnnotations != null)
			return this.btnOverrideManualAnnotations.getSelection();
		return false;
	}
	
	public String getFilterKey() {
		if (filterList == null)
			return null;
		if( ! this.filterList.getText().equals("") ) {
			for (CustomExtraData cnd : getFeatureCustomExtraData()) {
				if( this.filterList.getText().equals(cnd.getLabel()) ) {
					return cnd.getKey();
				}
			}			
		}
		return null;
	}
	
	protected void createScoreFilterSection (Composite container) {
		GridData gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.verticalSpan = 1;
		this.lblFilter = new Label(container, SWT.NONE);
		this.lblFilter.setText("Choose a criterion to use as score filter");
		this.lblFilter.setLayoutData(gridData);
		
		MSAnnotationMultiPageViewer parentViewer = MSAnnotationMultiPageViewer.getActiveViewerForEntry(part.getContext(), getMSAnnotationEntry());
		
		createLists(parentViewer, container);
		
		lblNumTopHits = new Label(container, SWT.NONE);
		lblNumTopHits.setText("Number of Top Hits to Select: ");
		GridData gridData2 = new GridData(GridData.FILL_HORIZONTAL);
		gridData2.horizontalSpan = 1;
		gridData2.verticalSpan = 1;
		lblNumTopHits.setLayoutData(gridData2);
		txtNumTopHits = new Text(container, SWT.BORDER);
		txtNumTopHits.setText(ALL);
		GridData gridData4 = new GridData(GridData.FILL_HORIZONTAL);
		gridData4.horizontalSpan = 3;
		gridData4.verticalSpan = 1;
		txtNumTopHits.setLayoutData(gridData4);
		if (parentViewer != null && parentViewer.getFilter() != null) {
			if (parentViewer.getFilter().getNumTopHits() == -1)
				txtNumTopHits.setText("All");
			else
				txtNumTopHits.setText(String.valueOf(parentViewer.getFilter().getNumTopHits()));
			//txtNumTopHits.setEnabled(false);
		}
		txtNumTopHits.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		btnOverrideManualAnnotations = new Button(container, SWT.CHECK);
		btnOverrideManualAnnotations.setText("Override manually selected annotations");
		GridData gridData3 = new GridData(GridData.FILL_HORIZONTAL);
		gridData3.horizontalSpan = 4;
		gridData3.verticalSpan = 1;
		btnOverrideManualAnnotations.setLayoutData(gridData3);
	}
	
	private void createLists(MSAnnotationMultiPageViewer parentViewer, Composite parent) {
		if (this.filterList != null)
			this.filterList.removeAll();
		else {			
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 3;
			gridData.verticalSpan = 1;
			filterList = new Combo(parent, SWT.SINGLE);
			filterList.setLayoutData(gridData);
		}
		filterList.add("");
		filterList.setEnabled(false);
		if( getFeatureCustomExtraData() == null )
			return;
		int i=1;   // since the first one is empty we need to skip that one
		for (CustomExtraData cnd : getFeatureCustomExtraData()) {
			filterList.add(cnd.getLabel());
			if (parentViewer != null && parentViewer.getFilter() != null) {
				if (cnd.getKey().equals(parentViewer.getFilter().getColumnKey()))
					filterList.select(i);
			}
			i++;
		}
		if (filterList.getItemCount() > 1) {	
			filterList.setEnabled(true);
			filterList.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					validateInput();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub		
				}
			});	
		}
	}	
	
	public Entry getMSAnnotationEntry() {
		return msAnnotationEntry;
	}

	public void setMSAnnotationEntry(Entry msGlycanEntry) {
		this.msAnnotationEntry = msGlycanEntry;
	}
	
	public void validateInput(){
		txtOutput.setText(PARAMS_OK);
		if( getMSAnnotationEntry() == null ) {
			txtOutput.setText("Please select MS Annotation Results");
		} else if( getFilterKey() == null ) {
			txtOutput.setText("Please select a filter criterion");			
		} else if( getNumTopHits() == -2 ) {
			txtOutput.setText("Invalid value for 'Num Top Hits'. Please enter 'All' or an integer greater than 0.");			
		}
		if (txtOutput.getText().equals(PARAMS_OK)) {
			getButton(IDialogConstants.OK_ID).setEnabled(true);
		} else {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
	}

	protected void createFilterSection(Composite parent) {
	}
}
