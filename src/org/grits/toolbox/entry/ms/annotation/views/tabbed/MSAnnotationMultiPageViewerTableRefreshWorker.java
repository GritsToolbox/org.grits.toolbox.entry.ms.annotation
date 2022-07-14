package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import java.util.List;

import org.apache.log4j.Logger;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewerTableRefreshWorker;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;

public class MSAnnotationMultiPageViewerTableRefreshWorker extends MassSpecMultiPageViewerTableRefreshWorker {
	private static final Logger logger = Logger.getLogger(MSAnnotationMultiPageViewerTableRefreshWorker.class);

	public MSAnnotationMultiPageViewerTableRefreshWorker(MassSpecMultiPageViewer parentEditor, List<String> columnKeys) {
		super(parentEditor, columnKeys);
	}

	@Override
	public int doWork() {
		iMajorCount = 0;
		updateListeners("Starting MassSpec work!", 1);
		int iSuccess = updateMSAnnotationTab(iMajorCount);
		iMajorCount+=1;  // 2 steps to load the scans!!
		if( iSuccess != GRITSProcessStatus.OK ) {
			return iSuccess;
		} 
		updateListeners("Finished MassSpec work!", iMajorCount);
		logger.debug("Finished MassSpec work");
		return iSuccess;
	}

	/**
	 * Refreshes the Peaks view table(s), moving any columns containing the labels in the column keys list to the beginning
	 * @param iProcessCount
	 * @return
	 */
	protected int updateMSAnnotationTab(int iProcessCount) {
		try {
			updateListeners("Updating Structure Annotation tab (loading)", iProcessCount);
			((MSAnnotationMultiPageViewer) getParentEditor()).reInitPeaksView(this.columnKeys);
			updateListeners("Updating MS Scans tab (done)", iProcessCount + 1);
			return GRITSProcessStatus.OK;				 
		} catch( Exception e ) {
			logger.error("Unable to update Structure Annotation view", e);
		}
		return GRITSProcessStatus.ERROR;
		
	}
	
}
