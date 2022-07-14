package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import org.apache.log4j.Logger;
import org.grits.toolbox.entry.ms.property.MassSpecEntityProperty;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewerWorker;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;

public class MSAnnotationMultiPageViewerWorker extends MassSpecMultiPageViewerWorker {
	private static final Logger logger = Logger.getLogger(MSAnnotationMultiPageViewerWorker.class);

	public MSAnnotationMultiPageViewerWorker( MassSpecMultiPageViewer parentEditor, MassSpecEntityProperty prop ) {
		super( parentEditor, prop );
	}

	@Override
	public int doWork() {
		MSAnnotationMultiPageViewer msParentEditor = (MSAnnotationMultiPageViewer) getParentEditor();
		iMajorCount = 0;

		int iSuccess = addPropertyPage(iMajorCount++);
		if( iSuccess != GRITSProcessStatus.OK ) {
			return iSuccess;
		} 

		if( ! msParentEditor.hasMSFile(prop) ) {
			return GRITSProcessStatus.OK;
		}

		if( getParentEditor().getMinMSLevel() < 0 ) {
			iSuccess = determineMinMSLevel(iMajorCount++);
			if( iSuccess != GRITSProcessStatus.OK ) {
				return iSuccess;
			} 
		}

		iSuccess = addMSScansTab(prop, iMajorCount);
		iMajorCount+=2;  // 2 steps to load the scans!!
		if( iSuccess != GRITSProcessStatus.OK ) {
			return iSuccess;
		} 

		iSuccess = addMSAnnotationPropertyPage(iMajorCount++);
		if( iSuccess != GRITSProcessStatus.OK ) {
			return iSuccess;
		} 
		
		iSuccess = addMSFilterPage(iMajorCount++);
		if( iSuccess != GRITSProcessStatus.OK ) {
			return iSuccess;
		} 
		
		iSuccess = addMSOtherSettingsPage(iMajorCount++);
		if( iSuccess != GRITSProcessStatus.OK ) {
			return iSuccess;
		} 
		
		iSuccess = addMSQuantificationPage(iMajorCount++);
		if( iSuccess != GRITSProcessStatus.OK ) {
			return iSuccess;
		} 

		if( getParentEditor().needsPeaksView(prop) ) {
			iSuccess = addPeakListPage(prop, iMajorCount++);
			if( ! msParentEditor.needsDetailsView(prop) ) {
				iMajorCount ++;  
			}
			if( iSuccess != GRITSProcessStatus.OK ) {
				return iSuccess;
			} 
		}

		if( msParentEditor.needsDetailsView(prop) ) {
			iSuccess = addDetailsPage(prop, iMajorCount++);			
			iMajorCount += msParentEditor.getNumDetailSteps();
			if( iSuccess != GRITSProcessStatus.OK ) {
				msParentEditor.setStatus(iSuccess);
				return iSuccess;
			} 
		}

		// Spectra page has to be loaded AFTER peaks view. Hacky to know this here
		if( getParentEditor().needsSpectraView(prop) ) {
			iSuccess = addSpectraPage( prop, iMajorCount++ );
			if( iSuccess != GRITSProcessStatus.OK ) {
				return iSuccess;
			} 
		}
		
		iSuccess = addScanHierarcyPage(prop, iMajorCount++ );
		if( iSuccess != GRITSProcessStatus.OK ) {
			return iSuccess;
		} 

		updateListeners("Finished MS Annotation work!", iMajorCount);
		logger.debug("Finished MS Annotation work");
		return iSuccess;
	}

	public int addMSAnnotationPropertyPage(int iProcessCount ) {
		try {
			updateListeners("Creating MS Annotation Property tab (loading)", iProcessCount);
			int iSuccess = ((MSAnnotationMultiPageViewer) getParentEditor()).addMSAnnotationPropertyView();	
			updateListeners("Creating MS Annotation Property tab (done)", iProcessCount + 1);
			return iSuccess;
		} catch( Exception ex ) {
			logger.error("Unable to open MS Annotation Property view", ex);
			return GRITSProcessStatus.ERROR;
		}
	}
	
	public int addMSFilterPage(int iProcessCount ) {
		try {
			updateListeners("Creating MS Annotation Filters tab (loading)", iProcessCount);
			int iSuccess = ((MSAnnotationMultiPageViewer) getParentEditor()).addMsFilterPage();	
			updateListeners("Creating MS Annotation Filters tab (done)", iProcessCount + 1);
			return iSuccess;
		} catch( Exception ex ) {
			logger.error("Unable to open MS Annotation Filters view", ex);
			return GRITSProcessStatus.ERROR;
		}
	}
	
	public int addMSOtherSettingsPage(int iProcessCount ) {
		try {
			updateListeners("Creating MS Custom Annotation Settings tab (loading)", iProcessCount);
			int iSuccess = ((MSAnnotationMultiPageViewer) getParentEditor()).addOtherSettingsPage();	
			updateListeners("Creating MS Custom Annotation Settings tab (done)", iProcessCount + 1);
			return iSuccess;
		} catch( Exception ex ) {
			logger.error("Unable to open MS Custom Annotation Settings view", ex);
			return GRITSProcessStatus.ERROR;
		}
	}
	
	public int addMSQuantificationPage(int iProcessCount ) {
		try {
			updateListeners("Creating MS Annotation Quantification Settings tab (loading)", iProcessCount);
			int iSuccess = ((MSAnnotationMultiPageViewer) getParentEditor()).addQuantificationPage();	
			updateListeners("Creating MS Annotation Quantification Settings tab (done)", iProcessCount + 1);
			return iSuccess;
		} catch( Exception ex ) {
			logger.error("Unable to open MS Annotation Quantification Settings view", ex);
			return GRITSProcessStatus.ERROR;
		}
	}

	@Override
	public int addPeakListPage(MassSpecEntityProperty prop, int iProcessCount ) {
		try {
			updateListeners("Creating MS Peak tab (loading)", iProcessCount);
			int iSuccess = getParentEditor().addPeakListPage_Step1(prop);
			if( iSuccess != GRITSProcessStatus.OK ) {
				return iSuccess;
			}
			if( ! ((MSAnnotationMultiPageViewer) getParentEditor()).needsDetailsView(prop) ) {
				updateListeners("Creating MS Peak tab (populating)", iProcessCount + 1);
				iSuccess = ((MSAnnotationMultiPageViewer) getParentEditor()).addPeakListPage_Step2();
				updateListeners("Creating MS Peak tab (done)", iProcessCount + 2);
			} else {
				updateListeners("Creating MS Peak tab (done)", iProcessCount + 1);				
			}
			return iSuccess;				 
		} catch( Exception e ) {
			logger.error("Unable to open MS Peaks view", e);
		}
		return GRITSProcessStatus.ERROR;
	}

	public int addDetailsPage(MassSpecEntityProperty prop, int iProcessCount) {
		try {
			updateListeners("Creating Details tab (loading)", iProcessCount);
			int iSuccess = ((MSAnnotationMultiPageViewer) getParentEditor()).addDetailsTab_Step1(prop);
			if( iSuccess != GRITSProcessStatus.OK ) {
				return iSuccess;
			}
			updateListeners("Creating Details tab (populating)", iProcessCount + 1);
			iSuccess = ((MSAnnotationMultiPageViewer) getParentEditor()).addDetailsTab_Step2();
			//			updateListeners("Creating Details tab (done)", iProcessCount += ((MSAnnotationMultiPageViewer) getParentEditor()).getNumDetailSteps());
			return iSuccess;				 
		} catch( Exception e ) {
			logger.error("Unable to open Details view", e);
		}
		return GRITSProcessStatus.ERROR;
	}


}
