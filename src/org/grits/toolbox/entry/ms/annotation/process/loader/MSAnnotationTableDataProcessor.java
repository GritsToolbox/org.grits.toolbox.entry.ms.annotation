package org.grits.toolbox.entry.ms.annotation.process.loader;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.datamodel.ms.annotation.preference.MSAnnotationViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.dmtranslate.DMAnnotation;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.dmtranslate.DMFeature;
import org.grits.toolbox.datamodel.ms.preference.MassSpecViewerPreference;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMPeak;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMPrecursorPeak;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMScan;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.display.control.table.datamodel.GRITSListDataRow;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationMetaData;
import org.grits.toolbox.entry.ms.extquantfiles.process.CorrectedQuantColumnInfo;
import org.grits.toolbox.entry.ms.extquantfiles.process.CustomAnnotationDataProcessor;
import org.grits.toolbox.entry.ms.extquantfiles.process.ExternalQuantColumnInfo;
import org.grits.toolbox.entry.ms.extquantfiles.process.ExtractDataProcessor;
import org.grits.toolbox.entry.ms.extquantfiles.process.FullMzXMLDataProcessor;
import org.grits.toolbox.entry.ms.extquantfiles.process.QuantFileProcessor;
import org.grits.toolbox.entry.ms.extquantfiles.process.StandardQuantColumnInfo;
import org.grits.toolbox.entry.ms.extquantfiles.process.StandardQuantDataProcessor;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotation;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotationPeak;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecStandardQuant;
import org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor;
import org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessorUtil;
import org.grits.toolbox.entry.ms.property.MassSpecEntityProperty;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.ExternalQuantAlias;
import org.grits.toolbox.entry.ms.property.datamodel.ExternalQuantFileToAlias;
import org.grits.toolbox.entry.ms.property.datamodel.InternalStandardQuantFileList;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.grits.toolbox.entry.ms.property.datamodel.MassSpecUISettings;
import org.grits.toolbox.ms.annotation.utils.AnnotationRowExtraction;
import org.grits.toolbox.ms.file.extquant.data.ExternalQuantSettings;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.CustomExtraData;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.DataHeader;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.FeatureSelection;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.Scan;
import org.grits.toolbox.ms.om.data.ScanFeatures;
import org.grits.toolbox.ms.om.io.xml.AnnotationReader;
import org.grits.toolbox.ms.om.io.xml.AnnotationWriter;
import org.grits.toolbox.widgets.progress.IProgressListener.ProgressType;

/**
 * Extends MassSpecTableDataProcessor with generic options for displaying annotated mass spec data
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * 
 */
public class MSAnnotationTableDataProcessor extends MassSpecTableDataProcessor
{
	private static final Logger logger = Logger.getLogger(MSAnnotationTableDataProcessor.class);
	//    protected Data data = null;
	protected Method method = null;
	protected ScanFeatures curScanFeatures = null;
	protected Feature curFeature = null;
	protected Annotation curAnnotation = null;
	protected AnnotationReader xmlReader = null;
	protected AnnotationWriter xmlWriter = null;
	public final static Integer READ_FEATURES = 3;
	protected MSAnnotationEntityProperty msProp = null;
	private GRITSListDataRow overviewRow = null;
	private Integer iCurDirtyParentScanNum = null; // the current dirty parent scan num when writing out multiple rows
	private List<Integer> dirtyParentScans = new ArrayList<>(); // stores a list of parent scan numbers when changing from the overvieiw

	/**
	 * @param _entry - current MS Entry
	 * @param _sourceProperty - current MS property
	 * @param iMinMSLevel - min MS level of this MS run
	 */
	public MSAnnotationTableDataProcessor(Entry _entry, Property _sourceProperty, int iMinMSLevel ) {
		super(_entry, _sourceProperty, iMinMSLevel);
	}

	/**
	 * @param _entry - current MS Entry
	 * @param _sourceProperty - current MS property
	 * @param fillType - FillType, options are "Scans" and "PeakList" and determine how to fill the GRITSTable 
	 * @param iMinMSLevel - min MS level of this MS run
	 */
	public MSAnnotationTableDataProcessor(Entry _entry, Property _sourceProperty, FillTypes fillType, int iMinMSLevel ) {
		super(_entry, _sourceProperty, fillType, iMinMSLevel);
	}

	/**
	 * @param _parent - if table created by a parent, the parent's TableDataProcessor 
	 * @param _entry - current MS Entry
	 * @param _sourceProperty - current MS property
	 * @param fillType - FillType, options are "Scans" and "PeakList" and determine how to fill the GRITSTable 
	 * @param iMinMSLevel - min MS level of this MS run
	 */
	public MSAnnotationTableDataProcessor( TableDataProcessor _parent, Property _sourceProperty, FillTypes fillType, int iMinMSLevel ) {
		super(_parent.getEntry(), _sourceProperty, fillType, iMinMSLevel);
		this.data = ( (MSAnnotationTableDataProcessor) _parent).getData();
		this.method = ( (MSAnnotationTableDataProcessor) _parent).getMethod();
		this.quantFileProcessors = ( (MassSpecTableDataProcessor) _parent).getQuantFileProcessors();
	}

	/**
	 * @return Integer - current scan number being processed for file save
	 */
	public Integer getCurDirtyParentScanNum() {
		return iCurDirtyParentScanNum;
	}

	/**
	 * @param iCurDirtyParentScanNum - Integer value for current scan number being processed for file save
	 */
	public void setCurDirtyParentScanNum(Integer iCurDirtyParentScanNum) {
		this.iCurDirtyParentScanNum = iCurDirtyParentScanNum;
	}

	/**
	 * @param iParentScan - Integer value for a scan number whose annotation selection has changed
	 * 
	 * Description: Adds the specified scan number to the list of dirty parent scans so GRITS knows
	 * which files to update on save.
	 */
	public void addDirtyParentScan( Integer iParentScan ) {
		if( ! this.dirtyParentScans.contains(iParentScan) ) {
			this.dirtyParentScans.add(iParentScan);
		}
	}

	/**
	 * Description: Clears all scan numbers from the list of dirty parent scans after GRITS saves them.
	 */
	public void clearDirtyParentScans() {
		this.dirtyParentScans.clear();
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#addUnrecognizedHeaders(org.grits.toolbox.datamodel.ms.preference.MassSpecViewerPreference)
	 */
	@Override
	protected int addUnrecognizedHeaders(MassSpecViewerPreference _preference) {
		int iColCnt = 0;
		if( this.fillType != FillTypes.Scans ) {
			if( getGRITSdata().getDataHeader().getFeatureCustomExtraData() != null ) {
				iColCnt += MSAnnotationTableDataProcessorUtil.fillColumnSettingsCustomExtraData(
						_preference.getPreferenceSettings(), 
						getGRITSdata().getDataHeader().getFeatureCustomExtraData(),
						_preference.getPreferenceSettings().getUnrecognizedHeaders());
			}	
			if( getGRITSdata().getDataHeader().getAnnotationCustomExtraData() != null ) {
				iColCnt += MSAnnotationTableDataProcessorUtil.fillColumnSettingsCustomExtraData(
						_preference.getPreferenceSettings(), 
						getGRITSdata().getDataHeader().getAnnotationCustomExtraData(),
						_preference.getPreferenceSettings().getUnrecognizedHeaders());
			}	
		}
		// DBW 10/21/14:  This is a hack solution to the current problem that the
		// "Peak Custom Data" is not calculated until after the first time the annotation
		// is rendered. Once calculated, it is written to the archive and will be stored
		// in the DataHeader. But the first time, we have to call the MS parent method to calculate
		// the Extra Data from the MS file.
		if( getGRITSdata().getDataHeader().getPeakCustomExtraData() != null && 
				! getGRITSdata().getDataHeader().getPeakCustomExtraData().isEmpty() ) {
			iColCnt += MSAnnotationTableDataProcessorUtil.fillColumnSettingsCustomExtraData(
					_preference.getPreferenceSettings(), 
					getGRITSdata().getDataHeader().getPeakCustomExtraData(),
					_preference.getPreferenceSettings().getUnrecognizedHeaders());			
		} else {
			iColCnt += super.addUnrecognizedHeaders(_preference);
		}
		return iColCnt;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#initializeTableDataObject(org.grits.toolbox.core.datamodel.property.Property)
	 */
	@Override
	public void initializeTableDataObject( Property _sourceProperty ) {
		setSimianTableDataObject(new MSAnnotationTableDataObject( ( (MassSpecEntityProperty) _sourceProperty).getMsLevel(), fillType));
		getSimianTableDataObject().initializePreferences();    	
		if( getSimianTableDataObject().getTablePreferences().settingsNeedInitialization() ) {
			TableViewerPreference tvp = initializePreferences();		
			MSAnnotationTableDataProcessor.setDefaultColumnViewSettings(this.fillType, tvp.getPreferenceSettings());
			getSimianTableDataObject().setTablePreferences(tvp);
			getSimianTableDataObject().getTablePreferences().writePreference();        	
		}       
	}

	/**
	 * @return MSAnnotationEntityProperty - a cast of the "sourceProperty" to MSAnnotationEntityProperty
	 */
	protected MSAnnotationEntityProperty getMSAnnotationEntityProperty() {
		if( this.sourceProperty instanceof MSAnnotationEntityProperty )
			return (MSAnnotationEntityProperty) this.sourceProperty;

		return null;
	}

	/**
	 * @return Method - the org.grits.toolbox.ms.om.data.Method associated with this annotation
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * @param method - the org.grits.toolbox.ms.om.data.Method associated with this annotation
	 */
	public void setMethod(Method method) {
		this.method = method;
	}

	/**
	 * @return Data - the "data" member variable (Object) cast to org.grits.toolbox.ms.om.data.Data 
	 */
	public Data getGRITSdata() {
		return (Data) data;
	}

	/**
	 * @param data - the "data" member variable (Object) cast to org.grits.toolbox.ms.om.data.Data 
	 */
	public void setData(Data data) {
		this.data = data;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#addScansTableData()
	 */
	@Override
	protected void addScansTableData() {
		int iCnt = 1;
		for( Integer iScan : getGRITSdata().getScans().keySet() )
		{
			Scan msScan = getGRITSdata().getScans().get(iScan);
			if( (iCnt%100) == 0 ) {
				this.progressBarDialog.getMinorProgressBarListener(0).setProgressMessage("Building scans table. Scan: " + iCnt + " of " + getScanData().size());
				this.progressBarDialog.getMinorProgressBarListener(0).setProgressValue(iCnt);
			}
			iCnt++;
			addScanRow(msScan);
			if ( bCancel ) {
				setSimianTableDataObject(null);
				return;
			}
		}    	
	}

	/**
	 * Description: Uses the list of "dirtyParentScans" to determine which files need to be updated.
	 * 
	 * @return boolean - success/fail of saving changes to data file
	 * @throws Exception
	 * 
	 */
	@Override    
	public boolean saveChanges() throws Exception {
		try {
			this.progressBarDialog.getMinorProgressBarListener(0).setProgressMessage("Updating data...");
			this.progressBarDialog.getMinorProgressBarListener(0).setProgressValue(0);

			this.progressBarDialog.getMinorProgressBarListener(0).setMaxValue(this.dirtyParentScans.size());
			this.xmlWriter = new AnnotationWriter(); 
			for( int i = 0; i < this.dirtyParentScans.size(); i++ ) {
				Integer iParentScan = this.dirtyParentScans.get(i);
				setCurDirtyParentScanNum(iParentScan);
				this.progressBarDialog.getMinorProgressBarListener(0).setProgressMessage("Updating scan: " + iParentScan);			
				updateAnnotationDataSingleScan(iParentScan);
				this.progressBarDialog.getMinorProgressBarListener(0).setProgressValue(i+1);
			}
			clearDirtyParentScans();
		} catch( Exception e) {
			clearDirtyParentScans();
			throw new Exception("Unable to write XML File", e);
		}	
		return true;
	}

	/**
	 * Description: Reads the annotation DataHeader and returns the Feature CustomExtraData, if any.
	 * 
	 * @param _entry - current MS Entry
	 * @return List<CustomExtraData> - list of MSAnnotation Feature CustomExtraData
	 */
	public final static List<CustomExtraData> getMSAnnotationFeatureCustomExtraData(Entry _entry) {
		try {
			Property property = _entry.getProperty();
			if( property instanceof MSAnnotationEntityProperty ) {
				property = ( (MSAnnotationEntityProperty) property).getMSAnnotationParentProperty();
			}

			String origFile = ((MSAnnotationProperty) property).getFullyQualifiedArchiveFileNameByAnnotationID(_entry);
			DataHeader dHeader = null;
			AnnotationReader xmlReader = new AnnotationReader();
			dHeader = xmlReader.readDataHeader(origFile);
			return dHeader.getFeatureCustomExtraData();
		} catch (Exception e) {
			logger.error("Unable to read data file for entry: "+ _entry, e);
		}
		return null;
	}

	public String findFullyQualifiedArchiveFileNameForScanNum(Entry entry) {
		String sArchiveFile = null;
		if (entry.getProperty() instanceof MSAnnotationEntityProperty) {
			MSAnnotationEntityProperty msAnnotEntityProp = (MSAnnotationEntityProperty) entry.getProperty();
			MSAnnotationProperty msAnnotProp = (MSAnnotationProperty) msAnnotEntityProp.getParentProperty();
			if ( msAnnotEntityProp.getParentScanNum() > 0 ) { // we want the scan num of the property
				sArchiveFile = msAnnotProp.getFullyQualifiedArchiveFileNameByScanNum(entry, msAnnotEntityProp.getParentScanNum());
			} else {
				sArchiveFile = msAnnotProp.getFullyQualifiedArchiveFileNameByScanNum(entry, msAnnotEntityProp.getScanNum());				
			}
			File f = new File(sArchiveFile);
			if( f.exists() ) {
				return sArchiveFile;
			}

			Entry parent = entry.getParent();
			return findFullyQualifiedArchiveFileNameForScanNum(parent);
		}
		return null;
	}


	/**
	 * getScanArchiveFile():  returns the fully qualified name of the archive file for this project
	 * The file name is determined at run-time based on the MS type and the parent scan number
	 * @param <none>
	 * @return String value of fully qualified name of the archive file
	 */	
	public String getScanArchiveFile() {
		if (entry == null)
			return null;

		String sArchiveFile = null;
		if (getEntry().getProperty() instanceof MSAnnotationEntityProperty) {
			MSAnnotationEntityProperty msAnnotEntityProp = (MSAnnotationEntityProperty) getEntry().getProperty();
			MSAnnotationProperty msAnnotProp = (MSAnnotationProperty) msAnnotEntityProp.getParentProperty();
			if( getMethod() != null && getMethod().getMsType().equals(Method.MS_TYPE_LC) ) {
				// 2 cases. If reading details, then curMSAnnotEntityProp won't be null && either will its scan and parent scan nums 
				sArchiveFile = findFullyQualifiedArchiveFileNameForScanNum(getEntry());
				//				MSAnnotationEntityProperty curMSAnnotEntityProp = getCurEntityProperty();
				//				if( curMSAnnotEntityProp != null && curMSAnnotEntityProp.getScanNum() != null && curMSAnnotEntityProp.getParentScanNum() != null )  {
				//					sArchiveFile = msAnnotProp.getFullyQualifiedArchiveFileNameByScanNum(getEntry(), curMSAnnotEntityProp.getParentScanNum() );
				//				} else if( msAnnotEntityProp != null && msAnnotEntityProp.getParentScanNum() == null && getCurDirtyParentScanNum() != null ) {
				//					sArchiveFile = msAnnotProp.getFullyQualifiedArchiveFileNameByScanNum(getEntry(), getCurDirtyParentScanNum());
				//				} else if ( msAnnotEntityProp.getScanNum() > 0 ) { // we want the scan num of the property
				//					sArchiveFile = msAnnotProp.getFullyQualifiedArchiveFileNameByScanNum(getEntry(), msAnnotEntityProp.getScanNum() );					
				//				}
			} else { // DI, TIM, or MS Profile
				sArchiveFile = msAnnotProp.getFullyQualifiedArchiveFileNameByAnnotationID(getEntry());
			}
		}
		if( sArchiveFile == null ) {
			logger.error("Unable to determine archive file name!");
		}
		return sArchiveFile;
	}

	/**
	 * getDataArchiveFile():  returns the fully qualified name of the data archive file for this project
	 * The file name is determined at run-time based on the MS type and the parent scan number
	 * @param <none>
	 * @return String value of fully qualified name of the archive file
	 */	
	public String getDataArchiveFile() {
		if (entry == null)
			return null;

		String sArchiveFile = null;
		if (getEntry().getProperty() instanceof MSAnnotationEntityProperty) {
			MSAnnotationEntityProperty msAnnotEntityProp = (MSAnnotationEntityProperty) getEntry().getProperty();
			MSAnnotationProperty msAnnotProp = (MSAnnotationProperty) msAnnotEntityProp.getParentProperty();
			if( getMethod() != null && getMethod().getMsType().equals(Method.MS_TYPE_LC) ) {
				// 2 cases. If reading details, then curMSAnnotEntityProp won't be null && either will its scan and parent scan nums 
				MSAnnotationEntityProperty curMSAnnotEntityProp = getCurEntityProperty();
				if( msAnnotEntityProp != null && msAnnotEntityProp.getParentScanNum() == null && getCurDirtyParentScanNum() != null ) {
					sArchiveFile = msAnnotProp.getFullyQualifiedArchiveFileNameByScanNum(getEntry(), getCurDirtyParentScanNum());
				} else if( curMSAnnotEntityProp != null && curMSAnnotEntityProp.getScanNum() != null && curMSAnnotEntityProp.getParentScanNum() != null )  {
					sArchiveFile = msAnnotProp.getFullyQualifiedArchiveFileNameByScanNum(getEntry(), curMSAnnotEntityProp.getParentScanNum() );
				} else if ( msAnnotEntityProp.getScanNum() > 0 ) { // we want the scan num of the property
					sArchiveFile = msAnnotProp.getFullyQualifiedArchiveFileNameByScanNum(getEntry(), msAnnotEntityProp.getScanNum() );					
				}
			} else { // DI, TIM, or MS Profile
				sArchiveFile = msAnnotProp.getFullyQualifiedArchiveFileNameByAnnotationID(getEntry());
			}
		}
		if( sArchiveFile == null ) {
			logger.error("Unable to determine archive file name!");
		}
		return sArchiveFile;
	}


	/**
	 * @return Map<Integer,Scan> - casts the Scans data in the Data object for specific use
	 */
	protected Map<Integer,Scan> getScanData() {
		return (Map<Integer,Scan>) ( (Data) this.data ).getScans();
	}

	protected boolean isLCMSAndNeedsScanSet(Integer iScanNum) {
//		if( true ) {
//			logger.debug("Source property: " + this.sourceProperty );
//			MSAnnotationEntityProperty msAnnotEntityProp2 = (MSAnnotationEntityProperty) getEntry().getProperty();
//			MSAnnotationEntityProperty msAnnotEntityProp3 = (MSAnnotationEntityProperty) getEntry().getParent().getProperty();
//			//		msAnnotEntityProp2.setScanNum(iScanNum);
//		}
		MSAnnotationEntityProperty msAnnotEntityProp = (MSAnnotationEntityProperty) getSourceProperty();
		MassSpecProperty msProp = msAnnotEntityProp.getMassSpecParentProperty();
		if ( ! msProp.getMassSpecMetaData().getMsExperimentType().equals(Method.MS_TYPE_LC ) )
			return false;
		if( msAnnotEntityProp.getMsLevel() > 2 )
			return false;
		return true;


	}	

	/**
	 * Note: this method only applicable when loading LC-MS data (see method addPeaksTableDataNonOverview())
	 * @param iScanNum - sets the scan number in the current Entry's MSAnnotationEntityProperty
	 */
	protected void setScanNumber(Integer iScanNum ) {
		MSAnnotationEntityProperty msAnnotEntityProp = (MSAnnotationEntityProperty) getEntry().getProperty();
		msAnnotEntityProp.setScanNum(iScanNum);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#readDataFromFile()
	 */
	@Override
	public boolean readDataFromFile() {
		this.progressBarDialog.getMinorProgressBarListener(0).setProgressType(ProgressType.Indeterminant);
		this.progressBarDialog.getMinorProgressBarListener(0).setProgressMessage("Reading data file...");
		try {
			this.xmlReader = new AnnotationReader();
			String sSourceFile = getScanArchiveFile();
			logger.debug("Reading archive: " + sSourceFile);
			this.data = this.xmlReader.readDataWithoutFeatures(sSourceFile);
			this.method = getGRITSdata().getDataHeader().getMethod();
		} catch( Exception e ) {
			logger.error("readDataFromFile: unable to read mzXML.", e);
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#getExternalQuantAlias(org.grits.toolbox.core.datamodel.property.PropertyDataFile)
	 */
	@Override
	protected ExternalQuantAlias getExternalQuantAlias(MSPropertyDataFile quantFile) {
		MSAnnotationProperty msap = getMSAnnotationEntityProperty().getMSAnnotationParentProperty(); 
		String sExtQuantType = MassSpecUISettings.getExternalQuantType(quantFile);	
		ExternalQuantFileToAlias mAliases = msap.getMSAnnotationMetaData().getExternalQuantToAliasByQuantType(sExtQuantType);

		ExternalQuantAlias foundAlias = null;
		if( mAliases != null ) {
			int iCnt = 0;
			for( String sFileName : mAliases.getSourceDataFileNameToAlias().keySet() ) {
				ExternalQuantAlias aliasInfo = mAliases.getSourceDataFileNameToAlias().get(sFileName);
				if( aliasInfo == null ) {
					continue; // not part of this analysis
				}
				if( sFileName.equals(quantFile.getName()) ) {
					foundAlias = aliasInfo;
				}
			}
		}
		return foundAlias;
	}

	@Override
	protected List<MassSpecCustomAnnotation> getCustomAnnotationList() {
		MSAnnotationProperty msap = getMSAnnotationEntityProperty().getMSAnnotationParentProperty(); 
		if( msap.getMSAnnotationMetaData() != null && 
				msap.getMSAnnotationMetaData().getCustomAnnotations() != null &&
				!  msap.getMSAnnotationMetaData().getCustomAnnotations().isEmpty() ) {
			return msap.getMSAnnotationMetaData().getCustomAnnotations();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#initializeExternalQuantProcessors()
	 */
	protected void initializeQuantFiles() {
		super.initializeQuantFiles();
	}

	/**
	 * @param msProp - an MSAnnotationEntityProperty object
	 */
	public void setCurEntityProperty(MSAnnotationEntityProperty msProp) {
		this.msProp = msProp;
	}

	/**
	 * @return MSAnnotationEntityProperty - the member variable "msProp"
	 */
	protected MSAnnotationEntityProperty getCurEntityProperty() {
		return msProp;
	}

	/**
	 * @param _iKey - int value for the key associated with the scan annotations, likely the scan num
	 * @return ScanFeatures - the scan features from the current source file for the specified scan number
	 */
	protected ScanFeatures loadScanFeatureFromXML(int _iKey) {
		try {
			if( this.xmlReader == null ) {
				this.xmlReader = new AnnotationReader();			
			}
			if ( isLCMSAndNeedsScanSet(_iKey) ) {
				setScanNumber(_iKey);
			}
			String sSourceFile = getScanArchiveFile();
			ScanFeatures features = this.xmlReader.readScanAnnotation(sSourceFile, _iKey);
//			ScanFeatures.usesComplexRowID = false;
//			if( features != null && features.getComplexRowId() != null && features.getComplexRowId() ) {
//				ScanFeatures.usesComplexRowID = true;
//			}
			setFeatureParentAnnotations(features, sSourceFile);
			return features;
		} catch( Exception ex ) {
			logger.error("Error loading scan feature from xml." , ex);
		}
		return null;
	}


	private void setFeatureParentAnnotations( ScanFeatures scanFeatures, String sSourceFile) {
		if( scanFeatures != null && ! scanFeatures.getFeatures().isEmpty() ) {
			Data data = this.xmlReader.readData(sSourceFile);
			if( data != null && ! data.getAnnotation().isEmpty() ) {
				for( Feature feature : scanFeatures.getFeatures() ) {
					if( feature.getParentAnnotation() == null ) {
						for( Annotation annotation : data.getAnnotation() ) {
							if( feature.getAnnotationId() != null && annotation.getId() != null && feature.getAnnotationId().equals(annotation.getId()) ) {
								feature.setParentAnnotation(annotation);
								if( ! getGRITSdata().getAnnotation().contains(annotation) ) {
									getGRITSdata().getAnnotation().add(annotation);
								}
							}
						}
					}
				}
			}
		}

	}

	private void verifyFeatureUsesRowIds( ScanFeatures scanFeatures ) {
		if( scanFeatures != null && scanFeatures.getFeatures() != null && ! scanFeatures.getFeatures().isEmpty() ) {
			for( Feature feature : scanFeatures.getFeatures() ) {
				if( feature.getPeaks().isEmpty() && feature.getPeaks().size() > 1 ) { // need to create row ids
					logger.debug("Got peaks!");
				}				
			}
		}
	}

	/**
	 * Description: sets the default order of columns for the MS Annotation table.
	 * 
	 * @param fillType - the FillType of the current page
	 * @param tvs - a TableViewerColumnSettings object
	 */
	public static void setDefaultColumnViewSettings(FillTypes fillType, TableViewerColumnSettings tvs) {
		MassSpecTableDataProcessor.setDefaultColumnViewSettings(fillType, tvs);
		if ( fillType == FillTypes.PeaksWithFeatures ) {
			GRITSColumnHeader header = tvs.getColumnHeader( DMAnnotation.annotation_id.name());
			if( header != null ) {
				tvs.setVisColInx(header, -1);
			}			
			header = tvs.getColumnHeader( DMAnnotation.annotation_sequence.name());
			if( header != null ) {
				tvs.setVisColInx(header, -1);
			}			
			header = tvs.getColumnHeader( DMAnnotation.annotation_num_candidates.name() );
			if( header != null ) {
				tvs.setVisColInx(header, -1);
			}		
			header = tvs.getColumnHeader( DMFeature.feature_id.name());
			if( header != null ) {
				tvs.setVisColInx(header, -1);
			}						
			header = tvs.getColumnHeader( DMFeature.feature_sequence.name());
			if( header != null ) {
				tvs.setVisColInx(header, -1);
			}						
			header = tvs.getColumnHeader( DMFeature.feature_precursor_id.name());
			if( header != null ) {
				tvs.setVisColInx(header, -1);
			}				
			header = tvs.getColumnHeader( DMPeak.peak_id.name());
			if( header != null ) {
				tvs.setVisColInx(header, -1);
			}			
		}
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.display.control.table.process.TableDataProcessor#getNewTableViewerPreferences()
	 * 
	 * Creates a new MSAnnotationViewerPreference object
	 */
	@Override
	protected TableViewerPreference getNewTableViewerPreferences() {
		return new MSAnnotationViewerPreference();
	}

	/** 
	 * Do not call this method directly. It will be called by initializedPreferences 
	 */
	@Override
	protected TableViewerColumnSettings initializeColumnSettings() {
		TableViewerColumnSettings newSettings = super.initializeColumnSettings();
		MSAnnotationTableDataProcessorUtil.fillMassSpecColumnSettingsScan(newSettings, getMassSpecEntityProperty().getMsLevel());
		if ( this.fillType == FillTypes.PeaksWithFeatures ) {
			addAnnotationColumns(newSettings);
			addFeatureColumns(newSettings);
		}
		return newSettings;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#initializePreferences()
	 */
	@Override
	public TableViewerPreference initializePreferences() {
		MSAnnotationViewerPreference newPreferences = (MSAnnotationViewerPreference) super.initializePreferences();

		MSAnnotationViewerPreference oldPreferences = (MSAnnotationViewerPreference) getSimianTableDataObject().getTablePreferences();
		if( oldPreferences != null ) { // preserve previous setting if present
			newPreferences.setHideUnannotatedPeaks(oldPreferences.isHideUnannotatedPeaks());
		}
		return newPreferences;
	}

	/**
	 * @param sExtQuantFileName - String file name of external quant file
	 * @return List<CustomExtraData> - list of CustomExtraData matching the sExtQuantFileName
	 */
	protected List<CustomExtraData> getCurrentListOfCustomExtraData( String sExtQuantFileName ) {
		List<CustomExtraData> lNewCED = new ArrayList<CustomExtraData>();
		List<CustomExtraData> lCurCED = getGRITSdata().getDataHeader().getPeakCustomExtraData();
		for( CustomExtraData ced : lCurCED ) {
			if( ced.getDescription().equals(sExtQuantFileName) ) {
				lNewCED.add(ced);
			}
		}
		return lNewCED;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#loadSubScanExternalQuant(org.grits.toolbox.entry.ms.extquantfiles.process.ExternalQuantFileProcessor, org.grits.toolbox.ms.om.data.Scan)
	 */
	@Override
	protected boolean loadSubScanExternalQuant(QuantFileProcessor _processor, Scan msScan) {
		int iScanNum = msScan.getScanNo();
		((CustomAnnotationDataProcessor)_processor).setCurScan(msScan);
		_processor.loadExternalData();
		_processor.getQuantPeakData().setScanNo(iScanNum);
		_processor.getSettings().setTargetScanNumber(iScanNum);
		// NOTE: There was a previous implementation and code was commented out for a while.
		//       Could be at some point you might want to look at code before 02/01/16
		_processor.setSourcePeakList(getScanData());
		boolean bUpdated = _processor.matchExternalPeaks(false);
		return bUpdated;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#processExternalQuant()
	 */
	@Override
	protected boolean processExternalQuant() {
		return processExternalQuant(false); // for testing writing out to archive
	}

	/**
	 * @param _bForceWrite - boolean, whether or not to force write the external quant to the archive
	 * @return boolean - whether or not external quant was written to the archive
	 */
	protected boolean processExternalQuant( boolean _bForceWrite ) {
		if( this.quantFileProcessors == null )
			return false;
		boolean bLoaded = false;
		try {
			bLoaded = super.processExternalQuant();
		} catch (Exception e) {
			logger.error("processExternalQuant: error processing external quant.", e);
		}		
		try {
			if( bLoaded || _bForceWrite ) { // if we loaded from external file, write to the archive
				writeArchive();
			} 
		} catch (Exception e) {
			logger.error("processExternalQuant: error writing external quant to archive.", e);
		}		
		return bLoaded;
	}	

	/**
	 * Writes the Data and DataHeader objects to the project archive
	 */
	public void writeArchive() {
		AnnotationWriter xmlWriter = new AnnotationWriter();
		String sSourceFile = getScanArchiveFile();
		xmlWriter.writeDataToZip(getGRITSdata(), sSourceFile);	
		xmlWriter.writeDataHeaderToArchive(getGRITSdata().getDataHeader(), sSourceFile);				
	}


	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#buildTable()
	 */
	@Override
	public void buildTable() throws Exception {

		this.progressBarDialog.getMinorProgressBarListener(0).setProgressMessage("Building table...");
		int iMax = getGRITSdata().getScans().size();
		this.progressBarDialog.getMinorProgressBarListener(0).setMaxValue( iMax );
		getMySimianTableDataObject().setUnAnnotatedRows(new ArrayList<Integer>());
		ArrayList<GRITSColumnHeader> alHeader = new ArrayList<GRITSColumnHeader>();
		try {
			addHeaderLine(alHeader);
		} catch (Exception e) {
			throw new Exception("Unable to build table", e);
		}
		this.getSimianTableDataObject().getTableHeader().add(alHeader);

		processExternalQuant();

		if( this.fillType == FillTypes.Scans ) {
			addScansTableData();
		} else if ( this.fillType == FillTypes.PeakList ) {
			addPeaksTableData();
		} else if ( this.fillType == FillTypes.PeaksWithFeatures ) {
			addPeaksTableData();
		}
		if( ! bCancel && getSimianTableDataObject().getTableData().isEmpty() ) {
			// adding 2 blank rows to the subset table		
			getSimianTableDataObject().getTableData().add( TableDataProcessor.getNewRow( getSimianTableDataObject().getLastHeader().size(), 
					getSimianTableDataObject().getTableData().size() ) );
			getSimianTableDataObject().getTableData().add( TableDataProcessor.getNewRow( getSimianTableDataObject().getLastHeader().size(),
					getSimianTableDataObject().getTableData().size() ) );			
		}
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#loadSubScanExternalQuant(org.grits.toolbox.entry.ms.extquantfiles.process.ExternalQuantFileProcessor)
	 */
	@Override
	protected boolean loadSubScanExternalQuant(QuantFileProcessor _processor) {
		boolean bLoaded = super.loadSubScanExternalQuant(_processor);
		// bLoaded just says we read it from the original file and set it.
		// now we have to check to see if we've written it to the Annotation File 
		//		bLoaded = false;
		try {
			DataHeader dHeader = getGRITSdata().getDataHeader();
			if( _processor instanceof CustomAnnotationDataProcessor ) {
				MassSpecCustomAnnotation msca = ((CustomAnnotationDataProcessor) _processor).getMassSpecCustomAnnotation();
				Collection<MassSpecCustomAnnotationPeak> annotatedPeaks = msca.getAnnotatedPeaks().values();
				if( annotatedPeaks == null || annotatedPeaks.isEmpty() ) {
					return false; // an error??
				}
				Iterator<MassSpecCustomAnnotationPeak> itr = annotatedPeaks.iterator();
				while( itr.hasNext() ) {
					MassSpecCustomAnnotationPeak mscap = itr.next();				
					Double dMz = mscap.getPeakMz();
					String sKey = dMz.toString();
					String sLabel = dMz.toString();
					if( msca.getAnnotatedPeaks().containsKey(dMz) ) {
						sLabel = sKey + " - " + msca.getAnnotatedPeaks().get(dMz).getPeakLabel();
					}
					CustomExtraData cedInt = ExternalQuantColumnInfo.getExternalQuantIntensity(sKey, sLabel);
					cedInt.setDescription(msca.getAnnotationName());
					CustomExtraData cedIntMz = ExternalQuantColumnInfo.getExternalQuantIntensityMz(sKey, sLabel);
					cedIntMz.setDescription(msca.getAnnotationName());
					if( ! dHeader.getPeakCustomExtraData().contains(cedIntMz) ) {
						dHeader.getPeakCustomExtraData().add( cedIntMz );
						bLoaded = true;
					}
					if( ! dHeader.getPeakCustomExtraData().contains(cedInt) ) {
						dHeader.getPeakCustomExtraData().add( cedInt );
						bLoaded = true;
					}
				}


			} else {
				throw new Exception("Processor type not supported: " + _processor.getClass());				
			}
		} catch (IOException e) {
			logger.error("loadExternalQuantData: error writing Data Header with external quant changes.", e);
		} catch (Exception e) {
			logger.error("loadExternalQuantData: error writing Data Header with external quant changes.", e);
		}		
		return bLoaded;
	}

	@Override
	protected void setQuantFiles( List<QuantFileProcessor> lQuantFileProcessors ) {
		if( this.quantFileProcessors != null && ! this.quantFileProcessors.isEmpty() ) {
			List<QuantFileProcessor> toRemove = new ArrayList<QuantFileProcessor>();
			for( QuantFileProcessor curQuantFileProcessor : this.quantFileProcessors ) {
				if( curQuantFileProcessor instanceof CustomAnnotationDataProcessor ) {
					continue;
				}				
				String sCurKeyID = curQuantFileProcessor.getKeyID();
				boolean bFound = false;
				for( QuantFileProcessor newQuantFileProcessor : lQuantFileProcessors ) {
					if( newQuantFileProcessor instanceof CustomAnnotationDataProcessor ) {
						continue;
					}				
					if( ! newQuantFileProcessor.getClass().equals(curQuantFileProcessor.getClass()) ) {
						continue;
					}
					String sNewKeyID = newQuantFileProcessor.getKeyID();
					if( sCurKeyID.equals(sNewKeyID) ) {
						bFound = true;
						break;
					}
				}
				if( ! bFound ) {
					toRemove.add(curQuantFileProcessor);
				}
			}
			if( ! toRemove.isEmpty() ) {
				this.quantFileProcessorsToRemove = toRemove;
				for( QuantFileProcessor oldQuantFileProcessor : this.quantFileProcessorsToRemove ) {
					removePrecursorExternalQuantHeaderData(oldQuantFileProcessor);
					removePrecursorExternalQuantPeakData(oldQuantFileProcessor);
				}
				writeArchive();
			}
		}
		super.setQuantFiles(lQuantFileProcessors);

	}

	protected boolean removePrecursorExternalQuantHeaderData( StandardQuantDataProcessor sqdp ) {
		boolean bRemoved = false;
		try {
			DataHeader dHeader = getGRITSdata().getDataHeader();

			for( Peak peak : sqdp.getSourcePeakListToMatch() ) {
				Double dMz = peak.getMz();
				String sKey = sqdp.getKeyID() + "-" + dMz.toString();
				String sLabel = dMz.toString();
				if( sqdp.getLabelAlias() != null && ! sqdp.getLabelAlias().equals("") ) {
					sLabel = sqdp.getLabelAlias() + "-" + dMz.toString();
				}
				CustomExtraData cedInt = ExternalQuantColumnInfo.getExternalQuantIntensity(sKey, sLabel);
				CustomExtraData cedTotalInt = ExternalQuantColumnInfo.getExternalQuantDeconvolutedIntensity(sKey, sLabel);
				CustomExtraData cedIntMz = ExternalQuantColumnInfo.getExternalQuantIntensityMz(sKey, sLabel);
				if( dHeader.getPeakCustomExtraData().contains(cedIntMz) ) {
					dHeader.getPeakCustomExtraData().remove(cedIntMz);
					bRemoved = true;
				}
				if( dHeader.getPeakCustomExtraData().contains(cedInt) ) {
					dHeader.getPeakCustomExtraData().remove(cedInt);
					bRemoved = true;
				}
				if( dHeader.getPeakCustomExtraData().contains(cedTotalInt) ) {
					dHeader.getPeakCustomExtraData().remove(cedTotalInt);
					bRemoved = true;
				}
				//				CustomExtraData cedCorInt = CorrectedQuantColumnInfo.getCorrectedQuantIntensity(sKey, sLabel);
				CustomExtraData cedCorInt = ExternalQuantColumnInfo.getExternalQuantCorrectedIntensity(sKey, sLabel);
				if( dHeader.getPeakCustomExtraData().contains(cedCorInt) ) {
					dHeader.getPeakCustomExtraData().remove(cedCorInt);
					bRemoved = true;
				}				
				CustomExtraData cedQuantRelInt = StandardQuantColumnInfo.getStandardQuantRelativeIntensity(sKey, sKey);											
				if( dHeader.getPeakCustomExtraData().contains(cedQuantRelInt) ) {
					dHeader.getPeakCustomExtraData().remove(cedQuantRelInt);
					bRemoved = true;
				}
				CustomExtraData cedFileName = ExternalQuantSettings.getExternalQuantFileName(sKey, sLabel);
				CustomExtraData cedTolerance = ExternalQuantSettings.getExternalQuantMatchingTolerance(sKey, sLabel);
				CustomExtraData cedScanNumber = ExternalQuantSettings.getExternalQuantScanNumber(sKey, sLabel);
				CustomExtraData cedUsePPM = ExternalQuantSettings.getExternalQuantUsePPM(sKey, sLabel);

				if( dHeader.getStringProp().containsKey(cedFileName.getKey()) ) {
					dHeader.getStringProp().remove( cedFileName.getKey() );
					bRemoved = true;
				}
				if( dHeader.getDoubleProp().containsKey(cedTolerance.getKey()) ) {
					dHeader.getDoubleProp().remove( cedTolerance.getKey() );
					bRemoved = true;
				}
				if( dHeader.getIntegerProp().containsKey(cedScanNumber.getKey()) ) {
					dHeader.getIntegerProp().remove( cedScanNumber.getKey() );
					bRemoved = true;
				}
				if( dHeader.getBooleanProp().containsKey(cedUsePPM.getKey()) ) {
					dHeader.getBooleanProp().remove( cedUsePPM.getKey() );
					bRemoved = true;
				}
			}
		} catch (Exception e) {
			logger.error("removePrecursorExternalQuantHeaderData: error removing external quant data from DataHeader.");
		}
		return bRemoved;
	}

	protected boolean removePrecursorExternalQuantHeaderData( QuantFileProcessor _processor ) {
		boolean bRemoved = false;
		try {
			DataHeader dHeader = getGRITSdata().getDataHeader();
			String sKey = null;
			String sLabel = null;
			if( _processor instanceof ExtractDataProcessor ) {
				sKey = QuantFileProcessor.getExternalQuantProcessorKey(_processor.getKeyID(), ExtractDataProcessor.DEFAULT_KEY);
				sLabel = QuantFileProcessor.getExternalQuantProcessorLabel(_processor.getLabelAlias(), ExtractDataProcessor.DEFAULT_LABEL);
			} else if ( _processor instanceof FullMzXMLDataProcessor ) {
				sKey = QuantFileProcessor.getExternalQuantProcessorKey(_processor.getKeyID(), FullMzXMLDataProcessor.DEFAULT_KEY);
				sLabel = QuantFileProcessor.getExternalQuantProcessorLabel(_processor.getLabelAlias(), FullMzXMLDataProcessor.DEFAULT_LABEL);
			} else if ( _processor instanceof StandardQuantDataProcessor ) { 
				return removePrecursorExternalQuantHeaderData( (StandardQuantDataProcessor) _processor );
			} else {
				throw new Exception("Processor type not supported: " + _processor.getClass());
			}
			CustomExtraData cedFileName = ExternalQuantSettings.getExternalQuantFileName(sKey, sLabel);
			CustomExtraData cedTolerance = ExternalQuantSettings.getExternalQuantMatchingTolerance(sKey, sLabel);
			CustomExtraData cedScanNumber = ExternalQuantSettings.getExternalQuantScanNumber(sKey, sLabel);
			CustomExtraData cedUsePPM = ExternalQuantSettings.getExternalQuantUsePPM(sKey, sLabel);

			if( dHeader.getStringProp().containsKey(cedFileName.getKey()) ) {
				dHeader.getStringProp().remove( cedFileName.getKey() );
				bRemoved = true;
			}
			if( dHeader.getDoubleProp().containsKey(cedTolerance.getKey()) ) {
				dHeader.getDoubleProp().remove( cedTolerance.getKey() );
				bRemoved = true;
			}
			if( dHeader.getIntegerProp().containsKey(cedScanNumber.getKey()) ) {
				dHeader.getIntegerProp().remove( cedScanNumber.getKey() );
				bRemoved = true;
			}
			if( dHeader.getBooleanProp().containsKey(cedUsePPM.getKey()) ) {
				dHeader.getBooleanProp().remove( cedUsePPM.getKey() );
				bRemoved = true;
			}

			CustomExtraData cedInt = ExternalQuantColumnInfo.getExternalQuantIntensity(sKey, sLabel);
			CustomExtraData cedTotalInt = ExternalQuantColumnInfo.getExternalQuantDeconvolutedIntensity(sKey, sLabel);
			CustomExtraData cedIntMz = ExternalQuantColumnInfo.getExternalQuantIntensityMz(sKey, sLabel);
			CustomExtraData cedCharge = ExternalQuantColumnInfo.getExternalQuantCharge(sKey, sLabel);
			CustomExtraData cedCorrected = ExternalQuantColumnInfo.getExternalQuantCorrectedIntensity(sKey, sLabel);
			if( dHeader.getPeakCustomExtraData().contains(cedIntMz) ) {
				dHeader.getPeakCustomExtraData().remove(cedIntMz);
				bRemoved = true;
			}
			if( dHeader.getPeakCustomExtraData().contains(cedInt) ) {
				dHeader.getPeakCustomExtraData().remove(cedInt);
				bRemoved = true;
			}
			if( dHeader.getPeakCustomExtraData().contains(cedTotalInt) ) {
				dHeader.getPeakCustomExtraData().remove(cedTotalInt);
				bRemoved = true;
			}
			if( dHeader.getPeakCustomExtraData().contains(cedCharge) ) {
				dHeader.getPeakCustomExtraData().remove(cedCharge);
				bRemoved = true;
			}
			if( dHeader.getPeakCustomExtraData().contains(cedCorrected) ) {
				dHeader.getPeakCustomExtraData().remove(cedCorrected);
				bRemoved = true;
			}

			CustomExtraData cedCorInt = CorrectedQuantColumnInfo.getCorrectedQuantIntensity(DMPrecursorPeak.precursor_peak_intensity.name(), 
					DMPrecursorPeak.precursor_peak_intensity.getLabel());								
			if( dHeader.getPeakCustomExtraData().contains(cedCorInt) ) {
				dHeader.getPeakCustomExtraData().remove(cedCorInt);
				bRemoved = true;
			}

			cedCorInt = CorrectedQuantColumnInfo.getCorrectedQuantIntensity(DMPeak.peak_id.name(), DMPeak.peak_id.getLabel());								
			if( dHeader.getPeakCustomExtraData().contains(cedCorInt) ) {
				dHeader.getPeakCustomExtraData().remove(cedCorInt);
				bRemoved = true;
			}

		} catch (Exception e) {
			logger.error("removePrecursorExternalQuantHeaderData: error removing external quant data from DataHeader.");
		}
		return bRemoved;

	}

	protected void removePrecursorExternalQuantPeakData(QuantFileProcessor _processor) {
		int iParentScanNum = -1;
		if ( getMassSpecEntityProperty().getParentScanNum() != null ) { 
			iParentScanNum = getMassSpecEntityProperty().getParentScanNum();
		}
		if( iParentScanNum != -1 ) { // if TIM or DI and no MS1 scan in the MS file, parent scan == 0. But it should be 1 in the Full
			super.loadPrecursorExternalQuantData(iParentScanNum, _processor, true);
		}		
	}

	/**
	 * Loads the MS-based external and internal quantification into the MS Annotation data object so it can be written to the file.
	 * 
	 * @param _processor, the current QuantFileProcessor
	 * @param sKey, the column key
	 * @param sLabel, the column label
	 * @return true if changed/loaded, false otherwise
	 */
	protected boolean loadPrecursorExternalQuantHeaderData( QuantFileProcessor _processor, String sKey, String sLabel  ) {
		boolean bLoaded = false;
		try {
			DataHeader dHeader = getGRITSdata().getDataHeader();
			CustomExtraData cedFileName = ExternalQuantSettings.getExternalQuantFileName(sKey, sLabel);
			CustomExtraData cedInt = ExternalQuantColumnInfo.getExternalQuantIntensity(sKey, sLabel);
			CustomExtraData cedTotalInt = ExternalQuantColumnInfo.getExternalQuantDeconvolutedIntensity(sKey, sLabel);
			CustomExtraData cedIntMz = ExternalQuantColumnInfo.getExternalQuantIntensityMz(sKey, sLabel);
			CustomExtraData cedCharge = ExternalQuantColumnInfo.getExternalQuantCharge(sKey, sLabel);
			CustomExtraData cedTolerance = ExternalQuantSettings.getExternalQuantMatchingTolerance(sKey, sLabel);
			CustomExtraData cedScanNumber = ExternalQuantSettings.getExternalQuantScanNumber(sKey, sLabel);
			CustomExtraData cedUsePPM = ExternalQuantSettings.getExternalQuantUsePPM(sKey, sLabel);

			//			bLoaded = false; 
			if( ! dHeader.getStringProp().containsKey(cedFileName.getKey()) ) {
				dHeader.getStringProp().put( cedFileName.getKey(), sKey);
				bLoaded = true;
			}
			if( ! dHeader.getDoubleProp().containsKey(cedTolerance.getKey()) ) {
				dHeader.addDoubleProp( cedTolerance.getKey(), _processor.getSettings().getIntensityCorrectionValue());
				bLoaded = true;
			}
			if( ! dHeader.getIntegerProp().containsKey(cedScanNumber.getKey()) ) {
				dHeader.addIntegerProp( cedScanNumber.getKey(), _processor.getSettings().getTargetScanNumber());
				bLoaded = true;
			}
			if( ! dHeader.getBooleanProp().containsKey(cedUsePPM.getKey()) ) {
				dHeader.addbooleanProp( cedUsePPM.getKey(), _processor.getSettings().isIntensityCorrectionPpm());
				bLoaded = true;
			}
			boolean bRemove = false;
			for( CustomExtraData ced : dHeader.getPeakCustomExtraData() ) {
				if( ced.getKey().equals(cedIntMz.getKey()) && ! ced.getLabel().equals(cedIntMz.getLabel()) ) {
					bRemove = true;
				}
			}
			if( bRemove ) {
				dHeader.getPeakCustomExtraData().remove(cedIntMz);
			}			
			if( ! dHeader.getPeakCustomExtraData().contains(cedIntMz) ) {
				dHeader.getPeakCustomExtraData().add(  cedIntMz );
				bLoaded = true;
			} 

			bRemove = false;
			for( CustomExtraData ced : dHeader.getPeakCustomExtraData() ) {
				if( ced.getKey().equals(cedInt.getKey()) && ! ced.getLabel().equals(cedInt.getLabel()) ) {
					bRemove = true;
				}
			}
			if( bRemove ) {
				dHeader.getPeakCustomExtraData().remove(cedInt);
			}						
			if( ! dHeader.getPeakCustomExtraData().contains(cedInt) ) {
				dHeader.getPeakCustomExtraData().add( cedInt );
				bLoaded = true;
			}

			bRemove = false;
			for( CustomExtraData ced : dHeader.getPeakCustomExtraData() ) {
				if( ced.getKey().equals(cedTotalInt.getKey()) && ! ced.getLabel().equals(cedTotalInt.getLabel()) ) {
					bRemove = true;
				}
			}
			if( bRemove ) {
				dHeader.getPeakCustomExtraData().remove(cedTotalInt);
			}									
			if( ! dHeader.getPeakCustomExtraData().contains(cedTotalInt) ) {
				dHeader.getPeakCustomExtraData().add( cedTotalInt );
				bLoaded = true;
			}

			boolean bFound = false;
			if( getSimianTableDataObject().getLastHeader() != null ) {
				CustomExtraData cedCorInt = CorrectedQuantColumnInfo.getCorrectedQuantIntensity(DMPrecursorPeak.precursor_peak_intensity.name(), 
						DMPrecursorPeak.precursor_peak_intensity.getLabel());				
				for( GRITSColumnHeader gritsHeader : getSimianTableDataObject().getLastHeader() ) {
					if( gritsHeader.getKeyValue().equals(cedCorInt.getKey()) ) {
						bFound = true;
					}
				}
				if( bFound ) {
					bRemove = false;
					for( CustomExtraData ced : dHeader.getPeakCustomExtraData() ) {
						if( ced.getKey().equals(cedCorInt.getKey()) && ! ced.getLabel().equals(cedCorInt.getLabel()) ) {
							bRemove = true;
						}
					}
					if( bRemove ) {
						dHeader.getPeakCustomExtraData().remove(cedCorInt);
					}									
					if( ! dHeader.getPeakCustomExtraData().contains(cedCorInt) ) {
						dHeader.getPeakCustomExtraData().add( cedCorInt );
						bLoaded = true;
					}			
				}

				bFound = false;
				cedCorInt = CorrectedQuantColumnInfo.getCorrectedQuantIntensity(DMPeak.peak_id.name(), DMPeak.peak_id.getLabel());								
				for( GRITSColumnHeader gritsHeader : getSimianTableDataObject().getLastHeader() ) {
					if( gritsHeader.getKeyValue().equals(cedCorInt.getKey()) ) {
						bFound = true;
					}
				}
				if( bFound ) {
					bRemove = false;
					for( CustomExtraData ced : dHeader.getPeakCustomExtraData() ) {
						if( ced.getKey().equals(cedCorInt.getKey()) && ! ced.getLabel().equals(cedCorInt.getLabel()) ) {
							bRemove = true;
						}
					}
					if( bRemove ) {
						dHeader.getPeakCustomExtraData().remove(cedCorInt);
					}									
					if( ! dHeader.getPeakCustomExtraData().contains(cedCorInt) ) {
						dHeader.getPeakCustomExtraData().add( cedCorInt );
						bLoaded = true;
					}			
				}

				bFound = false;
				cedCorInt = ExternalQuantColumnInfo.getExternalQuantCorrectedIntensity(sKey, sLabel);
				for( GRITSColumnHeader gritsHeader : getSimianTableDataObject().getLastHeader() ) {
					if( gritsHeader.getKeyValue().equals(cedCorInt.getKey()) ) {
						bFound = true;
					}
				}
				if( bFound ) {
					bRemove = false;
					for( CustomExtraData ced : dHeader.getPeakCustomExtraData() ) {
						if( ced.getKey().equals(cedCorInt.getKey()) && ! ced.getLabel().equals(cedCorInt.getLabel()) ) {
							bRemove = true;
						}
					}
					if( bRemove ) {
						dHeader.getPeakCustomExtraData().remove(cedCorInt);
					}									
					if( ! dHeader.getPeakCustomExtraData().contains(cedCorInt) ) {
						dHeader.getPeakCustomExtraData().add( cedCorInt );
						bLoaded = true;
					}			
				}
			}
			if( _processor instanceof ExtractDataProcessor ) {
				bRemove = false;
				for( CustomExtraData ced : dHeader.getPeakCustomExtraData() ) {
					if( ced.getKey().equals(cedCharge.getKey()) && ! ced.getLabel().equals(cedCharge.getLabel()) ) {
						bRemove = true;
					}
				}
				if( bRemove ) {
					dHeader.getPeakCustomExtraData().remove(cedCharge);
				}									
				if( ! dHeader.getPeakCustomExtraData().contains(cedCharge) ) {
					dHeader.getPeakCustomExtraData().add( cedCharge );
					bLoaded = true;
				}
			} else if ( _processor instanceof FullMzXMLDataProcessor ) {
				;
			} else if ( _processor instanceof StandardQuantDataProcessor ) {
				CustomExtraData cedRelInt = StandardQuantColumnInfo.getStandardQuantRelativeIntensity(sKey, sLabel);
				bRemove = false;
				for( CustomExtraData ced : dHeader.getPeakCustomExtraData() ) {
					if( ced.getKey().equals(cedRelInt.getKey()) && ! ced.getLabel().equals(cedRelInt.getLabel()) ) {
						bRemove = true;
					}
				}
				if( bRemove ) {
					dHeader.getPeakCustomExtraData().remove(cedRelInt);
				}									
				if( ! dHeader.getPeakCustomExtraData().contains(cedRelInt) ) {
					dHeader.getPeakCustomExtraData().add( cedRelInt );
					bLoaded = true;
				}
			} else {
				throw new Exception("Processor type not supported: " + _processor.getClass());
			}		
		} catch (IOException e) {
			logger.error("loadExternalQuantData: error writing Data Header with external quant changes.", e);
		} catch (Exception e) {
			logger.error("loadExternalQuantData: error writing Data Header with external quant changes.", e);
		}
		return bLoaded;
	}

	/**
	 * Loads the MS-based external and internal quantification into the MS Annotation data object so it can be written to the file.
	 * 
	 * @param _processor
	 * @return true if changed/loaded, false otherwise
	 */
	protected boolean loadPrecursorExternalQuantHeaderData( QuantFileProcessor _processor ) {
		boolean bLoaded = false;
		try {
			if( _processor instanceof ExtractDataProcessor ) {
				String sKey = QuantFileProcessor.getExternalQuantProcessorKey(_processor.getKeyID(), ExtractDataProcessor.DEFAULT_KEY);
				String sLabel = QuantFileProcessor.getExternalQuantProcessorLabel(_processor.getLabelAlias(), ExtractDataProcessor.DEFAULT_LABEL);
				bLoaded = loadPrecursorExternalQuantHeaderData(_processor, sKey, sLabel);
			} else if ( _processor instanceof FullMzXMLDataProcessor ) {
				String sKey = QuantFileProcessor.getExternalQuantProcessorKey(_processor.getKeyID(), FullMzXMLDataProcessor.DEFAULT_KEY);
				String sLabel = QuantFileProcessor.getExternalQuantProcessorLabel(_processor.getLabelAlias(), FullMzXMLDataProcessor.DEFAULT_LABEL);
				bLoaded = loadPrecursorExternalQuantHeaderData(_processor, sKey, sLabel);
			} else if ( _processor instanceof StandardQuantDataProcessor ) {
				StandardQuantDataProcessor sqdp = (StandardQuantDataProcessor) _processor;
				for( Peak peak : sqdp.getSourcePeakListToMatch() ) {
					Double dMz = peak.getMz();
					String sKey = sqdp.getKeyID() + "-" + dMz.toString();
					String sLabel = dMz.toString();
					if( sqdp.getLabelAlias() != null && ! sqdp.getLabelAlias().equals("") ) {
						sLabel = sqdp.getLabelAlias() + "-" + dMz.toString();
					}
					bLoaded = loadPrecursorExternalQuantHeaderData(_processor, sKey, sLabel);
				}
			} else {
				throw new Exception("Processor type not supported: " + _processor.getClass());
			}
		} catch (IOException e) {
			logger.error("loadExternalQuantData: error writing Data Header with external quant changes.", e);
		} catch (Exception e) {
			logger.error("loadExternalQuantData: error writing Data Header with external quant changes.", e);
		}
		return bLoaded;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#loadPrecursorExternalQuantData(int, org.grits.toolbox.entry.ms.extquantfiles.process.ExternalQuantFileProcessor)
	 */
	@Override
	protected boolean loadPrecursorExternalQuantData(int _iParentScanNum, QuantFileProcessor _processor, boolean _bRemove) {
		boolean bLoaded = super.loadPrecursorExternalQuantData(_iParentScanNum, _processor, _bRemove);
		bLoaded |= loadPrecursorExternalQuantHeaderData(_processor);
		return bLoaded;
	}

	/**
	 * @return MSAnnotationTableDataObject - casts the TableDataObject to MSAnnotationTableDataObject
	 */
	private MSAnnotationTableDataObject getMySimianTableDataObject() {
		return (MSAnnotationTableDataObject) getSimianTableDataObject();
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#addHeaderLine(int, org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader, java.util.ArrayList)
	 */
	@Override
	protected void addHeaderLine( int iPrefColNum, GRITSColumnHeader colHeader, ArrayList<GRITSColumnHeader> alHeader ) {   	
		if ( colHeader.getKeyValue().equals( DMScan.scan_scanNo.name()) || colHeader.getKeyValue().equals( DMScan.scan_pseudoScanNo.name()) ) {
			this.getMySimianTableDataObject().addScanNoCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMScan.scan_parentScan.name() ) ) {
			this.getMySimianTableDataObject().addParentNoCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMFeature.feature_id.name() ) ) {
			this.getMySimianTableDataObject().addFeatureIdCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMFeature.feature_sequence.name() ) ) {
			this.getMySimianTableDataObject().addSequenceCol(iPrefColNum);		
		} else if ( colHeader.getKeyValue().equals( DMAnnotation.annotation_id.name() ) ) {
			this.getMySimianTableDataObject().addAnnotationIdCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMAnnotation.annotation_string_id.name() ) ) {
			this.getMySimianTableDataObject().addAnnotationStringIdCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMPeak.peak_id.name() ) ) {
			this.getMySimianTableDataObject().addPeakIdCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMPeak.peak_mz.name() ) ) {
			this.getMySimianTableDataObject().addMzCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMPeak.peak_intensity.name() ) ) {
			this.getSimianTableDataObject().addPeakIntensityCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMPeak.peak_is_precursor.name() ) ) {
			this.getSimianTableDataObject().addPeakIsPrecursorCol(iPrefColNum);
		} else if ( colHeader.getKeyValue().equals( DMPrecursorPeak.precursor_peak_intensity.name()) ) {
			this.getSimianTableDataObject().addPrecursorIntensityCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(TableDataProcessor.filterColHeader.getKeyValue())) {
			this.getMySimianTableDataObject().addFilterCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(TableDataProcessor.commentColHeader.getKeyValue())) {
			this.getMySimianTableDataObject().addCommentCol(iPrefColNum);
		} else if (colHeader.getKeyValue().equals(DMAnnotation.annotation_ratio.name())) {
			this.getMySimianTableDataObject().addRatioCol(iPrefColNum);
		}
		MassSpecTableDataProcessorUtil.setHeaderValue(iPrefColNum, colHeader, alHeader);
	}

	@Override
	protected InternalStandardQuantFileList getInternalStandardQuantFileList(MSPropertyDataFile quantFile) {
		MSAnnotationMetaData msSettings = getMSAnnotationEntityProperty().getMSAnnotationParentProperty().getMSAnnotationMetaData();
		String sExtQuantType = MassSpecUISettings.getExternalQuantType(quantFile);
		if( msSettings.getInternalStandardQuantFiles() == null || msSettings.getInternalStandardQuantFiles().isEmpty() ) {
			return null;
		}

		InternalStandardQuantFileList isqfl = msSettings.getInternalStandardQuantFiles().get(sExtQuantType);
		return isqfl;		
	}

	@Override
	protected List<MassSpecStandardQuant> getStandardQuantitationList() {
		if( getMSAnnotationEntityProperty().getMSAnnotationParentProperty().getMSAnnotationMetaData() != null &&
				getMSAnnotationEntityProperty().getMSAnnotationParentProperty().getMSAnnotationMetaData().getStandardQuant() != null &&
				! getMSAnnotationEntityProperty().getMSAnnotationParentProperty().getMSAnnotationMetaData().getStandardQuant().isEmpty() ) {
			MSAnnotationMetaData msSettings = getMSAnnotationEntityProperty().getMSAnnotationParentProperty().getMSAnnotationMetaData();
			return msSettings.getStandardQuant();
		}
		return null;
	}

	@Override
	protected HashMap<String, InternalStandardQuantFileList> getStandardQuantitationFileList() {
		if( getMSAnnotationEntityProperty().getMSAnnotationParentProperty().getMSAnnotationMetaData() != null &&
				getMSAnnotationEntityProperty().getMSAnnotationParentProperty().getMSAnnotationMetaData().getInternalStandardQuantFiles() != null &&
				! getMSAnnotationEntityProperty().getMSAnnotationParentProperty().getMSAnnotationMetaData().getInternalStandardQuantFiles().isEmpty() ) {
			MSAnnotationMetaData msSettings = getMSAnnotationEntityProperty().getMSAnnotationParentProperty().getMSAnnotationMetaData();
			return msSettings.getInternalStandardQuantFiles();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#addScanRow(org.grits.toolbox.ms.om.data.Scan)
	 */
	@Override
	protected void addScanRow(Scan msScan) {
		final DecimalFormat formatDec4 = new DecimalFormat("0.0000");
		// Special handling of LC-MS/MS Data.
		// If LC-MS/MS, create an overview MS1 row so the user has the option to open a view of all MS2s in the next editor

		// If not LC, then handle normally
		if( ! getMethod().getMsType().equals(Method.MS_TYPE_LC) ) {
			super.addScanRow(msScan);
			return;
		}

		if( (getMassSpecEntityProperty().getScanNum() != null && getMassSpecEntityProperty().getScanNum() != -1) ||
				(getMassSpecEntityProperty().getParentScanNum() != null && getMassSpecEntityProperty().getParentScanNum() != -1 ) ||
				(getMassSpecEntityProperty().getMsLevel() != null && getMassSpecEntityProperty().getMsLevel() != 1) ) {
			super.addScanRow(msScan);
		}

		if( this.overviewRow == null ) {
			this.overviewRow = getNewRow();
			// initialize with the data from the first scan
			MassSpecTableDataProcessorUtil.fillMassSpecScanData(msScan, this.overviewRow.getDataRow(), 
					getTempPreference().getPreferenceSettings());
			// Except overwrite the scan number to 0 to indicate the overview scan
			MassSpecTableDataProcessorUtil.setRowValue(
					getTempPreference().getPreferenceSettings().getColumnPosition(DMScan.scan_scanNo.name()), 
					Integer.valueOf(0), this.overviewRow.getDataRow());

			// Retention time doesn't make sense either, so set to 0.0
			MassSpecTableDataProcessorUtil.setRowValue(
					getTempPreference().getPreferenceSettings().getColumnPosition(DMScan.scan_retentionTime.name()), 
					Double.valueOf(0.0), this.overviewRow.getDataRow());
			getSimianTableDataObject().getTableData().add(this.overviewRow);				    	
		}
		if( msScan.getScanStart() != null ) {
			Double dCurScanStart = (Double) this.overviewRow.getDataRow().get(
					getTempPreference().getPreferenceSettings().getColumnPosition(DMScan.scan_scanStart.name()) );
			if( dCurScanStart != null && dCurScanStart > msScan.getScanStart() ) {
				MassSpecTableDataProcessorUtil.setRowValue(
						getTempPreference().getPreferenceSettings().getColumnPosition(DMScan.scan_scanStart.name()), 
						new Double(formatDec4.format(msScan.getScanStart())), this.overviewRow.getDataRow());				
			}
		}
		if( msScan.getScanEnd() != null ) {
			Double dCurScanEnd = (Double) this.overviewRow.getDataRow().get(
					getTempPreference().getPreferenceSettings().getColumnPosition(DMScan.scan_scanEnd.name()) );
			if( dCurScanEnd != null && dCurScanEnd < msScan.getScanEnd() ) {
				MassSpecTableDataProcessorUtil.setRowValue(
						getTempPreference().getPreferenceSettings().getColumnPosition(DMScan.scan_scanEnd.name()), 
						new Double(formatDec4.format(msScan.getScanEnd())), this.overviewRow.getDataRow());				
			}
		}
		if( msScan.getSubScans() != null ) {
			Integer iNumSubScans = (Integer) this.overviewRow.getDataRow().get(
					getTempPreference().getPreferenceSettings().getColumnPosition(DMScan.scan_numsubscans.name()) );
			iNumSubScans += msScan.getSubScans().size();

			MassSpecTableDataProcessorUtil.setRowValue(
					getTempPreference().getPreferenceSettings().getColumnPosition(DMScan.scan_numsubscans.name()), 
					iNumSubScans, this.overviewRow.getDataRow());
		}
		if( msScan.getNumAnnotatedPeaks() != null ) {
			Integer iNumAnnotatedPeaks = (Integer) this.overviewRow.getDataRow().get(
					getTempPreference().getPreferenceSettings().getColumnPosition(DMScan.scan_numannotatedpeaks.name()) );
			if( iNumAnnotatedPeaks == null ) {
				iNumAnnotatedPeaks  = Integer.valueOf(0);
			}
			iNumAnnotatedPeaks += msScan.getNumAnnotatedPeaks();			
			MassSpecTableDataProcessorUtil.setRowValue(
					getTempPreference().getPreferenceSettings().getColumnPosition(DMScan.scan_numannotatedpeaks.name()), 
					iNumAnnotatedPeaks, this.overviewRow.getDataRow());
		}

		// add the current row as usual
		super.addScanRow(msScan);	
	}

	/**
	 * Not used!
	 */
	protected void addPeaksTableDataForLCMS() {
		try {
			HashMap<Integer,Scan> rawData = (HashMap) getGRITSdata().getScans();
			Set<Integer> scans = rawData.keySet();
			List<Integer> lScans = new ArrayList<>(scans.size());
			lScans.addAll(scans);

			Collections.sort(lScans);

			for( Integer iScan : lScans ) {
				((MSAnnotationEntityProperty) getEntry().getProperty()).setScanNum(iScan);
				String sSourceFile = getScanArchiveFile();				
				if( new File(sSourceFile).exists() ) {
					this.data = this.xmlReader.readDataWithoutFeatures(sSourceFile);
					getMassSpecEntityProperty().setParentScanNum(iScan);
					addPeaksTableData();
					getGRITSdata().getScanFeatures().clear();
					getGRITSdata().getAnnotation().clear();
					getGRITSdata().getScans().clear();
				}
				((MSAnnotationEntityProperty) getEntry().getProperty()).setScanNum(-1);		
				getMassSpecEntityProperty().setParentScanNum(-1);
				if( progressBarDialog.isCanceled() ) {
					break;
				}
			}
			getGRITSdata().setScans(rawData);
			getGRITSdata().getScanFeatures().clear();
			getGRITSdata().getAnnotation().clear();
		} catch (Exception e) {
			logger.error("addPeaksTableDataForLCMS: error building top level table for LC-MSMS data", e);
			getMassSpecEntityProperty().setParentScanNum(-1);
			((MSAnnotationEntityProperty) getEntry().getProperty()).setScanNum(-1);
		}

	}

	/**
	 * Implemented for use with the LC-MS/MS overview scan (0) to ensure that the selected scan is set in the property so the features
	 * can be loaded and written correctly.
	 * 
	 * @param iScanNum
	 */
	public void setPropertyScanNum( int iScanNum ) {
		((MSAnnotationEntityProperty) getEntry().getProperty()).setScanNum(iScanNum);
	}

	/**
	 * Checks the current value of curScanFeatures. If set and the associated scan num is the
	 * same as that which is passed to method, then return the curScanFeatures. Otherwise,
	 * load the scan features from the file.
	 * 
	 * @param iScanNum
	 * @return
	 */
	public ScanFeatures getScanFeatures( int iScanNum ) {
		ScanFeatures features = null;
		if( getCurScanFeature() != null && getCurScanFeature().getScanId() == iScanNum )
			features = getCurScanFeature();
		else { 
			features = loadScanFeatureFromXML(iScanNum);
		}
		return features;
	}

	/**
	 * Adds the peak table for a view that is not the overview (not the first page). This page
	 * may require update and save if modified (e.g. selection change)
	 */
	protected void addPeaksTableDataNonOverview() {
		if( getMassSpecEntityProperty().getParentScanNum() == null ||  getMassSpecEntityProperty().getParentScanNum().equals(-1) )
			return;
		int iScanNum = getMassSpecEntityProperty().getParentScanNum();
		// TODO: if direct infusion, we are merging all precursors from all MS1 scans
		// However, if selected annotations change, then those are reflected in the *particular* parent scan
		// For now, always pull the feature for the first ms1 scan regardless of which one is clicked
		// we will then update just that xml file.
		// BUT, now the xml files for ms1 scans are not synched...this needs to be fixed

		if( getMethod().getMsType().equals(Method.MS_TYPE_INFUSION) ) {
			Scan scan = getGRITSdata().getScans().get( getMassSpecEntityProperty().getParentScanNum() );
			if( scan.getMsLevel() == 1 ) {
				iScanNum = getGRITSdata().getFirstMS1Scan();
			} 
		}
		ScanFeatures features = getScanFeatures(iScanNum);

		boolean bUpdateFeature = false;
		if (features != null) {
			bUpdateFeature = addAnnotationData(features, getTempPreference().getPreferenceSettings());
		}
		setCurScanFeature(features);
		if( bUpdateFeature ) { // this used to only update if MS type was infusion. No idea why? Without this update, then it won't convert older projects to support FeatureSelection
			//		if( bUpdateFeature && getMethod().getMsType().equals(Method.MS_TYPE_INFUSION) ) {
			String sSourceFile = getScanArchiveFile();
			this.xmlWriter = new AnnotationWriter();
			try {
				this.xmlWriter.writeSingleScanToZipFile(iScanNum, features, sSourceFile);
			} catch( Exception e ) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * @return Annotation - current Annotation object associated with the MSAnnotationEntityProperty annotation id
	 */
	protected Annotation getCurrentParentAnnotation() {
		MSAnnotationEntityProperty prop = (MSAnnotationEntityProperty) getSourceProperty();
		Annotation parentAnnotation = AnnotationRowExtraction.getAnnotation(getGRITSdata(), prop.getAnnotationId());
		return parentAnnotation;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor#addPeaksTableData()
	 */
	@Override
	protected void addPeaksTableData() {	
		if( ! getMethod().getMsType().equals(Method.MS_TYPE_LC) ) {
			addPeaksTableDataNonOverview();
			Annotation parentAnnotation = getCurrentParentAnnotation();
			setCurAnnotation(parentAnnotation);		
			return;
		}

		if( (getMassSpecEntityProperty().getScanNum() != null && getMassSpecEntityProperty().getScanNum() != -1) ||
				(getMassSpecEntityProperty().getParentScanNum() != null && getMassSpecEntityProperty().getParentScanNum() != 0 ) ||
				(getMassSpecEntityProperty().getMsLevel() != null && getMassSpecEntityProperty().getMsLevel() != 2) ) {
			addPeaksTableDataNonOverview();
			Annotation parentAnnotation = getCurrentParentAnnotation();
			setCurAnnotation(parentAnnotation);		
			return;
		}
		int iCnt = 1;
		for( int iScan = 0; iScan < getScanData().size(); iScan++ ) {
			if( (iCnt%100) == 0 ) {
				this.progressBarDialog.getMinorProgressBarListener(0).setProgressMessage("Building overview LC-MS/MS table. Scan: " + iCnt + " of " + getScanData().size());
				this.progressBarDialog.getMinorProgressBarListener(0).setProgressValue(iCnt);
			}
			iCnt++;
			Scan msScan = getScanData().get(iScan);
			if( msScan == null || msScan.getMsLevel() != 1 ) {
				continue;
			}
			getMassSpecEntityProperty().setParentScanNum(msScan.getScanNo());
			addPeaksTableDataNonOverview();			
		}
		getMassSpecEntityProperty().setParentScanNum(0);
		logger.debug("Number of rows in table: " + simianTableDataObject.getTableData().size());
		Annotation parentAnnotation = getCurrentParentAnnotation();
		setCurAnnotation(parentAnnotation);		
	}


	/**
	 * @param iAnnotId - Integer value of annotation id
	 * @return Annotation
	 */
	public Annotation getAnnotation(Integer iAnnotId) {
		return AnnotationRowExtraction.getAnnotation(getGRITSdata(), iAnnotId);
	}

	/**
	 * @param iScanNo - Integer value of a scan number
	 * @return Scan - the Scan object in the Data object associated w/ the specified scan number
	 */
	public Scan getScan( Integer iScanNo ) {
		return getGRITSdata().getScans().get(iScanNo);
	}

	/**
	 * Description: builds a map of Feature to Peak for the current ParentScanNum (in current entity property)
	 * 
	 * @return HashMap<Feature, Peak>
	 */
	public HashMap<Feature, Peak> getFeaturesForPeak() {
		try {
			HashMap<String, List<Feature>> htPeakToFeatures = null;
			HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> htParentScanToParentPeaksToSubScan = null;
			ScanFeatures features = loadScanFeatureFromXML(getCurEntityProperty().getParentScanNum());
			htPeakToFeatures = AnnotationRowExtraction.createRowIdToFeatureHash(features);		
			boolean bNeedsConvert = false;
			if( htPeakToFeatures.isEmpty() ) { // no features by row ids? Is this an old project? try the old way
				htPeakToFeatures = AnnotationRowExtraction.createPeakIdToFeatureHash(features);
				bNeedsConvert = true;
			}
			if( htPeakToFeatures.isEmpty() ) {
				return null; // fail
			}
			htParentScanToParentPeaksToSubScan = AnnotationRowExtraction.createParentScanToParentPeaksToSubScanHash(getGRITSdata());
			if( getMethod().getMsType().equals(Method.MS_TYPE_INFUSION) ) {
				AnnotationRowExtraction.updateParentScanToParentPeaksToSubScanHashForDirectInfusion(htParentScanToParentPeaksToSubScan, getGRITSdata());
			}	
			HashMap<Feature, Peak> featList = new HashMap<>();
			for (Peak peak : features.getScanPeaks() ) {
				if( peak == null) {
					continue;
				}
				ArrayList<Scan> precursorScans = AnnotationRowExtraction.getPrecursorScan(getGRITSdata(),
						getCurEntityProperty().getParentScanNum(), peak.getId(), htParentScanToParentPeaksToSubScan);

				// kinda hacky way to handle the conversion of associated features to peaks to features to row ids
				// the first time we load the "old" way, the row ids won't have any scan numbers in the keys
				// so we have to check both with and without scan numbers
				if( bNeedsConvert ) {
					AnnotationRowExtraction.convertPeakIdsToRowIds( getGRITSdata(), features, getCurEntityProperty().getParentScanNum(), 
							peak.getId(), precursorScans, htPeakToFeatures);
					getMySimianTableDataObject().setUsesComplexRowId(features.getUsesComplexRowId());
				}

				for( Scan precursorScan : precursorScans ) {
					if( precursorScan == null )
						continue;
					if(	! getCurEntityProperty().getScanNum().equals( precursorScan.getScanNo()) ) {
						continue;
					}
					String sRowId = Feature.getRowId(peak.getId(), precursorScan.getScanNo(), features.getUsesComplexRowId());
					if( htPeakToFeatures.containsKey(sRowId) ) {    			
						List<Feature> alFeatures = htPeakToFeatures.get(sRowId);
						for( Feature feature : alFeatures ) {
							featList.put(feature, peak);					
						}
					}
				}		
			}
			return featList;
		} catch( Exception ex ) {
			logger.error("Error getting features for peak in getFeaturesForPeak", ex );
		}
		return null;
	}

	@Override
	protected boolean addPeaksData(Integer _parentScanNum, Scan _scan, Peak _peak, TableViewerColumnSettings _settings,
			GRITSListDataRow alRow) {
		//		boolean bAddQuant = false;
		//		if( this.fillType == FillTypes.PeaksWithFeatures ) {
		//			bAddQuant |= MassSpecTableDataProcessorUtil.fillMassSpecScanDataCorrectedPeakScanData(_peak, alRow.getDataRow(), _settings, getMassSpecEntityProperty());
		//			if( this.quantFileProcessors != null &&
		//					_scan != null &&
		//					_scan.getPrecursor() != null ) {
		//				for( QuantFileProcessor quantFileProcessor : this.quantFileProcessors ) {
		//					Peak extPeak = getPrecursorPeak(_scan.getPrecursor().getMz(), quantFileProcessor.getSettings().getTargetScanNumber());
		//					MassSpecTableDataProcessorUtil.fillMassSpecScanDataCorrectedPeakScanData(extPeak, alRow.getDataRow(), _settings, getMassSpecEntityProperty(), quantFileProcessor);
		//				}
		// now that update any relative quantitation
		//				for( QuantFileProcessor quantFileProcessor : this.quantFileProcessors ) {				
		//					if ( quantFileProcessor instanceof StandardQuantDataProcessor ) {
		//						Peak extPeak = getPrecursorPeak(_scan.getPrecursor().getMz(), quantFileProcessor.getSettings().getTargetScanNumber());
		//						MassSpecTableDataProcessorUtil.fillMassSpecScanDataStandardQuantPeakScanData(extPeak, alRow.getDataRow(), 
		//								_settings, getMassSpecEntityProperty(), (StandardQuantDataProcessor) quantFileProcessor);
		//					}				
		//				}
		//			}
		//		}
		//		bAddQuant |= super.addPeaksData(_parentScanNum, _scan, _peak, _settings, alRow);		
		//		return bAddQuant;
		return super.addPeaksData(_parentScanNum, _scan, _peak, _settings, alRow);
	}


	/**
	 * Description:  This method populates the MSAnnotation table!
	 * 
	 * @param _features - a ScanFeatures object
	 * @param _settings - a TableViewerColumnSettings object
	 * @return boolean - whether or not data was converted or external quant added during load
	 */
	protected boolean addAnnotationData( ScanFeatures _features, TableViewerColumnSettings _settings) {
		getMySimianTableDataObject().setUsesComplexRowId(_features.getUsesComplexRowId());
		
		List<Integer> alUnAnnotatedRows = getMySimianTableDataObject().getUnAnnotatedRows();
		HashMap<String, List<Feature>> htPeakToFeatures = null;		
		HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> htParentScanToParentPeaksToSubScan = null;
		htPeakToFeatures = AnnotationRowExtraction.createRowIdToFeatureHash(_features);
		boolean bNeedsConvert = false;
		boolean bDirty = false; // if we add external quant, set to dirty so we know to re-save
		if( htPeakToFeatures.isEmpty() ) { // no features by row ids? Is this an old project? try the old way
			htPeakToFeatures = AnnotationRowExtraction.createPeakIdToFeatureHash(_features);
			if( ! htPeakToFeatures.isEmpty() ) {
				bNeedsConvert = true;
				bDirty = true;
			}
		}

		htParentScanToParentPeaksToSubScan = AnnotationRowExtraction.createParentScanToParentPeaksToSubScanHash(getGRITSdata());
		if( getMethod().getMsType().equals(Method.MS_TYPE_INFUSION) && getMassSpecEntityProperty().getMsLevel() - 1 == 1) {
			AnnotationRowExtraction.updateParentScanToParentPeaksToSubScanHashForDirectInfusion(htParentScanToParentPeaksToSubScan, getGRITSdata()) ;
		}	
		Scan scan = getGRITSdata().getScans().get(getMassSpecEntityProperty().getParentScanNum());

		Integer iParentScan = scan.getScanNo();
		int iArchiveParentScan = iParentScan;
		if( getMethod().getMsType().equals(Method.MS_TYPE_INFUSION) ) {
			iArchiveParentScan = getGRITSdata().getFirstMS1Scan();
		}

		Annotation parentAnnot = null;
		if( getMSAnnotationEntityProperty().getAnnotationId() != null ) {
			parentAnnot = AnnotationRowExtraction.getAnnotation(getGRITSdata(), getMSAnnotationEntityProperty().getAnnotationId());
		}
		for (Peak peak : _features.getScanPeaks() ) {
			if( peak == null) {
				continue;
			}
			// if external quant was removed then we must update the archive
			boolean bAddedQuant = removeExternalQuantPeaksData(peak);
			if( bAddedQuant && ! bDirty ) {
				bDirty = true;
			}
			ArrayList<Scan> precursorScans = AnnotationRowExtraction.getPrecursorScan(getGRITSdata(),
					iParentScan, peak.getId(), htParentScanToParentPeaksToSubScan);

			// kinda hacky way to handle the conversion of associated features to peaks to features to row ids
			// the first time we load the "old" way, the row ids won't have any scan numbers in the keys
			// so we have to check both with and without scan numbers
			if( bNeedsConvert ) {
				AnnotationRowExtraction.convertPeakIdsToRowIds( getGRITSdata(), _features, iParentScan, peak.getId(), precursorScans, htPeakToFeatures);
				getMySimianTableDataObject().setUsesComplexRowId(_features.getUsesComplexRowId());
			}

			for( Scan precursorScan : precursorScans ) {
				Integer iParentScanNumber = null;
				if( precursorScan != null ) {
					iParentScanNumber = precursorScan.getParentScan();
				} else { // must be the real parent scan 
					iParentScanNumber = scan.getScanNo();
				}
				boolean bAtLeastOne = false;
				String sFirstFeatureId = null;
				int iNumMatch = 0;

				String sRowId = Feature.getRowId(peak.getId(), precursorScan != null ? precursorScan.getScanNo() : null, _features.getUsesComplexRowId());
				if( htPeakToFeatures.containsKey(sRowId) ) {    
					List<Feature> alFeatures = htPeakToFeatures.get(sRowId);
					for( Feature feature : alFeatures ) {
						if( parentAnnot != null ) {
							Annotation annot = feature.getParentAnnotation();
							if( annot == null ){
								continue;
							}
							if( ! annot.getId().equals(parentAnnot.getId()) ) {
								continue;
							}
							String sParentFeatureId = getMSAnnotationEntityProperty().getFeatureId();
							if( feature.getParentId() != null && sParentFeatureId != null && ! feature.getParentId().equals(sParentFeatureId) ) {
								continue;
							}
						}
						iNumMatch++;
					}
				}

				if( iNumMatch > 0 ) {

					List<Feature> alFeatures = htPeakToFeatures.get(sRowId);
					for( Feature feature : alFeatures ) {    
						if( parentAnnot != null ) {
							Annotation annot = feature.getParentAnnotation();
							if( annot == null ){
								continue;
							}
							if( ! annot.getId().equals(parentAnnot.getId()) ) {
								continue;
							}
						}
						setCurFeature(feature);

						// this is a hack to avoid printing peaks that don't belong to this parent scan
						// the current annotation method assumes direct infusion and creates an MS1 "scan" from all precursors over entire run!
						if( ! getMethod().getMsType().equals(Method.MS_TYPE_INFUSION) && 
								scan.getMsLevel() == 1 && precursorScans.size() == 1 && precursorScans.get(0) == null) 
							continue;
						FeatureSelection fs = Feature.getFeatureSelection(feature, sRowId);
						if( fs.getManuallySelected() ) {
							getSimianTableDataObject().addManuallyChangedPeak(iArchiveParentScan, sRowId);
						}
						GRITSListDataRow alRow = getNewRow();
						bAddedQuant |= addPeaksData(iParentScanNumber, precursorScan, peak, _settings, alRow);						
						bAddedQuant |= addSubScanPeaksData(precursorScan, peak, _settings, alRow);
						if( bAddedQuant && ! bDirty ) {
							bDirty = true;
						}
						fillFeatureData(feature, _settings, alRow);
						fillAnnotationData(feature.getParentAnnotation(), feature, precursorScan, iNumMatch, _settings, alRow); 
						// add comment column, get it from FeatureSelection
						MassSpecTableDataProcessorUtil.setRowValue(_settings.getColumnPosition( TableDataProcessor.commentColHeader.getKeyValue()), 
								fs.getComment(), alRow.getDataRow()); 
						// add ratio column, get it from FeatureSelection
						MassSpecTableDataProcessorUtil.setRowValue(_settings.getColumnPosition( DMAnnotation.annotation_ratio.name()), 
								fs.getRatio(), alRow.getDataRow()); 
						getSimianTableDataObject().getTableData().add(alRow);
						if( ! fs.getSelected() ) {
							getSimianTableDataObject().setHiddenRow(_features.getScanId(), sRowId, feature.getId().toString());
							if( sFirstFeatureId == null ) {
								sFirstFeatureId = feature.getId();
							}
						} else {
							bAtLeastOne = true;
						}
					}

					if (peak.getIsSelectionsLocked()) {
						if (scan.getMsLevel() > 1)
							getSimianTableDataObject().addLockedPeak(iParentScanNumber, sRowId);
						else
							getSimianTableDataObject().addLockedPeak(iArchiveParentScan, sRowId);
					}

				} else {
					if( ! getMethod().getMsType().equals(Method.MS_TYPE_INFUSION) && 
							scan.getMsLevel() == 1 && precursorScans.size() == 1 && precursorScans.get(0) == null) 
						continue;
					GRITSListDataRow alRow = getNewRow();
					addPeaksData(iParentScanNumber, precursorScan, peak, _settings, alRow);
					addSubScanPeaksData(precursorScan, peak, _settings, alRow);
					fillFeatureData(null, _settings, alRow);
					fillAnnotationData(null, null, precursorScan, 0, _settings, alRow);
					// add comment column, get it from the Peak
					MassSpecTableDataProcessorUtil.setRowValue(_settings.getColumnPosition( TableDataProcessor.commentColHeader.getKeyValue()), 
							peak.getComment(), alRow.getDataRow());
					alUnAnnotatedRows.add(getSimianTableDataObject().getTableData().size());
					getSimianTableDataObject().getTableData().add(alRow);
				}
				if( ! bAtLeastOne ) {				
					getSimianTableDataObject().addInvisibleRow(iArchiveParentScan, sRowId);    
					getSimianTableDataObject().removeHiddenRow(iArchiveParentScan, sRowId, sFirstFeatureId);
				}
			}

		}
		return bDirty;
	}

	/**
	 * @param _scan - a org.grits.toolbox.ms.om.data.Scan object
	 * @param _settings - a TableViewerColumnSettings object
	 * @param alRow - a GRITSListDataRow object to be filled
	 * 
	 * Description: Populates a GRITSListDataRow with data from a Scan object at the column positions defined 
	 * in TableViewerColumnSettings object
	 */
	@Override
	protected void addScanData(Scan _scan, TableViewerColumnSettings _settings, GRITSListDataRow alRow) {
		super.addScanData(_scan, _settings, alRow);
		MSAnnotationTableDataProcessorUtil.fillMassSpecScanData(_scan, alRow.getDataRow(), _settings);
	}

	/**
	 * Description: Adds the Feature-related columns to the the TableViewerColumnSettings object
	 * @param _settings - a TableViewerColumnSettings object
	 */
	protected void addFeatureColumns(TableViewerColumnSettings _settings) {
		MSAnnotationTableDataProcessorUtil.fillMSAnnotationColumnSettingsFeature(_settings);
		MSAnnotationTableDataProcessorUtil.fillColumnSettingsCustomExtraData(_settings, 
				getGRITSdata().getDataHeader().getFeatureCustomExtraData());
	}

	/**
	 * Description: Adds the Annotation-related columns to the the TableViewerColumnSettings object
	 * @param _settings - a TableViewerColumnSettings object
	 */
	protected void addAnnotationColumns(TableViewerColumnSettings _settings) {
		MSAnnotationTableDataProcessorUtil.fillMSAnnotationColumnSettingsAnnotation(_settings);
		MSAnnotationTableDataProcessorUtil.fillColumnSettingsCustomExtraData(_settings, 
				getGRITSdata().getDataHeader().getAnnotationCustomExtraData());
		setLastVisibleCol(getLastVisibleCol() + 4);  // we want to include the # Candidate Annotations column, which was added first
	}

	/**
	 * Description: fills the GRITSListDataRow object with data from the Feature object
	 * 
	 * @param feature - A Feature object
	 * @param _settings - a TableViewerColumnSettings object
	 * @param alRow - a GRITSListDataRow object
	 */
	protected void fillFeatureData( Feature feature, TableViewerColumnSettings _settings, GRITSListDataRow alRow  )
	{
		MSAnnotationTableDataProcessorUtil.fillFeatureData(feature, alRow.getDataRow(), _settings); ; 
		MSAnnotationTableDataProcessorUtil.fillMSFeatureCustomExtraData(feature, alRow.getDataRow(), _settings, getGRITSdata().getDataHeader().getFeatureCustomExtraData());
	}

	/**
	 * Description: fills the GRITSListDataRow object with data from the Annotation object
	 * 
	 * @param a_annotation - An Annotation object
	 * @param feature - A Feature object, not used here but part of API just in case
	 * @param _settings - a TableViewerColumnSettings object
	 * @param alRow - a GRITSListDataRow object
	 */
	protected void fillAnnotationData( Annotation a_annotation, Feature feature, Scan a_scan, int _iNumCandidates, 
			TableViewerColumnSettings _settings, GRITSListDataRow alRow ) {
		MSAnnotationTableDataProcessorUtil.fillAnnotationData(a_annotation, _iNumCandidates, alRow.getDataRow(), _settings);	
		MSAnnotationTableDataProcessorUtil.fillMSAnnotationCustomExtraData(a_annotation, alRow.getDataRow(), _settings, getGRITSdata().getDataHeader().getAnnotationCustomExtraData());
	}

	/**
	 * Description: Reads the features for a scan from file and then updates based on what is in the TableDataObject. 
	 * If changed, then write to file.
	 * @param iParentScan - Integer value for a scan to be updated
	 * @return
	 */	
	protected boolean updateAnnotationDataSingleScan( Integer iParentScan ) {
		try {
			if( iParentScan == null || iParentScan.equals(-1) )
				return false;	
			ScanFeatures features = loadScanFeatureFromXML(iParentScan);
			if (features == null)
				return false;
			updateScanFeaturesWithFilter(features);

			boolean bIsDI = getMethod().getMsType().equals(Method.MS_TYPE_INFUSION) && getMassSpecEntityProperty().getMsLevel() - 1 == 1;
			boolean bIsDirty = updateAnnotationData(features, iParentScan, bIsDI);
			if( ! bIsDirty ) 
				return false;
			String sSourceFile = getScanArchiveFile();
			//		this.xmlWriter.writeSingleScanToZipFile(iParentScan, features, sSourceFile);
			writeFeaturesToFile(iParentScan, features, sSourceFile);
			return true;
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return false;
	}

	/**
	 * Description: Writes the ScanFeatures for the specified scan number to the specified source file
	 * 
	 * @param iParentScan - Integer value of a scan number
	 * @param features - a ScanFeatures object
	 * @param sSourceFile - String value of the archive file
	 * @return boolean - whether or not write was successful
	 */
	protected boolean writeFeaturesToFile( Integer iParentScan, ScanFeatures features, String sSourceFile ) {
		try {
			this.xmlWriter.writeSingleScanToZipFile(iParentScan, features, sSourceFile);
			return true;			
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * after loading the scan features from the xml file, update the object with currently applied filters, if any, 
	 * so that the currently applied filters can be stored in the xml file as well.
	 * 
	 * Needs to be overridden by the subclasses with the actual filter settings
	 * 
	 * @param features
	 */
	protected void updateScanFeaturesWithFilter(ScanFeatures features) {
		// no generic implementation
	}

	/**
	 * Iterates over the peaks in the ScanFeatures object to check if selections changed or if the external
	 * quant has changed.
	 * 
	 * @param _features - a ScanFeatures object
	 * @param iParentScan - Integer value of a scan number
	 * @param _bIsDI - whether or not the file is direct infusion
	 * @return boolean - whether or not data has changed and thus needs to be written
	 */
	protected boolean updateAnnotationData( ScanFeatures _features, Integer iParentScan, boolean _bIsDI ) {
		boolean bIsDirty = false;
		HashMap<String, List<Feature>> htPeakToFeatures = null;		
		HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> htParentScanToParentPeaksToSubScan = null;
		htPeakToFeatures = AnnotationRowExtraction.createRowIdToFeatureHash(_features);
		boolean bNeedsConvert = false;
		if( htPeakToFeatures.isEmpty() ) { // no features by row ids? Is this an old project? try the old way
			htPeakToFeatures = AnnotationRowExtraction.createPeakIdToFeatureHash(_features);
			bNeedsConvert = true;
		}
		if( htPeakToFeatures.isEmpty() ) {
			return false; // fail
		}
		htParentScanToParentPeaksToSubScan = AnnotationRowExtraction.createParentScanToParentPeaksToSubScanHash(getGRITSdata());
		if( _bIsDI ) {
			AnnotationRowExtraction.updateParentScanToParentPeaksToSubScanHashForDirectInfusion(htParentScanToParentPeaksToSubScan, getGRITSdata()) ;
		}	
		Annotation parentAnnot = null;
		if( getMSAnnotationEntityProperty().getAnnotationId() != null ) {
			parentAnnot = AnnotationRowExtraction.getAnnotation(getGRITSdata(), getMSAnnotationEntityProperty().getAnnotationId());
		}

		for (Peak peak : _features.getScanPeaks() ) {
			if( peak == null) {
				continue;
			}

			// is _features.getScanId() the same as scan num???
			Peak precursorPeak = getPrecursorPeak(peak.getMz(), _features.getScanId());
			ArrayList<Scan> precursorScans = AnnotationRowExtraction.getPrecursorScan(getGRITSdata(),
					_features.getScanId(), peak.getId(),
					htParentScanToParentPeaksToSubScan);

			if( bNeedsConvert ) {
				AnnotationRowExtraction.convertPeakIdsToRowIds( getGRITSdata(), _features, iParentScan, peak.getId(), precursorScans, htPeakToFeatures);
				getMySimianTableDataObject().setUsesComplexRowId(_features.getUsesComplexRowId());
			}

			for( Scan precursorScan : precursorScans ) {
				String sRowId = Feature.getRowId(peak.getId(), precursorScan != null ? precursorScan.getScanNo() : null, _features.getUsesComplexRowId());
				peak.setIsSelectionsLocked(getSimianTableDataObject().isLockedPeak(_features.getScanId(), sRowId));
				// add the comment to Peak here (this is for the unannotated ones)
				String newComment = getCommentFromTable(peak.getId());
				if (newComment != null && !newComment.equals(peak.getComment()))
					bIsDirty = true;
				peak.setComment(newComment);
				boolean bInvisible = getSimianTableDataObject().isInvisibleRow(_features.getScanId(), sRowId);
				logger.debug("Scan num: " + _features.getScanId() + " , peak id: " + peak.getId());
				boolean bManuallySelected = getSimianTableDataObject().isManuallyChangedPeak(_features.getScanId(), sRowId);
				if( htPeakToFeatures.containsKey(sRowId) ) {    			
					List<Feature> alFeatures = htPeakToFeatures.get(sRowId);
					for( Feature feature : alFeatures ) {    
						if( parentAnnot != null && (feature.getParentAnnotation() == null ||
								! feature.getParentAnnotation().getStringId().startsWith(parentAnnot.getStringId())) ) {
							continue;
						}
						FeatureSelection fs = Feature.getFeatureSelection(feature, sRowId);
						boolean bHidden = getSimianTableDataObject().isHiddenRow(_features.getScanId(), sRowId, feature.getId());
						if ( (bHidden || bInvisible) && fs.getSelected() ) {    		        
							fs.setSelected( false );
							bIsDirty = true;
						} else if ( ! bHidden && ! bInvisible && ! fs.getSelected() ){
							fs.setSelected( true );
							bIsDirty = true;
						}
						fs.setManuallySelected(bManuallySelected);
						// for the annotated ones, set the comment to FeatureSelection
						newComment = getCommentFromTable(feature, sRowId);
						Double newRatio = getRatioFromTable(feature, sRowId);
						if (newComment != null && !newComment.equals(fs.getComment()))
							bIsDirty = true;
						if (newRatio != null && !newRatio.equals(fs.getRatio()))
							bIsDirty = true;
						fs.setComment(newComment);
						fs.setRatio(newRatio);
						if( precursorPeak != null && peak.getId() == precursorPeak.getId() ) {
							peak.setIntegerProp(precursorPeak.getIntegerProp());
							peak.setDoubleProp(precursorPeak.getDoubleProp());
							bIsDirty = true;
						}
					}
				} 
			}
		}			
		return bIsDirty;
	}

	/**
	 * Extracts the edited comment value from the underlying table for the given peak
	 * @param peakId id of the peak
	 * @return comment
	 */
	private String getCommentFromTable(Integer peakId) {
		if (((MSAnnotationTableDataObject)getSimianTableDataObject()).getPeakIdCols() == null ||
				((MSAnnotationTableDataObject)getSimianTableDataObject()).getPeakIdCols().isEmpty())
			return null;
		Integer peakIdCol = ((MSAnnotationTableDataObject)getSimianTableDataObject()).getPeakIdCols().get(0);
		Integer commentCol = null;
		if (((MSAnnotationTableDataObject)getSimianTableDataObject()).getCommentCols() != null &&
				!((MSAnnotationTableDataObject)getSimianTableDataObject()).getCommentCols().isEmpty()) 
			commentCol = ((MSAnnotationTableDataObject)getSimianTableDataObject()).getCommentCols().get(0);

		// go through the underlying data to extract the comment for the given feature
		for (GRITSListDataRow row : getSimianTableDataObject().getTableData()) {
			Object peakIdVal= row.getDataRow().get(peakIdCol);
			if (peakIdVal != null && ((Integer)peakIdVal).equals(peakId)) {
				// match
				if (commentCol != null)
					return (String)row.getDataRow().get(commentCol);
			}
		}
		return null;
	}

	/**
	 * Extracts the comment edited from the underlying table object for a given Feature and the peak
	 * @param feature feature to search
	 * @param peakId peak id of the feature
	 * @return comment
	 */
	private String getCommentFromTable(Feature feature, String peakId) {
		if (((MSAnnotationTableDataObject)getSimianTableDataObject()).getFeatureIdCols() == null ||
				((MSAnnotationTableDataObject)getSimianTableDataObject()).getFeatureIdCols().isEmpty() ||
				((MSAnnotationTableDataObject)getSimianTableDataObject()).getPeakIdCols() == null ||
				((MSAnnotationTableDataObject)getSimianTableDataObject()).getPeakIdCols().isEmpty())
			return null;
		Integer featureIdCol = ((MSAnnotationTableDataObject)getSimianTableDataObject()).getFeatureIdCols().get(0);
		Integer peakIdCol = ((MSAnnotationTableDataObject)getSimianTableDataObject()).getPeakIdCols().get(0);
		Integer commentCol = null;
		if (((MSAnnotationTableDataObject)getSimianTableDataObject()).getCommentCols() != null &&
				!((MSAnnotationTableDataObject)getSimianTableDataObject()).getCommentCols().isEmpty()) 
			commentCol = ((MSAnnotationTableDataObject)getSimianTableDataObject()).getCommentCols().get(0);
		// go through the underlying data to extract the comment for the given feature
		for (GRITSListDataRow row : getSimianTableDataObject().getTableData()) {
			Object featureId = row.getDataRow().get(featureIdCol);
			Object peakId2 = row.getDataRow().get(peakIdCol);
			if (featureId != null && ((String)featureId).equals(feature.getId())) {
				if (peakId2 != null && ((Integer)peakId2).toString().equals(peakId)) {
					// match
					if (commentCol != null)
						return (String)row.getDataRow().get(commentCol);
				}
			}
		}
		return null;
	}

	/**
	 * Extracts the ratio edited from the underlying table object for a given Feature and the peak
	 * 
	 * @param feature feature to search
	 * @param peakId peak id of the feature
	 * @return ratio entered by the user
	 */
	private Double getRatioFromTable(Feature feature, String peakId) {
		if (((MSAnnotationTableDataObject)getSimianTableDataObject()).getFeatureIdCols() == null ||
				((MSAnnotationTableDataObject)getSimianTableDataObject()).getFeatureIdCols().isEmpty() ||
				((MSAnnotationTableDataObject)getSimianTableDataObject()).getPeakIdCols() == null ||
				((MSAnnotationTableDataObject)getSimianTableDataObject()).getPeakIdCols().isEmpty())
			return null;
		Integer featureIdCol = ((MSAnnotationTableDataObject)getSimianTableDataObject()).getFeatureIdCols().get(0);
		Integer peakIdCol = ((MSAnnotationTableDataObject)getSimianTableDataObject()).getPeakIdCols().get(0);
		Integer ratioCol = null;
		if (((MSAnnotationTableDataObject)getSimianTableDataObject()).getRatioCols() != null &&
				!((MSAnnotationTableDataObject)getSimianTableDataObject()).getRatioCols().isEmpty()) 
			ratioCol = ((MSAnnotationTableDataObject)getSimianTableDataObject()).getRatioCols().get(0);
		// go through the underlying data to extract the ratio for the given feature
		for (GRITSListDataRow row : getSimianTableDataObject().getTableData()) {
			Object featureId = row.getDataRow().get(featureIdCol);
			Object peakId2 = row.getDataRow().get(peakIdCol);
			if (featureId != null && ((String)featureId).equals(feature.getId())) {
				if (peakId2 != null && ((Integer)peakId2).toString().equals(peakId)) {
					// match
					if (ratioCol != null) {
						try {
							Object val = row.getDataRow().get(ratioCol);
							if (val != null) {
								return Double.valueOf(val.toString());
							} 
						} catch (NumberFormatException e) {
							return null; 
						}
					}
				}
			}
		}
		return null;

	}

	/**
	 * @return Annotation - the current Annotation object
	 */
	public Annotation getCurAnnotation() {
		return curAnnotation;
	}

	/**
	 * @param curAnnotation - an Annotation object
	 */
	public void setCurAnnotation(Annotation curAnnotation) {
		this.curAnnotation = curAnnotation;
	}

	/**
	 * @return Feature - the current Feature object
	 */
	public Feature getCurFeature() {
		return curFeature;
	}

	/**
	 * @param curFeature - a Feature object
	 */
	public void setCurFeature(Feature curFeature) {
		this.curFeature = curFeature;
	}

	/**
	 * @return ScanFeatures - the current ScanFeatures object
	 */
	public ScanFeatures getCurScanFeature() {
		return curScanFeatures;
	}

	/**
	 * @param scanFeature - a ScanFeatures object
	 */
	public void setCurScanFeature(ScanFeatures scanFeature) {
		this.curScanFeatures = scanFeature;
	}
}