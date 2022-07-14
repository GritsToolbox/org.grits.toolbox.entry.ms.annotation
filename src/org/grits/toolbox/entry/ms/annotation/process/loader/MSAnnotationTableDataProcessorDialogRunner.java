package org.grits.toolbox.entry.ms.annotation.process.loader;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.widgets.progress.CancelableThread;
import org.grits.toolbox.widgets.progress.IProgressThreadHandler;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;

import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.display.control.table.process.TableDataProcessorRunner;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.Peak;

public class MSAnnotationTableDataProcessorDialogRunner extends TableDataProcessorRunner
{
	private static final Logger logger = Logger.getLogger(MSAnnotationTableDataProcessorDialogRunner.class);
	protected HashMap<Feature, Peak> scanFeaturesToPeak = null;

	public MSAnnotationTableDataProcessorDialogRunner(TableDataProcessor extractor) {
		super(extractor);
	}    

	public HashMap<Feature, Peak> getScanFeatures() {
		return scanFeaturesToPeak;
	}

	@Override
	public int startJob() {
		try {
			scanFeaturesToPeak = null;
			if( extractor.getProcessType() == MSAnnotationTableDataProcessor.READ_FEATURES) {
				CancelableThread t = new CancelableThread() {
					@Override
					public boolean threadStart(IProgressThreadHandler a_progressThreadHandler) throws Exception {
						logger.debug("Starting job: " + extractor.getProcessType() );
						try {
							scanFeaturesToPeak = ( (MSAnnotationTableDataProcessor) extractor).getFeaturesForPeak();
							return true;
						} catch(Exception e) {
							logger.error(e.getMessage(), e);
						}
						return false;
					}
				};
				t.setProgressThreadHandler(extractor.getProgressBarDialog());
				extractor.getProgressBarDialog().setThread(t);
				t.start();	
				while ( ! t.isCanceled() && ! t.isFinished() && t.isAlive() ) 
				{
					if (!Display.getDefault().readAndDispatch()) 
					{
						Display.getDefault().sleep();
					}
				}
				if( t.isCanceled() ) {
					return GRITSProcessStatus.CANCEL;
				}
				return GRITSProcessStatus.OK;
			} else {
				return super.startJob();
			}
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return GRITSProcessStatus.ERROR;
	}

}