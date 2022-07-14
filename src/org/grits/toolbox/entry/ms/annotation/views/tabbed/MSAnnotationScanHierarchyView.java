package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.entry.ms.annotation.command.ViewMSOverviewCommandExecutor;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecScanHierarchyView;
import org.grits.toolbox.ms.annotation.utils.AnnotationRowExtraction;
import org.grits.toolbox.ms.file.scan.data.ScanView;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.Scan;
import org.grits.toolbox.ms.om.data.ScanFeatures;
import org.grits.toolbox.ms.om.io.xml.AnnotationReader;

public class MSAnnotationScanHierarchyView extends MassSpecScanHierarchyView {
	static final Logger logger = Logger.getLogger(MSAnnotationScanHierarchyView.class);
	
	AnnotationReader xmlReader;
	String sSourceFile;
	Data data;
	Method method;
	
	@Override
	protected void readData() {
		if (property == null) {
			this.data = null;
			return;
		}
			
		if (this.data == null) { // try to load once
			this.xmlReader = new AnnotationReader();
			if (parentView.getEntry().getProperty() instanceof MSAnnotationEntityProperty) {
				MSAnnotationEntityProperty msAnnotEntityProp = (MSAnnotationEntityProperty) parentView.getEntry().getProperty();
				MSAnnotationProperty msAnnotProp = (MSAnnotationProperty) msAnnotEntityProp.getParentProperty();
				sSourceFile = msAnnotProp.getFullyQualifiedArchiveFileNameByAnnotationID(parentView.getEntry());
				logger.debug("Reading archive: " + sSourceFile);
				this.data = xmlReader.readDataWithoutFeatures(sSourceFile);
				this.method = data.getDataHeader().getMethod();
			}
		}
	}
	
	@Override
	protected Integer getNumberOfAnnotations(ScanView scanView) {
		Integer numAnnotations = null;
		
		if (this.data != null && this.data.getScans() != null && !this.data.getScans().isEmpty()) {
			Scan scan = this.data.getScans().get(scanView.getScanNo());
			if (scan != null) {
				if (scanView.getMsLevel() > 1) {
					int parentScan = scan.getParentScan();
					if (scanView.getMsLevel() == 2) {
						if( method.getMsType().equals(Method.MS_TYPE_INFUSION) ) {
							parentScan = data.getFirstMS1Scan();
						}
					}
					if (sSourceFile != null) {
						ScanFeatures features = this.xmlReader.readScanAnnotation(sSourceFile, parentScan);
						if (features != null) {
							numAnnotations = findNumberOfAnnotations (features, scanView);
						}
					} 
				}
				else 
					numAnnotations = scan.getNumAnnotatedPeaks();
			}
		}
		return numAnnotations;
	}
	
	private int findNumberOfAnnotations(ScanFeatures features, ScanView scanView) {
		HashMap<String, List<Feature>> htPeakToFeatures = null;		
		HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> htParentScanToParentPeaksToSubScan = null;
		htPeakToFeatures = AnnotationRowExtraction.createRowIdToFeatureHash(features);
		boolean bNeedsConvert = false;
		if( htPeakToFeatures.isEmpty() ) { // no features by row ids? Is this an old project? try the old way
			htPeakToFeatures = AnnotationRowExtraction.createPeakIdToFeatureHash(features);
			bNeedsConvert = true;
		}
		if( htPeakToFeatures.isEmpty() ) {
			return 0; // fail
		}
		htParentScanToParentPeaksToSubScan = AnnotationRowExtraction.createParentScanToParentPeaksToSubScanHash(data);
		if( this.method.getMsType().equals(Method.MS_TYPE_INFUSION) && scanView.getMsLevel() - 1 == 1) {
			AnnotationRowExtraction.updateParentScanToParentPeaksToSubScanHashForDirectInfusion(htParentScanToParentPeaksToSubScan, data) ;
		}	
		for (Peak peak : features.getScanPeaks() ) {
			ArrayList<Scan> precursorScans = AnnotationRowExtraction.getPrecursorScan(data,
					scanView.getParentScan(), peak.getId(), htParentScanToParentPeaksToSubScan);
			if( bNeedsConvert ) {
				AnnotationRowExtraction.convertPeakIdsToRowIds( data, features, scanView.getParentScan(), peak.getId(), precursorScans, htPeakToFeatures);
			}
			for( Scan precursorScan : precursorScans ) {
				if (precursorScan != null && precursorScan.getScanNo().equals(scanView.getScanNo())) {
					String sRowId = Feature.getRowId(peak.getId(), precursorScan != null ? precursorScan.getScanNo() : null, features.getUsesComplexRowId());
					int iNumMatch = 0;
					if( htPeakToFeatures.containsKey(sRowId) ) {    
						List<Feature> alFeatures = htPeakToFeatures.get(sRowId);
						for( Feature feature : alFeatures ) {    
							if( ((MSAnnotationEntityProperty) parentView.getEntry().getProperty()).getAnnotationId() != -1 &&
									! ((MSAnnotationEntityProperty) parentView.getEntry().getProperty()).getAnnotationId().equals(feature.getAnnotationId()) ) {
								continue;
							}
							iNumMatch++;
						}
					}
					return iNumMatch;
				}
			}
		}
		
		return 0;
	}

	@Override
	public void mouseDoubleClick(ScanView selected) {	
		// find the path to the selected ScanView
		List<ScanView> path = findPath (selected);
		int iAnnotId = -1;
		String annotIdString = null;
		
		if( parentView.getEntry().getProperty() instanceof MSAnnotationEntityProperty ) {
			Property parentProperty = ((MSAnnotationEntityProperty) parentView.getEntry().getProperty()).getParentProperty();
			if (parentProperty instanceof MSAnnotationProperty) {
				annotIdString = ((MSAnnotationProperty) parentProperty).getMSAnnotationMetaData().getAnnotationId();
				if (annotIdString != null && !annotIdString.isEmpty())
					iAnnotId = Integer.parseInt(annotIdString);
			}
		}
		
		Entry annotEntry = MSAnnotationMultiPageViewer.getEntryByAnnotationId (annotIdString);
		showMSOverview(annotEntry);
		IEclipseContext parentViewerContext = parentView.getContext();
		int i=0;
		for(ScanView scan: path) {
			MSAnnotationMultiPageViewer msAnnotView = MSAnnotationMultiPageViewer.getActiveViewer( parentViewerContext );
			if (scan.getMsLevel() > 2) {
				if (msAnnotView.getDetailsView() != null) {
					MSAnnotationTable msTable = (MSAnnotationTable) msAnnotView.getDetailsView().getViewBase().getNatTable();
					msAnnotView.setActivePage(msAnnotView.getAnnotDetailsTabIndex());
					MSAnnotationTableDataObject tdo = (MSAnnotationTableDataObject) msTable.getGRITSTableDataObject();
					iAnnotId = findAnnotId(msTable, tdo, scan);
					if (i < path.size()-1) // don't double-click for the last one
						msTable.performDoubleClickOnScan(scan.getScanNo(), scan.getPreCursorMz(), iAnnotId, null, scan.getMsLevel(), scan.getParentScan());
				} else if (msAnnotView.getPeaksView() != null && !msAnnotView.getPeaksView().isEmpty()) {
					MSAnnotationTable msTable = (MSAnnotationTable) msAnnotView.getPeaksView().get(0).getViewBase().getNatTable();
					MSAnnotationTableDataObject tdo = (MSAnnotationTableDataObject) msTable.getGRITSTableDataObject();
					iAnnotId = findAnnotId(msTable, tdo, scan);
					if (i < path.size()-1) // don't double-click for the last one
						msTable.performDoubleClickOnScan(scan.getScanNo(), scan.getPreCursorMz(), iAnnotId, null, scan.getMsLevel(), scan.getParentScan());
				} else {
					MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "Could not open scan " + scan.getScanNo() + " page");
				}
			}
			else if (scan.getMsLevel() > 1) {
				MSAnnotationTable msTable = (MSAnnotationTable) msAnnotView.getPeaksView().get(0).getViewBase().getNatTable();
				MSAnnotationTableDataObject tdo = (MSAnnotationTableDataObject) msTable.getGRITSTableDataObject();
				iAnnotId = findAnnotId(msTable, tdo, scan);
				if (i < path.size()-1) // don't double-click for the last one
					msTable.performDoubleClickOnScan(scan.getScanNo(), scan.getPreCursorMz(), iAnnotId, null, scan.getMsLevel(), scan.getParentScan());
			}
			else {
				MSAnnotationTable msTable = (MSAnnotationTable) msAnnotView.getScansView().getViewBase().getNatTable();
				msTable.performDoubleClickOnScan(scan.getScanNo(), 0.0, -1, null, scan.getMsLevel(), scan.getParentScan());
			}
			i++;
		}
	}
	
	protected Integer findAnnotId(MSAnnotationTable msTable, MSAnnotationTableDataObject tdo, ScanView scan) {
		// now find the row that matches by scan number
		int iAnnotId = -1;
		for( int i = 0; i < msTable.getBottomDataLayer().getRowCount(); i++ ) {
			Object targetScanObj = msTable.getBottomDataLayer().getDataValueByPosition(tdo.getScanNoCols().get(0), i);	
			if (targetScanObj == null) // skip this row: this might happen in MS2 tables (detailsView) since not each peak has MS3 
				continue;
			int iTargetScanNum = -1;		
			try {
				iTargetScanNum = Integer.parseInt(targetScanObj.toString());
			} catch(NumberFormatException ex) {
				logger.debug(ex.getMessage(), ex);
			}
			if(iTargetScanNum == -1) {
				logger.error("Unable to find scan number source table. Can't continue.");
				return -1;				
			}
			if (scan.getScanNo() != -1 && iTargetScanNum == scan.getScanNo()) {
				// matched! Now look up the annotation id for that row
				if( tdo.getAnnotationIdCols()!= null && ! tdo.getAnnotationIdCols().isEmpty() ) {
					Object annotObj = msTable.getBottomDataLayer().getDataValueByPosition( tdo.getAnnotationIdCols().get(0), i);	
					if( annotObj != null ) {
						try {
							iAnnotId = Integer.parseInt(annotObj.toString());
						} catch(NumberFormatException ex) {
							logger.error(ex.getMessage(), ex);
							return -1;
						}
					}
				}
				msTable.performMouseDown(i);
				break;
			}
		}
		
		return iAnnotId;
	}
	
	@Override
	protected void showMSOverview(Entry newEntry) {
		ViewMSOverviewCommandExecutor.showMSOverview(parentView.getContext(), newEntry);		
	}
	
	
	@Override
	public void initializeView(List<ScanView> scans) {
		super.initializeView(scans);
		// clear the toggle button (show annotated)'s status since the view's contents have been changed
		List<MToolBarElement> items = this.part.getToolbar().getChildren();
		for (MToolBarElement mToolBarElement : items) {
			if (mToolBarElement instanceof MToolItem) {
				((MToolItem) mToolBarElement).setSelected(false);    // clear toggle button
			}
		}
	}
		
	@Override
	public void filter(boolean filter) {
		if (filter) {
			// show progress dialog and apply filter to select the ones with an annotation
			class FilterProcess implements IRunnableWithProgress {
				List<ScanView> filteredScans;
				
				@Override
		        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
		        {
		            int totalWork = 1;
		            if (scanList != null && !scanList.isEmpty()) {
		            	// get the last scan number
		            	totalWork = scanList.get(scanList.size()-1).getScanNo();
		            }
		            monitor.beginTask("Filtering...", totalWork);
			    	filteredScans = filterScansWithAnnotation (scanList, monitor);
			    	monitor.done();
			    }
			    List<ScanView> getFilteredScans () {
			    	return filteredScans;
			    }
			};
			FilterProcess p = new FilterProcess();
			try {
				new ProgressMonitorDialog(new Shell()).run(true, false, p);
				treeViewer.setInput(p.getFilteredScans());
				treeViewer.refresh();
			} catch (InvocationTargetException e) {
				logger.error("Error while filtering", e);
			} catch (InterruptedException e) {
				logger.error("Error while filtering", e);
			}
		}
		else {
			// back to original
			treeViewer.setInput(scanList);
			treeViewer.refresh();
		}
	}
	
	List<ScanView> filterScansWithAnnotation (List<ScanView> scanList, IProgressMonitor monitor) {
		List<ScanView> filteredScans = new ArrayList<>();
		for (ScanView scanView : scanList) {
			Integer annotations = getNumberOfAnnotations(scanView);
			if (annotations != null && annotations > 0) {
				ScanView scanViewCopy = new ScanView();
				scanViewCopy.setMsLevel(scanView.getMsLevel());
				scanViewCopy.setParentScan(scanView.getParentScan());
				scanViewCopy.setPreCursorIntensity(scanView.getPreCursorIntensity());
				scanViewCopy.setPreCursorMz(scanView.getPreCursorMz());
				scanViewCopy.setRetentionTime(scanView.getRetentionTime());
				scanViewCopy.setScanNo(scanView.getScanNo());
				if (scanView.getSubScans() != null) {
					monitor.subTask("Subscans of " + scanView.getScanNo());
					scanViewCopy.setSubScans(filterScansWithAnnotation(scanView.getSubScans(), monitor));
				}
				if (method.getMsType().equals(Method.MS_TYPE_INFUSION)) {
					// do not add top level scans unless they have sub scans with an annotation
					if (scanViewCopy.getMsLevel() != 1 || !scanViewCopy.getSubScans().isEmpty())   
						filteredScans.add(scanViewCopy);
				} else
					filteredScans.add(scanViewCopy);
			}
			monitor.worked(1);
		}
		return filteredScans;
	}
}
