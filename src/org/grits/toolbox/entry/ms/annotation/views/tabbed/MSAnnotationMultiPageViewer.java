package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.DataModelHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.editor.EntryEditorPart;
import org.grits.toolbox.core.editor.IEntryEditorPart;
import org.grits.toolbox.core.preference.share.IGritsPreferenceStore;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.datamodel.ms.annotation.preference.MSAnnotationViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.datamodel.ms.preference.MassSpecViewerPreference;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.preference.TableViewerPreference;
import org.grits.toolbox.entry.ms.annotation.command.ViewRowChooserInTabCommandExecutor;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.property.MassSpecEntityProperty;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.ExternalQuantAlias;
import org.grits.toolbox.entry.ms.property.datamodel.ExternalQuantFileToAlias;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.grits.toolbox.entry.ms.property.datamodel.MassSpecMetaData;
import org.grits.toolbox.entry.ms.property.datamodel.MassSpecUISettings;
import org.grits.toolbox.entry.ms.tablehelpers.MassSpecTable;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecPeaksView;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecScansView;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecSpectraView;
import org.grits.toolbox.ms.file.FileCategory;
import org.grits.toolbox.ms.file.MSFileInfo;
import org.grits.toolbox.ms.om.data.AnnotationFilter;
import org.grits.toolbox.widgets.processDialog.GRITSProgressDialog;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;
import org.grits.toolbox.widgets.tools.GRITSWorker;

/**
 * A tabbed-editor for displaying information for MS Annotation Data.<br>
 * This editor extends MassSpecMultiPageViewer.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * @see MassSpecMultiPageViewer
 * @see MSAnnotationPropertyView
 * @see MSAnnotationDetails
 *
 */
public class MSAnnotationMultiPageViewer extends MassSpecMultiPageViewer {
	private static final Logger logger = Logger.getLogger(MSAnnotationMultiPageViewer.class);

	public static final String ROW_NUM_CONTEXT = "Row Number Context";

	protected MSAnnotationPropertyView msAnnotPropertyView = null;
	protected MSAnnotationFilterView msAnnotFilterView = null;
	protected int msAnnotPropertyViewTabIndex = -1;
	protected int msAnnotFilterViewTabIndex = -1;
	protected int msAnnotOtherViewTabIndex = -1;
	protected int msAnnotQuantViewTabIndex = -1;
	protected MSAnnotationDetails annotDetails = null;
	protected int annotDetailsTabIndex = -1;
	protected int numDetailSteps = -1;
	protected MSAnnotationOtherSettingsView msAnnotOtherView;
	protected MSAnnotationQuantificationView msAnnotQuantView;

	

	public static String VIEW_ID = "plugin.ms.annotation.views.MSAnnotationMultiPageViewer";

	public MSAnnotationMultiPageViewer() {
		super();
	}

	@Inject
	public MSAnnotationMultiPageViewer( Entry entry ) {
		super(entry);
	}

	@Inject
	public MSAnnotationMultiPageViewer (MPart part) {
		super(part);
	}

	public int getNumDetailSteps() {
		return numDetailSteps;
	}

	public int getMsAnnotPropertyViewTabIndex() {
		return msAnnotPropertyViewTabIndex;
	}

	public void setMsAnnotPropertyViewTabIndex(int msAnnotPropertyViewTabIndex) {
		this.msAnnotPropertyViewTabIndex = msAnnotPropertyViewTabIndex;
	}

	public int getAnnotDetailsTabIndex() {
		return annotDetailsTabIndex;
	}

	public void setAnnotDetailsTabIndex(int annotDetailsTabIndex) {
		this.annotDetailsTabIndex = annotDetailsTabIndex;
	}

	public void setMsAnnotFilterViewTabIndex(int msAnnotFilterViewTabIndex) {
		this.msAnnotFilterViewTabIndex = msAnnotFilterViewTabIndex;
	}
	
	public int getMsAnnotFilterViewTabIndex() {
		return msAnnotFilterViewTabIndex;
	}
	
	public int getMsAnnotOtherViewTabIndex() {
		return msAnnotOtherViewTabIndex;
	}
	
	public int getMsAnnotQuantViewTabIndex() {
		return msAnnotQuantViewTabIndex;
	}
	
	public void setMsAnnotOtherViewTabIndex(int msAnnotOtherViewTabIndex) {
		this.msAnnotOtherViewTabIndex = msAnnotOtherViewTabIndex;
	}
	
	public void setMsAnnotQuantViewTabIndex(int msAnnotQuantViewTabIndex) {
		this.msAnnotQuantViewTabIndex = msAnnotQuantViewTabIndex;
	}
	
	@Override
	public String toString() {
		return "MSAnnotationMultiPageViewer (" + entry + ")";
	}

	public MSAnnotationDetails getAnnotationDetails() {
		return annotDetails;
	}		

	protected Entry getNewTableCompatibleEntry(Entry parentEntry) {
		Entry newEntry = MSAnnotationEntityProperty.getTableCompatibleEntry(parentEntry);	
		return newEntry;
	}

	@Override
	protected Entry getFirstPageEntry() {
		Entry parentEntry = null;
		if (gritsModelService.getLastSelection() != null
				&& gritsModelService.getLastSelection().getFirstElement() instanceof Entry)
			parentEntry = (Entry) gritsModelService.getLastSelection().getFirstElement();
		Entry newEntry = getNewTableCompatibleEntry(parentEntry);
		MassSpecEntityProperty msEntityProp = (MassSpecEntityProperty) newEntry.getProperty();
		msEntityProp.setScanNum(-1);
		msEntityProp.setMz(0.0);
		msEntityProp.setParentMz(0.0);
		setMinMSLevel(1);
		msEntityProp.setMsLevel(getMinMSLevel());
		newEntry.setDisplayName(newEntry.getDisplayName());
		if( newEntry.getProperty() instanceof MSAnnotationEntityProperty ) {
			MSAnnotationEntityProperty msAnnotEntityProp = (MSAnnotationEntityProperty) newEntry.getProperty();
			msAnnotEntityProp.setAnnotationId(-1);
		}
		return newEntry;
	}

	@Override
	protected Object getDesiredActivePage() {
		return this.annotDetails != null ? this.annotDetails : 
			getPeaksView().isEmpty() ? this.scansView : this.alPeaksViews.get(0);
	}

	@Override
	protected int addPages( final int _iMajorCount, final MassSpecEntityProperty prop ) {
		this.dtpdThreadedDialog = new GRITSProgressDialog(new Shell(), 1, false);
		this.dtpdThreadedDialog.open();
		this.dtpdThreadedDialog.getMajorProgressBarListener().setMaxValue(_iMajorCount);
		MSAnnotationMultiPageViewerWorker msmpvw = new MSAnnotationMultiPageViewerWorker(this, prop);
		this.dtpdThreadedDialog.setGritsWorker(msmpvw);
		int iSuccess = this.dtpdThreadedDialog.startWorker();
		return iSuccess;
	}


	@Override
	public int addPeakListPage_Step2() {
		int iStatus = super.addPeakListPage_Step2();
		if( iStatus != GRITSProcessStatus.OK ) {
			return iStatus;
		}
		int inx = getPeaksViewsFirstTabIndex();
		String sTitle = "Structure Annotation";
		if( entry.getProperty() instanceof MSAnnotationEntityProperty ) {
			MSAnnotationEntityProperty ep = (MSAnnotationEntityProperty) entry.getProperty();
			if( ep.getMsLevel() > 3 && ep.getAnnotationId() == -1 ) {
				sTitle = "Peak List";
			}
		}
		setPageText(inx, sTitle);
		return getStatus();	
	}

	public int addDetailsTab_Step1( MassSpecEntityProperty prop) {
		try {
			int iSuccess = initDetailsView(prop);		
			dtpdThreadedDialog.setMinorStatus(iSuccess);
			if( iSuccess == GRITSProcessStatus.CANCEL ) {
				setStatus(GRITSProcessStatus.CANCEL);
				return GRITSProcessStatus.CANCEL;
			}
		} catch( Exception ex ) {
			logger.error("Unable to open Details view", ex);
			setStatus(GRITSProcessStatus.ERROR);
		}
		if( getStatus() == GRITSProcessStatus.ERROR ) {
			String sMessage = "An error occurred creating the Details tab.";
			this.dtpdThreadedDialog.getMajorProgressBarListener().setError(sMessage);
		}
		return getStatus();	
	}

	public int addDetailsTab_Step2() {
		try {
			boolean success = true;
			int iPageCount = getPageCount();
			try {
				int inx = getAnnotDetailsTabIndex();
				if( inx >= getPageCount() ) {
					inx = addPage( annotDetails, entry);	
					setAnnotDetailsTabIndex(inx);
				} else {
					addPage( inx, annotDetails, entry);
				}
				setPageText(inx, "Details View");
				setActivePage(inx);
				int iSuccess = annotDetails.getStatus();
				setStatus(iSuccess);
				dtpdThreadedDialog.setMinorStatus(iSuccess);
			} catch( Exception ex ) {
				logger.error("Error adding Details tab.", ex);
				setStatus(GRITSProcessStatus.ERROR);
			}			
			success = (getStatus() != GRITSProcessStatus.ERROR);

			if( ! success ) {
				if( getPageCount() != iPageCount ) {
					removePage(getPageCount());
				}
				annotDetails = null;
			} 
		} catch( Exception ex ) {
			logger.error("Unable to open Details view", ex);
			setStatus(GRITSProcessStatus.ERROR);
		}
		if( getStatus() == GRITSProcessStatus.ERROR ) {
			String sMessage = "An error occurred creating the Details tab.";
			this.dtpdThreadedDialog.getMajorProgressBarListener().setError(sMessage);
		}
		return getStatus();	
	}

	@Override
	protected int getNumMajorSteps(MassSpecEntityProperty prop) {
		int iCount = 1; // property view
		int iTabCnt = 0;
		setPropertyViewTabIndex(iTabCnt++);
		setMsAnnotPropertyViewTabIndex(iTabCnt++);
		setMsAnnotFilterViewTabIndex(iTabCnt++);
		setMsAnnotOtherViewTabIndex(iTabCnt++);
		setMsAnnotQuantViewTabIndex(iTabCnt++);
		if ( getMinMSLevel() < 0 ) {
			iCount++; // we will  have to determine the min MS level
		}
		iCount += 2; // we know we have to open the data file and create at least one table
		setScansViewTabIndex(iTabCnt++);
		iCount ++; // MS Annotation property
		if( needsPeaksView( prop) ) {
			if( needsDetailsView((MSAnnotationEntityProperty) prop)) {
				iCount++; // for the first call to init Peak View
				iCount++; // for the first call init details
				setAnnotDetailsTabIndex(iTabCnt++);
				MSAnnotationMultiPageViewer viewer = MSAnnotationMultiPageViewer.getActiveViewer(getContext());
				if( viewer != null ) {
					if( viewer.getDetailsView() != null && ! viewer.getDetailsView().getPeaksViews().isEmpty() ) { // previous page must have been the MSn
						MSAnnotationPeaksView peakView = null;
						for( int i = 0; i < viewer.getDetailsView().getPeaksViews().size(); i++ ) {
							peakView = (MSAnnotationPeaksView) viewer.getDetailsView().getPeaksViews().get(i);
							if( peakView.getSelectionView() == null ) {
								continue;
							}
							MSAnnotationTable subTable = peakView.getSelectionView().getSubTable();
							if( subTable == null ) {
								continue;
							}
							MSAnnotationTableDataObject stdo = (MSAnnotationTableDataObject) subTable.getGRITSTableDataObject();
							if( stdo.getTableData().isEmpty() ) {
								continue;
							}
							Integer scan = (Integer) stdo.getTableData().get(0).getDataRow().get( stdo.getScanNoCols().get(0) );
							if( scan != null && scan.intValue() == prop.getScanNum() ) {
								numDetailSteps = stdo.getTableData().size();
								iCount += numDetailSteps;	
								break;
							}
						}
						//						MSAnnotationPeaksView peakView =  (MSAnnotationPeaksView) viewer.getDetailsView().getPeaksViews().get(0);
						//						numDetailSteps = peakView.getSelectionView().getSubTable().getSimDataObject().getTableData().size() - 2; // take off the 2 empties!
						//						iCount += numDetailSteps;												
					} else if ( ! viewer.getPeaksView().isEmpty() && viewer.getPeaksView().get(0) instanceof MSAnnotationPeaksView ) {
						MSAnnotationPeaksView peakView =  (MSAnnotationPeaksView) viewer.getPeaksView().get(0);
						numDetailSteps = peakView.getSelectionView().getSubTable().getGRITSTableDataObject().getTableData().size() - 2; // take off the 2 empties!
						iCount += numDetailSteps;						
					}
				}
			} else {
				iCount+=2;
				setPeaksViewsFirstTabIndex(iTabCnt++);
			}
		}
		if( needsSpectraView((MassSpecEntityProperty) prop) ) {
			iCount++;
			setSpectraViewTabIndex(iTabCnt++);
		}		
		return iCount;
	}

	@Override
	public void createPages() {
		setMinMSLevel(-1);
		if ( entry == null || ! (entry.getProperty() instanceof MassSpecEntityProperty) ) {
			entry = getFirstPageEntry();
		} 

		Property prop = entry.getProperty();	
		if (((MassSpecEntityProperty) prop).getDataFile() == null) {
			// check to see if the annotation file is set, if not find it from the parent
			this.msFile = getAnnotationFile (entry);
			((MassSpecEntityProperty) prop).setDataFile(this.msFile);
		} else {
			this.msFile = ((MassSpecEntityProperty) prop).getDataFile();
		}
		int iNumSteps = getNumMajorSteps( (MassSpecEntityProperty) prop);

		// need to add this multi-page editor into the part's context before adding pages
		// pages may need to access their parent editor
		getPart().getContext().set(MSAnnotationMultiPageViewer.class, this);

		iStatus = addPages(iNumSteps, (MassSpecEntityProperty) prop);
		if (iStatus == GRITSProcessStatus.ERROR) {
			// need to close the editor, it failed to open
			throw new RuntimeException("Failed to open the entry");
		}
		setPartName(entry.getDisplayName());
		setActivePage();		
		//	setTitleImage();
	}
	
	@Override
	protected String getScanHierarchyPartName() {
		return "ScanHierarchy-Annotation";
	}
	
	/**
	 * 
	 * uniqueness is determined by having the same parent property
	 */
	@Override
	protected boolean needsScanHierarchyReload(MassSpecEntityProperty oldProp, MassSpecEntityProperty newProp) {
		if (oldProp != null && oldProp.getParentProperty().equals(newProp.getParentProperty()))
			return false;
		return true;
	}
	
	/**
	 * finds and activates the correct scan hierarchy view part
	 * if the "MassSpec" scan hierarchy is open, it closes that one before opening the annotation one.
	 */
	@Override
	protected MPart getScanHiearchyViewPart () {
		String label = "Scan Hierarchy View";
		//check if the part is already open
		MPart msPart = partService.findPart ("ScanHierarchy");
		if (msPart != null) {
			partService.hidePart(msPart, true);
		}
		MPart part = partService.findPart("ScanHierarchy-Annotation");
		if (part != null) {
			partService.activate(part);
		} else {
			part = partService.createPart("ScanHierarchy-Annotation");
			part.setLabel(label);
			List<MPartStack> stacks = modelService.findElements(application, "org.grits.toolbox.core.partstack.ms",
					MPartStack.class, null);
			if (stacks.size() < 1) {
				setStatus(GRITSProcessStatus.ERROR);
				this.dtpdThreadedDialog.getMajorProgressBarListener().setError("Part stack not found. Is the following ID correct?" + "org.grits.toolbox.core.partstack.ms");
				return null;
			} 
			stacks.get(0).getChildren().add(part);
			stacks.get(0).setVisible(true);
			PropertyHandler.changePerspective("org.grits.toolbox.core.perspective.msperspective");
			// activates the part
			partService.showPart(part, PartState.ACTIVATE);
		}
		return part;
	}

	/**
	 * Updates the meta data  by cloning the MSPropertyDataFile object and putting it in the right list.
	 * 
	 * @param metaData
	 * @param pdf
	 */
	private static void updateLegacyProjectMetaData(MSAnnotationMetaData metaData, MSPropertyDataFile pdf) {
		if( pdf.getMSFileType().equals(MSFileInfo.MS_FILE_TYPE_INSTRUMENT) ) {
			return;
		}
		if( pdf.getCategory().equals(FileCategory.ANNOTATION_CATEGORY )) {
			MSPropertyDataFile file = new MSPropertyDataFile(pdf.getName(), pdf.getVersion(), 
					pdf.getType(), pdf.getCategory(), pdf.getMSFileType(), 
					pdf.getOriginalFileName(), pdf.getPurpose());
			metaData.addAnnotationFile(file);
		} else if ( pdf.getCategory().equals(FileCategory.EXTERNAL_QUANTIFICATION_CATEGORY )) {
			MSPropertyDataFile file = new MSPropertyDataFile(pdf.getName(), pdf.getVersion(), 
					pdf.getType(), pdf.getCategory(), pdf.getMSFileType(), 
					pdf.getOriginalFileName(), pdf.getPurpose());
			metaData.addSourceFile(file);
			ExternalQuantAlias eqa = new ExternalQuantAlias();
			eqa.setAlias(MSPropertyDataFile.getLegacyExternalQuantAlias(file));
			//			eqa.setKeyId(MassSpecUISettings.getRandomId());
			metaData.addExternalQuantFile(file, eqa);
			String sExtQuantType = MassSpecUISettings.getExternalQuantType(file);
			ExternalQuantFileToAlias mAliases = metaData.getExternalQuantToAliasByQuantType(sExtQuantType);
			metaData.updateQuantAliasKeyInfo(file, mAliases, null);
		}			
	}

	/**
	 * Updates the meta data with the MSPropertyDataFile information from the parent MassSpecProperty
	 * @param entry
	 */
	private static void updateLegacyProjectMetaData(Entry entry) {
		Property parentProperty = entry.getParent().getProperty();
		if (parentProperty instanceof MSAnnotationProperty) {
			MSAnnotationMetaData metaData = ((MSAnnotationProperty) parentProperty).getMSAnnotationMetaData();
			Entry msParent = entry.getParent().getParent();
			if (msParent != null) {
				if (msParent.getProperty() instanceof MassSpecProperty) {
					MassSpecProperty msProperty = (MassSpecProperty) msParent.getProperty();
					MassSpecMetaData msMetaData = msProperty.getMassSpecMetaData();
					for( MSPropertyDataFile pdf : msMetaData.getFileList() ) {
						MSAnnotationMultiPageViewer.updateLegacyProjectMetaData(metaData, pdf);
						if( pdf.getChildren() != null && ! pdf.getChildren().isEmpty() ) {
							for( MSPropertyDataFile pdfChild : pdf.getChildren() ) {
								MSAnnotationMultiPageViewer.updateLegacyProjectMetaData(metaData, pdfChild);								
							}
						}
					}
					String settingsFile = ((MSAnnotationProperty)parentProperty).getFullyQualifiedMetaDataFileName(entry.getParent());
					MSAnnotationProperty.marshallSettingsFile(settingsFile, metaData);
				}
			}
		}
	}


	/**
	 * Returns the MSPropertyDataFile object corresponding to the annotation file from the Mass Spec parent
	 * 
	 * @param entry
	 * @return
	 */
	private static MSPropertyDataFile getAnnotationFile(Entry entry) {
		if (entry.getParent() != null) {
			Property parentProperty = entry.getParent().getProperty();
			if (parentProperty instanceof MSAnnotationProperty) {
				MSAnnotationMetaData metaData = ((MSAnnotationProperty) parentProperty).getMSAnnotationMetaData();
				if( metaData.getAnnotationFile() == null ) { // legacy, set things up first
					MSAnnotationMultiPageViewer.updateLegacyProjectMetaData(entry);					
				}
				return metaData.getAnnotationFile();
			}
		}
		return null;
	}

	protected boolean initMSAnnotationPropertyView() {
		try {
			getPart().getContext().set(Entry.class, entry);
			this.msAnnotPropertyView = ContextInjectionFactory.make(MSAnnotationPropertyView.class, getPart().getContext());
			return true;
		} catch( Exception ex ) {
			logger.error("Unable to open property view", ex);
		}		
		return false;
	}
	
	protected boolean initMsAnnotationFilterView() {
		try {
			getPart().getContext().set(Entry.class, entry);
			this.msAnnotFilterView = ContextInjectionFactory.make(MSAnnotationFilterView.class, getPart().getContext());
			return true;
		} catch( Exception ex ) {
			logger.error("Unable to open property view", ex);
		}		
		return false;
	}
	
	protected boolean initOtherSettingsView() {
		try {
			getPart().getContext().set(Entry.class, entry);
			this.msAnnotOtherView = ContextInjectionFactory.make(MSAnnotationOtherSettingsView.class, getPart().getContext());
			return true;
		} catch( Exception ex ) {
			logger.error("Unable to open property view", ex);
		}		
		return false;
	}
	
	protected boolean initQuantificationView() {
		try {
			getPart().getContext().set(Entry.class, entry);
			this.msAnnotQuantView = ContextInjectionFactory.make(MSAnnotationQuantificationView.class, getPart().getContext());
			return true;
		} catch( Exception ex ) {
			logger.error("Unable to open property view", ex);
		}		
		return false;
	}

	public int addMSAnnotationPropertyView( ) {
		try {
			boolean success = initMSAnnotationPropertyView();	
			int iPageCount = getPageCount();
			if( success ) {
				try {
					if( scansViewTabIndex >= 0 ) { // did we create the scans table?
						MSAnnotationTableDataProcessor proc = (MSAnnotationTableDataProcessor) this.scansView.getTableDataProcessor();
						this.msAnnotPropertyView.setMsAnnotationMethod(proc.getMethod());						
					}
					int inx = getMsAnnotPropertyViewTabIndex();
					if( inx >= getPageCount() ) {
						inx = addPage(msAnnotPropertyView, entry);
						setMsAnnotPropertyViewTabIndex(inx);
					} else {
						addPage( inx, msAnnotPropertyView, entry);
					}
					setPageText(inx, "MS Annotation Properties");	
					setActivePage(inx);
					setStatus(GRITSProcessStatus.OK);
				} catch( Exception ex ) {
					logger.error("Unable to open MS property view", ex);
					setStatus(GRITSProcessStatus.ERROR);
				}
			}
			if( isCanceled() ) {
				setStatus(GRITSProcessStatus.CANCEL);
			}
			success = (getStatus() == GRITSProcessStatus.OK );
			if( ! success ) {
				if( getPageCount() != iPageCount ) {
					removePage(getPageCount());
				}
				msAnnotPropertyView = null;
			} 
		} catch( Exception ex ) {
			logger.error("Unable to open MS property view", ex);
			setStatus(GRITSProcessStatus.ERROR);
		}
		if( getStatus() == GRITSProcessStatus.ERROR ) {
			String sMessage = "An error occurred creating the MS Annotation Properties tab.";
			this.dtpdThreadedDialog.getMajorProgressBarListener().setError(sMessage);
		}
		return getStatus();
	}
	
	public int addMsFilterPage() {
		try {
			boolean success = initMsAnnotationFilterView();	
			int iPageCount = getPageCount();
			if( success ) {
				try {
					if( scansViewTabIndex >= 0 ) { // did we create the scans table?
						MSAnnotationTableDataProcessor proc = (MSAnnotationTableDataProcessor) this.scansView.getTableDataProcessor();
						this.msAnnotFilterView.setMsAnnotationMethod(proc.getMethod());						
					}
					int inx = getMsAnnotFilterViewTabIndex();
					if( inx >= getPageCount() ) {
						inx = addPage(msAnnotFilterView, entry);
						setMsAnnotFilterViewTabIndex(inx);
					} else {
						addPage( inx, msAnnotFilterView, entry);
					}
					setPageText(inx, "Filter Settings");	
					setActivePage(inx);
					setStatus(GRITSProcessStatus.OK);
				} catch( Exception ex ) {
					logger.error("Unable to open MS Filter Settings view", ex);
					setStatus(GRITSProcessStatus.ERROR);
				}
			}
			if( isCanceled() ) {
				setStatus(GRITSProcessStatus.CANCEL);
			}
			success = (getStatus() == GRITSProcessStatus.OK );
			if( ! success ) {
				if( getPageCount() != iPageCount ) {
					removePage(getPageCount());
				}
				msAnnotFilterView = null;
			} 
		} catch( Exception ex ) {
			logger.error("Unable to open MS settings view", ex);
			setStatus(GRITSProcessStatus.ERROR);
		}
		if( getStatus() == GRITSProcessStatus.ERROR ) {
			String sMessage = "An error occurred creating the MS Annotation Filter Settings tab.";
			this.dtpdThreadedDialog.getMajorProgressBarListener().setError(sMessage);
		}
		return getStatus();
	}
	
	public int addOtherSettingsPage() {
		try {
			boolean success = initOtherSettingsView();	
			int iPageCount = getPageCount();
			if( success ) {
				try {
					int inx = getMsAnnotOtherViewTabIndex();
					if( inx >= getPageCount() ) {
						inx = addPage(msAnnotOtherView, entry);
						setMsAnnotOtherViewTabIndex(inx);
					} else {
						addPage( inx, msAnnotOtherView, entry);
					}
					setPageText(inx, "Custom Annotations");	
					setActivePage(inx);
					setStatus(GRITSProcessStatus.OK);
				} catch( Exception ex ) {
					logger.error("Unable to open MS Custom Annotations view", ex);
					setStatus(GRITSProcessStatus.ERROR);
				}
			}
			if( isCanceled() ) {
				setStatus(GRITSProcessStatus.CANCEL);
			}
			success = (getStatus() == GRITSProcessStatus.OK );
			if( ! success ) {
				if( getPageCount() != iPageCount ) {
					removePage(getPageCount());
				}
				msAnnotOtherView = null;
			} 
		} catch( Exception ex ) {
			logger.error("Unable to open MS custom annotations view", ex);
			setStatus(GRITSProcessStatus.ERROR);
		}
		if( getStatus() == GRITSProcessStatus.ERROR ) {
			String sMessage = "An error occurred creating the MS Annotation Custom Annotations tab.";
			this.dtpdThreadedDialog.getMajorProgressBarListener().setError(sMessage);
		}
		return getStatus();
	}
	
	public int addQuantificationPage() {
		try {
			boolean success = initQuantificationView();	
			int iPageCount = getPageCount();
			if( success ) {
				try {
					int inx = getMsAnnotQuantViewTabIndex();
					if( inx >= getPageCount() ) {
						inx = addPage(msAnnotQuantView, entry);
						setMsAnnotQuantViewTabIndex(inx);
					} else {
						addPage( inx, msAnnotQuantView, entry);
					}
					setPageText(inx, "Quantification Settings");	
					setActivePage(inx);
					setStatus(GRITSProcessStatus.OK);
				} catch( Exception ex ) {
					logger.error("Unable to open MS Quantification Settings view", ex);
					setStatus(GRITSProcessStatus.ERROR);
				}
			}
			if( isCanceled() ) {
				setStatus(GRITSProcessStatus.CANCEL);
			}
			success = (getStatus() == GRITSProcessStatus.OK );
			if( ! success ) {
				if( getPageCount() != iPageCount ) {
					removePage(getPageCount());
				}
				msAnnotQuantView = null;
			} 
		} catch( Exception ex ) {
			logger.error("Unable to open MS quantification settings view", ex);
			setStatus(GRITSProcessStatus.ERROR);
		}
		if( getStatus() == GRITSProcessStatus.ERROR ) {
			String sMessage = "An error occurred creating the MS Quantification Settings tab.";
			this.dtpdThreadedDialog.getMajorProgressBarListener().setError(sMessage);
		}
		return getStatus();
	}


	@Override
	protected MassSpecSpectraView getNewSpectraView() {
		getPart().getContext().set(Entry.class, this.entry);
		return ContextInjectionFactory.make(MSAnnotationSpectraView.class, getPart().getContext());
		//new MSAnnotationSpectraView(this.entry);
	}

	@Override
	protected boolean initSpectraView( MassSpecEntityProperty prop ) {
		try {
			int iMSLevelForSpectrum = 1;
			if ( prop.getMsLevel() != null ) {
				if ( entry.getParent().getProperty() instanceof MSAnnotationEntityProperty ) {
					MSAnnotationEntityProperty parentProp = (MSAnnotationEntityProperty) entry.getParent().getProperty();
					iMSLevelForSpectrum = parentProp.getMsLevel();
				}
			}
			spectraView = getNewSpectraView();
			if( this.getScansView() != null ) {
				MSAnnotationTableDataProcessor scansTableProcessor = (MSAnnotationTableDataProcessor) this.getScansView().getTableDataProcessor();
				( (MSAnnotationSpectraView) spectraView).setScanListTableProcessor(scansTableProcessor);
			}
			if( ! this.getPeaksView().isEmpty() && this.getPeaksView().get(0) != null ) {
				MSAnnotationTableDataProcessor peaksTableProcessor = (MSAnnotationTableDataProcessor) this.getPeaksView().get(0).getTableDataProcessor();
				( (MSAnnotationSpectraView) spectraView).setPeakListTableProcessor(peaksTableProcessor);
			}
			( (MSAnnotationSpectraView) spectraView).setMSLevel(iMSLevelForSpectrum);
//			( (MSAnnotationSpectraView) spectraView).setScanNum(prop.getScanNum());
			//			MassSpecProperty msProperty = getMSProperty(entry);
			updateMSView(prop, spectraView);
			return true;
		} catch( Exception ex ) {
			logger.error("Unable to open spectra view", ex);
		}		
		return false;	
	}

	// peaks view for annotation is like scans view except with annotations
	@Override
	protected int initPeaksView( MassSpecEntityProperty entityProperty ) {
		try {
			MassSpecEntityProperty msProp = (MassSpecEntityProperty) entityProperty.clone();
			msProp.setParentScanNum( entityProperty.getScanNum() );
			msProp.setScanNum(null);
			MassSpecPeaksView peaksView = getNewPeaksView( this.entry, msProp);
			int iSuccess = peaksView.setTableDataProcessor(this.dtpdThreadedDialog);
			if( iSuccess == GRITSProcessStatus.OK ) {
				alPeaksViews.add(peaksView);
			}
			return iSuccess;
		} catch( Exception ex ) {
			logger.error("Unable to open peaks view", ex);
			return GRITSProcessStatus.ERROR;
		}		
	}

	@Override
	protected MSAnnotationScansView getNewScansView( Entry entry, MassSpecEntityProperty entityProperty ) {
		MSAnnotationEntityProperty msProp = (MSAnnotationEntityProperty) entityProperty.clone();
		msProp.setParentScanNum( entityProperty.getScanNum() );
		msProp.setScanNum(null);
		getPart().getContext().set(MIN_MS_LEVEL_CONTEXT, getMinMSLevel());
		getPart().getContext().set(Property.class, msProp);
		getPart().getContext().set(Entry.class, entry);
		MSAnnotationScansView view = ContextInjectionFactory.make(MSAnnotationScansView.class, getPart().getContext());
		return view;
	}

	@Override
	protected MSAnnotationPeaksView getNewPeaksView(Entry entry, MassSpecEntityProperty entityProperty) {
		getPart().getContext().set(MIN_MS_LEVEL_CONTEXT, getMinMSLevel());
		getPart().getContext().set(Property.class, entityProperty);
		getPart().getContext().set(Entry.class, entry);
		MSAnnotationPeaksView view = ContextInjectionFactory.make(MSAnnotationPeaksView.class, getPart().getContext());
		return view;
	}

	public boolean needsDetailsView( MassSpecEntityProperty entityProperty ) {
		if( entityProperty.getParentScanNum() == null || entityProperty.getParentScanNum() < 0 ) 
			return false;
		if( entityProperty.getMsLevel() < 3 ) 
			return false;
		if( ((MSAnnotationEntityProperty) entityProperty).getAnnotationId() == null || 
				((MSAnnotationEntityProperty) entityProperty).getAnnotationId() < 0 ) 
			return false;

		return true;
	}

	protected int initDetailsView( MassSpecEntityProperty entityProperty ) {
		try {
			annotDetails = getNewDetailsView( this.entry, entityProperty);
			return GRITSProcessStatus.OK;
		} catch( Exception ex ) {
			logger.error("Unable to open peaks view", ex);
		}		
		return GRITSProcessStatus.ERROR;
	}

	protected MSAnnotationDetails getNewDetailsView( Entry entry, MassSpecEntityProperty entityProperty) {
		MSAnnotationMultiPageViewer parent = MSAnnotationMultiPageViewer.getActiveViewerForEntry(getContext(), entry);
		if ( parent != null ) {		
			getPart().getContext().set(MSAnnotationMultiPageViewer.class, this);
			getPart().getContext().set(MIN_MS_LEVEL_CONTEXT, getMinMSLevel());
			getPart().getContext().set(Property.class, entityProperty);
			getPart().getContext().set(Entry.class, entry);
			MSAnnotationDetails view = ContextInjectionFactory.make(MSAnnotationDetails.class, getPart().getContext());
			//new MSAnnotationDetails(parent, entry, (MSAnnotationEntityProperty) entityProperty, getMinMSLevel());
			return view;
		}
		return null;
	}


	public static void showRowSelection(IEclipseContext context, Entry entry, MSAnnotationTable parentTable, int iRowIndex, int iScanNum, String sRowId ) {
		try {
			MSAnnotationMultiPageViewer parent = MSAnnotationMultiPageViewer.getActiveViewerForEntry(context, entry);
			if ( parent != null ) {
				MSAnnotationPeaksView me = null;
				Object oActiveTab = parent.getPageItem( parent.getActivePage() );
				if( oActiveTab instanceof MSAnnotationDetails ) {
					me = (MSAnnotationPeaksView) parent.getAnnotationDetails().getCurrentPeaksView();
				} else if( me instanceof MSAnnotationPeaksView ) {
					me = (MSAnnotationPeaksView) parent.getPeaksView();
				}
				if( me == null )
					return;
				MSAnnotationSelectionView viewer = ViewRowChooserInTabCommandExecutor.showRowChooser(me, parentTable, iRowIndex, iScanNum, sRowId);
				me.setSelectionView(viewer);
				me.getBottomPane().layout();
			}
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);
		}
	}

	public static MSAnnotationMultiPageViewer getActiveViewerForEntry(IEclipseContext context, Entry entry ) {
		EPartService partService = context.get(EPartService.class);
		for (MPart part: partService.getParts()) {
			if (part.getObject() instanceof MSAnnotationMultiPageViewer) {
				if (((MSAnnotationMultiPageViewer)part.getObject()).getEntry().equals(entry)) {
					return (MSAnnotationMultiPageViewer)part.getObject();
				}
			}
		}
		return null;
	}

	public static MSAnnotationMultiPageViewer getActiveViewer(IEclipseContext context) {
		MPart part = (MPart) context.get(IServiceConstants.ACTIVE_PART);
		if (part != null && part.getObject() instanceof MSAnnotationMultiPageViewer)
			return (MSAnnotationMultiPageViewer) part.getObject();
		return null;
	}


	public static Entry getEntryByAnnotationId( String _sAnnotationId ) {
		Entry root = DataModelHandler.instance().getRoot();
		List<Entry> kids = root.getChildren();
		Entry foundEntry = null;
		for( int i = 0; i < kids.size(); i++ ) {
			Entry res = getEntryByAnnotationId(_sAnnotationId, kids.get(i) );
			if( res != null ) {
				foundEntry = res;
				break;
			}
		}
		return foundEntry;
	}

	private static Entry getEntryByAnnotationId( String _sAnnotationId, Entry _entry ) {
		if(_entry.getProperty() instanceof MSAnnotationProperty ) {
			MSAnnotationProperty prop = (MSAnnotationProperty) _entry.getProperty();
			if( _sAnnotationId.equals( prop.getMSAnnotationMetaData().getAnnotationId() ) ) {
				return _entry;
			}
		}
		if( _entry.hasChildren() ) {
			List<Entry> kids = _entry.getChildren();
			for( int i = 0; i < kids.size(); i++ ) {
				Entry res = getEntryByAnnotationId(_sAnnotationId, kids.get(i) );
				if( res != null ) {
					return res;
				}
			}			
		}
		return null;
	}

	@Persist
	public void doSave(IProgressMonitor monitor) {
		GRITSProgressDialog progressDialog = new GRITSProgressDialog(Display.getCurrent().getActiveShell(), 0, false, false);
		progressDialog.open();
		progressDialog.getMajorProgressBarListener().setMaxValue(5 + alPeaksViews.size());
		progressDialog.setGritsWorker(new GRITSWorker() {

			@Override
			public int doWork() {
				if( propertyView != null && propertyView.isDirty()) {
					updateListeners("Saving properties", 1);
					propertyView.doSave(monitor);
				}
				if( scansView != null && scansView.isDirty()) {
					updateListeners("Saving scans", 2);
					scansView.doSave(monitor);
				}
				int i=3;
				for( MassSpecPeaksView peaksView : alPeaksViews ) {
					if( peaksView != null && peaksView.isDirty()) {
						updateListeners("Saving changes", i++);
						peaksView.doSave(monitor);
					}
				}
				if( msAnnotPropertyView != null && msAnnotPropertyView.isDirty()) {
					updateListeners("Saving annotation properties", i++);
					msAnnotPropertyView.doSave(monitor);
				}
				if( annotDetails != null && annotDetails.isDirty()) {
					updateListeners("Saving annotation details", i++);
					annotDetails.doSave(monitor);
				}

				setDirty(false);
				updateListeners("Done saving", i);
				return GRITSProcessStatus.OK;
			}
		});
		progressDialog.startWorker();
	}

	/** 
	 * this method is called whenever a page (tab) is updated 
	 * However we have to check to make sure the modified page is one of the pages of this
	 * multi-page editor
	 *  
	 * @param the part that gets dirty
	 */
	@Optional @Inject
	public void tabContentModified (@UIEventTopic
			(EntryEditorPart.EVENT_TOPIC_CONTENT_MODIFIED) IEntryEditorPart part) {
		super.tabContentModified(part);
		if (part.equals(msAnnotPropertyView) || part.equals(annotDetails))
			setDirty (part.isDirty());
	}

	public MSAnnotationDetails getDetailsView() {
		return annotDetails;
	}

	public MSAnnotationPeaksView getPrimaryAnnotatedPeaksView() {
		if( annotDetails != null ) { // Details Viewer, we want the peaks view w/in the details viewer
			return annotDetails.getCurrentPeaksView();
		} else {
			return (MSAnnotationPeaksView) getPeaksView().get(0);
		}
	}

	@Override
	public boolean needsPeaksView(MassSpecEntityProperty entityProperty) {
		boolean bRes = entityProperty.getScanNum() != null && entityProperty.getScanNum() != -1;
		bRes |= ( entityProperty.getParentScanNum() != null && entityProperty.getParentScanNum() != -1 );
		return bRes;
	}

	@Override
	protected void updateColumnVisibility( MassSpecTable table, MassSpecViewerPreference curPref, MassSpecViewerPreference updatePref ) {
		if( curPref.getClass().equals(updatePref.getClass()) && 
				curPref.getMSLevel() == updatePref.getMSLevel() && 
				curPref.getFillType() == updatePref.getFillType() ) {
			if( ! updatePref.getColumnSettings().equals(curPref.getColumnSettings()) ){
				table.getGRITSTableDataObject().setTablePreferences( updatePref );
				table.updateViewFromPreferenceSettings();	
			}
			if( Boolean.compare( ((MSAnnotationViewerPreference)curPref).isHideUnannotatedPeaks(), ((MSAnnotationViewerPreference) updatePref).isHideUnannotatedPeaks()) != 0 ) {
				table.getGRITSTableDataObject().setTablePreferences( updatePref );
				// do the opposite
				if( ((MSAnnotationViewerPreference)curPref).isHideUnannotatedPeaks() ) {
					((MSAnnotationTable)table).showUnannotatedRows();
				} else {
					((MSAnnotationTable)table).hideUnannotatedRows();
				}
			}
		}		
	}

	@Override
	protected void updateColumnVisibility( MassSpecViewerPreference updatePref ) {
		if( getScansView() != null ) {
			try {
				MassSpecScansView scansView = getScansView();
				updateColumnVisibilityForView(scansView, updatePref);
			} catch( Exception ex ) {
				logger.error("Error updating scans view from editor: " + getTitle(), ex);
			}
		} 
		if ( getPeaksView() != null ) {
			for( int j = 0; j < getPeaksView().size(); j++ ) {	
				try {
					MSAnnotationPeaksView peaksView = (MSAnnotationPeaksView) getPeaksView().get(j);
					updateColumnVisibilityForView(peaksView, updatePref);
					if( peaksView.getSelectionView() != null ) {
						MSAnnotationTable table = peaksView.getSelectionView().getSubTable();
						MassSpecViewerPreference curPref = (MassSpecViewerPreference) table.getGRITSTableDataObject().getTablePreferences();
						updateColumnVisibility(table, curPref, updatePref);
					}
				} catch( Exception ex ) {
					logger.error("Error updating peak view: " + j + " from editor: " + getTitle(), ex);
				}
			}		
		}

		if( getDetailsView() != null ) {
			if ( getDetailsView().getPeaksViews() != null ) {
				for( int j = 0; j < getDetailsView().getPeaksViews().size(); j++ ) {	
					try {
						MSAnnotationPeaksView peaksView = getDetailsView().getPeaksViews().get(j);
						updateColumnVisibilityForView(peaksView, updatePref);
						if( peaksView.getSelectionView() != null ) {
							MSAnnotationTable table = peaksView.getSelectionView().getSubTable();
							MassSpecViewerPreference curPref = (MassSpecViewerPreference) table.getGRITSTableDataObject().getTablePreferences();
							updateColumnVisibility(table, curPref, updatePref);
						}

						
//						if( peaksView.getViewBase() == null ) {
//							continue;
//						}
//						MSAnnotationTableBase viewBase = peaksView.getViewBase();
//						if( viewBase.getNatTable() == null ) {
//							return;
//						}
//						MSAnnotationTable table = (MSAnnotationTable) getDetailsView().getPeaksViews().get(j).getViewBase().getNatTable();
//						MSAnnotationViewerPreference curPref = (MSAnnotationViewerPreference) table.getGRITSTableDataObject().getTablePreferences();
//						updateColumnVisibility(table, curPref, updatePref);
					} catch( Exception ex ) {
						logger.error("Error updating details peak view: " + j + " from editor: " + getTitle(), ex);
					}
				}		
			}
		}
	}

	@Optional @Inject
	public void updatePreferences(@UIEventTopic(IGritsPreferenceStore.EVENT_TOPIC_PREF_VALUE_CHANGED)
	String preferenceName)
	{
		if(preferenceName != null && preferenceName.startsWith(MSAnnotationViewerPreference.class.getName())) {
			PreferenceEntity preferenceEntity;
			try {
				preferenceEntity = gritsPreferenceStore.getPreferenceEntity(preferenceName);

				MSAnnotationViewerPreference updatePref = (MSAnnotationViewerPreference) TableViewerPreference.getTableViewerPreference(preferenceEntity, MSAnnotationViewerPreference.class);
				this.updateColumnVisibility(updatePref);
			} catch (UnsupportedVersionException e) {
				logger.error("Error updating column visibility", e);
			}
		}
	}

	public int reLoadStructureAnnotationTab(List<String> columnKeys) {
		this.dtpdThreadedDialog = new GRITSProgressDialog(Display.getCurrent().getActiveShell(), 1, false);
		this.dtpdThreadedDialog.open();
		this.dtpdThreadedDialog.getMajorProgressBarListener().setMaxValue(1);
		MSAnnotationMultiPageViewerTableRefreshWorker msmpvw = new MSAnnotationMultiPageViewerTableRefreshWorker(this, columnKeys);
		this.dtpdThreadedDialog.setGritsWorker(msmpvw);
		int iSuccess = this.dtpdThreadedDialog.startWorker();
		return iSuccess;		
	}

	public int reInitPeaksView(List<String> columnKeys) {
		try {
			this.alPeaksViews.get(0).getTableDataProcessor().setProgressBarDialog(this.dtpdThreadedDialog);
			this.alPeaksViews.get(0).reLoadView();
			this.alPeaksViews.get(0).getTopPane().layout();
			if( columnKeys != null ) {
				for( String sKeyVal : columnKeys ) {
					this.alPeaksViews.get(0).getViewBase().getNatTable().moveToFirstColumn(sKeyVal);
				}
				this.alPeaksViews.get(0).getViewBase().getNatTable().updatePreferenceSettingsFromCurrentView();
				this.alPeaksViews.get(0).getViewBase().getNatTable().getGRITSTableDataObject().getTablePreferences().writePreference();	
			}
			return GRITSProcessStatus.OK;
		} catch( Exception ex ) {
			logger.error("Unable to open scans view", ex);
			return GRITSProcessStatus.ERROR;
		}		
	}


	public static String[] getPreferencePageLabels( int _iMSLevel ) {
		if( _iMSLevel == 1 ) {
			return new String[]{"MS Scans"};
		} else {
			return new String[]{"MS Scans", "Annotated Peak List"};
		}
	}

	public static FillTypes[] getPreferencePageFillTypes( int _iMSLevel ) {
		if( _iMSLevel == 1 ) {
			return new FillTypes[]{FillTypes.Scans};
		} else {
			return new FillTypes[]{FillTypes.Scans, FillTypes.PeaksWithFeatures};
		}
	}

	public static int getPreferencePageMaxNumPages() {
		return 2;
	}

	/**
	 * At this level we don't have structure based filters, so return null
	 * 
	 * @return filter
	 */
	protected AnnotationFilter getFilter() {
		return null;
	}
	
}
