package org.grits.toolbox.entry.ms.annotation.preference.viewer;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.datamodel.ms.annotation.preference.MSAnnotationViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.preference.MSAnnotationViewerPreferenceLoader;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.datamodel.ms.preference.MassSpecViewerPreference;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.display.control.table.tablecore.GRITSTable;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessorUtil;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage_NatBridge;


/**
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class MSAnnotationViewerPreferencePage_NatBridge extends MassSpecViewerPreferencePage_NatBridge {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSAnnotationViewerPreferencePage_NatBridge.class);	
	protected boolean bHideUnannotatedPeaks = false; 	

	/**
	 * @param parent - the parent Composite (viewer)
	 * @param iMSLevel - current MS leel
	 * @param fillType - FillType to specify column settings for
	 * @param bHideUnannotatedPeaks - whether or not to hide rows that aren't annotated
	 */
	public MSAnnotationViewerPreferencePage_NatBridge( Composite parent, int iMSLevel, FillTypes fillType, boolean bHideUnannotatedPeaks ) {			
		super(parent, iMSLevel, fillType);
		this.bHideUnannotatedPeaks = bHideUnannotatedPeaks;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage_NatBridge#getNewSimianTable(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected GRITSTable getNewSimianTable(Composite parent) {
		return new MSAnnotationTable( parent, null ); 		
	}

	/**
	 * @return MSAnnotationTableDataObject - a newly instantiated MassSpecTableDataObject
	 */
	@Override 
	protected MSAnnotationTableDataObject getNewTableDataObject() {
		return new MSAnnotationTableDataObject(this.iMSLevel, this.fillType);
	}

	/**
	 * @return TableViewerPreference - a newly instantiated MSAnnotationViewerPreference
	 */
	@Override
	protected TableViewerPreference getNewTableViewerPreference() {
		return new MSAnnotationViewerPreference();
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage_NatBridge#getDefaultSettings()
	 */
	@Override
	protected TableViewerColumnSettings getDefaultSettings() {
		TableViewerColumnSettings newSettings = super.getDefaultSettings();
		MSAnnotationTableDataProcessorUtil.fillMassSpecColumnSettingsScan(newSettings, this.iMSLevel);
		if ( this.fillType == FillTypes.PeaksWithFeatures ) {
			MSAnnotationTableDataProcessorUtil.fillMSAnnotationColumnSettingsAnnotation(newSettings);
			MSAnnotationTableDataProcessorUtil.fillMSAnnotationColumnSettingsFeature(newSettings);
		}
		return newSettings;
	}

	/**
	 * @return boolean - value of member variable "bHideUnannotatedPeaks", whether or not to hide unannotated peaks
	 */
	public boolean getHideUnannotatedPeaks() {
		return this.bHideUnannotatedPeaks;
	}

	/**
	 * @param _bVal - sets the value of member variable "bHideUnannotatedPeaks", whether or not to hide unannotated peaks
	 */
	public void setHideUnannotatedPeaks( boolean _bVal ) {
		this.bHideUnannotatedPeaks = _bVal;
	}
	
	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage_NatBridge#setDefaultPreferences()
	 */
	@Override
	protected void setDefaultPreferences() {
		super.setDefaultPreferences();
		MSAnnotationViewerPreference preferences = (MSAnnotationViewerPreference ) getNatTable().getGRITSTableDataObject().getTablePreferences();
		MSAnnotationTableDataProcessor.setDefaultColumnViewSettings(this.fillType, preferences.getPreferenceSettings());
	}
	
	
	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage_NatBridge#initializeTableData(boolean)
	 */
	@Override
	protected void initializeTableData(boolean _bDefault) throws Exception {
		super.initializeTableData(_bDefault);
		if( ! _bDefault ) {
			MSAnnotationViewerPreference preferences = (MSAnnotationViewerPreference ) getNatTable().getGRITSTableDataObject().getTablePreferences();
			setHideUnannotatedPeaks( preferences.isHideUnannotatedPeaks() );
		}
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage_NatBridge#updatePreferences()
	 */
	@Override
	public void updatePreferences() {
		((MSAnnotationTable) natTable).setHideUnannotated(this.bHideUnannotatedPeaks);
		super.updatePreferences();
	}
	
	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.preference.viewer.MassSpecViewerPreferencePage_NatBridge#getCurrentTableViewerPreference(int, org.grits.toolbox.datamodel.ms.tablemodel.FillTypes)
	 */
	@Override
	protected MassSpecViewerPreference getCurrentTableViewerPreference( int _iMSLevel, FillTypes _fillType ) {
		return MSAnnotationViewerPreferenceLoader.getTableViewerPreference(_iMSLevel, _fillType);
	}

}
