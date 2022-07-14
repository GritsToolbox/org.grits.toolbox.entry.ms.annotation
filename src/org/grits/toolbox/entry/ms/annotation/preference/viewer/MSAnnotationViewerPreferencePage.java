package org.grits.toolbox.entry.ms.annotation.preference.viewer;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.grits.toolbox.datamodel.ms.annotation.preference.MSAnnotationViewerPreference;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage;
import org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage_NatBridge;

/**
 * Extends the MassSpecViewerPreferencePage page to allow the user to specify 
 * the MS Annotation Viewer specifc preferences.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * @see MassSpecViewerPreferencePage
 */
public class MSAnnotationViewerPreferencePage extends MassSpecViewerPreferencePage {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSAnnotationViewerPreferencePage.class);
	protected Boolean[][] hideUnannotated = null;
	protected Button cboxHideUnannotated =null;
	protected Button cbUnannotatedPeaks = null;
	protected Boolean[][] showUnannotatedPeaks = null;
	protected Button cbUnannotatedPeakLabels = null;
	protected Boolean[][] showUnannotatedPeakLabels = null;

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage#getTableFillType()
	 */
	@Override
	protected FillTypes getTableFillType() {
		FillTypes[] fillTypes = MSAnnotationMultiPageViewer.getPreferencePageFillTypes(getCurMSLevel());
		FillTypes fillType = fillTypes[getTableNumber()];
		return fillType;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage#getTableFillLabel()
	 */
	@Override
	protected String getTableFillLabel() {
		String[] labels = MSAnnotationMultiPageViewer.getPreferencePageLabels(getCurMSLevel());
		String label = labels[getTableNumber()];
		return label;
	}
	
	/**
	 * Extends the super-class method. First call the super-class method and then
	 * handle the un-annotated elements.
	 * @see org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage#updateColumnChooserElements(org.eclipse.swt.widgets.Composite, boolean)
	 */
	@Override
	protected void updateColumnChooserElements(Composite container, boolean _bDefault) {
		super.updateColumnChooserElements(container, _bDefault);
		int iCurMS = getCurMSLevel() - 1;
		MSAnnotationViewerPreference pref = (MSAnnotationViewerPreference) natBridge[getTableNumber()][iCurMS].getPreference();
		if( hideUnannotated[getTableNumber()][iCurMS] == null ) {
			hideUnannotated[getTableNumber()][iCurMS] = new Boolean( pref.isHideUnannotatedPeaks() );
		}
		boolean bEnableHideUnannotated = (getTableFillType() == FillTypes.PeaksWithFeatures && getCurMSLevel() > 1) || 
				(getTableFillLabel().contains("Summary") && getCurMSLevel() > 1);
				
		cboxHideUnannotated.setEnabled(bEnableHideUnannotated);
		boolean bSelected = hideUnannotated[getTableNumber()][iCurMS] && bEnableHideUnannotated;
		cboxHideUnannotated.setSelection(bSelected);
		if( showUnannotatedPeaks[getTableNumber()][iCurMS] == null ) {
			showUnannotatedPeaks[getTableNumber()][iCurMS] = new Boolean( pref.isShowUnannotated());
			showUnannotatedPeakLabels[getTableNumber()][iCurMS] = new Boolean( pref.isShowUnannotatedLabels());
		}
		if ( this.comboMSlevel.getText().equals( OVERVIEW ) ) {
			cbUnannotatedPeaks.setEnabled(false);
			cbUnannotatedPeakLabels.setEnabled(false);
		} else {
			cbUnannotatedPeaks.setEnabled(true);
		}
		cbUnannotatedPeaks.setSelection(showUnannotatedPeaks[getTableNumber()][iCurMS]);
		if( cbUnannotatedPeaks.getSelection() ) {
			cbUnannotatedPeakLabels.setEnabled(true);
		} else {
			cbUnannotatedPeakLabels.setEnabled(false);
		}
		cbUnannotatedPeakLabels.setSelection(showUnannotatedPeakLabels[getTableNumber()][iCurMS]);
	}
	
	/**
	 * Extends the super-class method. First call the super-class method to add their
	 * components, then add the components for supporting un-annotated peaks.
	 * 
	 * @see org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage#setAnnotatedElements(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void setAnnotatedElements(Composite container) {
		super.setAnnotatedElements(container);
		cbUnannotatedPeaks = new Button(spectraContainer, SWT.CHECK);
		cbUnannotatedPeaks.setText("Unannotated Peaks");
		GridData gdUnannotatedPeaks = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1);
		cbUnannotatedPeaks.setLayoutData(gdUnannotatedPeaks);
		showUnannotatedPeaks = new Boolean[getMaxNumTables()][MAX_VAL];
		cbUnannotatedPeaks.addSelectionListener(new SelectionListener() {			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int iCurMS = getCurMSLevel() - 1;
				showUnannotatedPeaks[getTableNumber()][iCurMS] = cbUnannotatedPeaks.getSelection();				
				if( ! cbUnannotatedPeaks.getSelection() ) {
					cbUnannotatedPeakLabels.setSelection(false);
				} 
				cbUnannotatedPeakLabels.setEnabled(cbUnannotatedPeaks.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		cbUnannotatedPeakLabels = new Button(spectraContainer, SWT.CHECK);
		cbUnannotatedPeakLabels.setText("Show labels");
		GridData gdUnannotatedPeakLabels = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1);
		cbUnannotatedPeakLabels.setLayoutData(gdUnannotatedPeakLabels);
		cbUnannotatedPeakLabels.setEnabled(false);
		cbUnannotatedPeakLabels.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int iCurMS = getCurMSLevel() - 1;
				showUnannotatedPeakLabels[getTableNumber()][iCurMS] = cbUnannotatedPeakLabels.getSelection();				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		showUnannotatedPeakLabels = new Boolean[getMaxNumTables()][MAX_VAL];
	}


	/**
	 * Adds the "Hide annotated" options to a parent container. 
	 * @param container
	 * 		parent container to which to add the options 
	 */
	protected void initHideAnnotated(Composite container) {
		GridData gridData1 = GridDataFactory.fillDefaults().grab(true, false).create();
		gridData1.horizontalSpan = 4;
		cboxHideUnannotated = new Button(container, SWT.CHECK);
		cboxHideUnannotated.setText("Hide unannotated peaks");
		cboxHideUnannotated.setLayoutData(gridData1);
		cboxHideUnannotated.setEnabled(false);		
		hideUnannotated = new Boolean[getMaxNumTables()][MAX_VAL];
		cboxHideUnannotated.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int iCurMS = getCurMSLevel() - 1;
				hideUnannotated[getTableNumber()][iCurMS] = cboxHideUnannotated.getSelection();				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	/** 
	 * Extend the super-class method. First call the super-class method to initialize
	 * their components then add the components specific to MS Annotation
	 * @see org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage#initComponents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void initComponents( Composite container ) {
		super.initComponents(container);
		initHideAnnotated(container);

		GridData gridData1 = GridDataFactory.fillDefaults().grab(true, false).create();
		gridData1.horizontalSpan = 4;
		Label lbl = new Label(container, SWT.None);
		lbl.setText("");
		lbl.setLayoutData(gridData1);

	}

	/**
	 * Used to set the user's preference for whether or not to hide un-annotated peaks
	 * in the GRITS table
	 * @return true if the user selected "Hide Unannotated Peaks", false otherwise
	 */
	protected boolean getHideUnannotatedPeaks() {
		if( cboxHideUnannotated == null ) {
			return false;
		}
		return cboxHideUnannotated.getSelection();		
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage#updateTableTypeCombo(int)
	 */
	@Override
	protected void updateTableTypeCombo( int _iMSLevel ) {
		String[] tableTypes = MSAnnotationMultiPageViewer.getPreferencePageLabels(_iMSLevel);
		String defaultTable = tableTypes[0];
		comboTablelevel.setItems(tableTypes);
		comboTablelevel.setText(defaultTable);		
	}
	
	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage#getMaxNumTables()
	 */
	@Override
	protected int getMaxNumTables() {
		int iMaxTableTypes = MSAnnotationMultiPageViewer.getPreferencePageMaxNumPages();
		return iMaxTableTypes;
	}
	
	/**
	 * Creates a new MS Annotation Preference Page to GRITS Table "bridge" that 
	 * allows the column chooser dialog and underlying components to be 
	 * utilized in a GRITS preference page.
	 *
	 * @see org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage#getPreferenceUItoNatBridge(boolean)
	 */
	@Override
	protected MassSpecViewerPreferencePage_NatBridge getPreferenceUItoNatBridge(boolean _bDefault) {
		int iMSLevel = getCurMSLevel();
		FillTypes fillType = getTableFillType();
		MSAnnotationViewerPreferencePage_NatBridge natBridge = new MSAnnotationViewerPreferencePage_NatBridge( 
				new Composite(getShell(), SWT.NONE), 
				iMSLevel, fillType, getHideUnannotatedPeaks() );
		natBridge.initializeComponents(_bDefault);
		return natBridge;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage#getType()
	 */
	@Override
	protected String getType() {
		return MSAnnotationProperty.TYPE;
	}
	
	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage#updateMassSpecViewerPreference(int, int)
	 */
	@Override
	protected void updateMassSpecViewerPreference(int i, int j) {
		MSAnnotationViewerPreference pref = (MSAnnotationViewerPreference) natBridge[i][j].getPreference();
		pref.setShowUnannotated(showUnannotatedPeaks[i][j]);
		pref.setShowUnannotatedLabels(showUnannotatedPeakLabels[i][j]);
		super.updateMassSpecViewerPreference(i, j);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage#save(org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage_NatBridge)
	 */
	@Override
	protected void save(MassSpecViewerPreferencePage_NatBridge natBridge) {
		if( this.cboxHideUnannotated.isEnabled() ) {
			((MSAnnotationViewerPreferencePage_NatBridge) natBridge).setHideUnannotatedPeaks(getHideUnannotatedPeaks());
		}
		natBridge.updatePreferences();
	}	
}
