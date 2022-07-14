package org.grits.toolbox.entry.ms.annotation.dialog;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.dialog.MassSpecExternalQuantDialog;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.grits.toolbox.entry.ms.property.datamodel.MassSpecUISettings;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;

/**
 * Extends the parent MassSpecExternalQuantDialog to facilitate adding and/or removing external quant data to 
 * annotated MS data.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class MSAnnotationExternalQuantDialog extends MassSpecExternalQuantDialog  {
	private static final Logger logger = Logger.getLogger(MSAnnotationExternalQuantDialog.class);

	public MSAnnotationExternalQuantDialog(Shell parentShell, MassSpecMultiPageViewer curView) {
		super(parentShell, curView);
		setShellStyle(SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.DIALOG_TRIM );
	}
	
	/**
	 * @return the MassSpecProperty of the annotation's parent MS entry
	 */
	public Property getMSEntryParentProperty() {
		try {
			Entry entry = getEntryForCurrentViewer();
			MSAnnotationEntityProperty msep = (MSAnnotationEntityProperty) entry.getProperty();
			MassSpecProperty msp = msep.getMassSpecParentProperty();
			return msp;
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return null;
	}
	
	
	/**
	 * @return the MSAnnotationProperty from the Entry associated with the current open MSAnnotationMultiPageViewer
	 */
	@Override
	public Property getEntryParentProperty() {
		try {
			Entry entry = getEntryForCurrentViewer();
			MSAnnotationEntityProperty msep = (MSAnnotationEntityProperty) entry.getProperty();
			MSAnnotationProperty msap = msep.getMSAnnotationParentProperty();
			return msap;
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return null;
	}
	
	/**
	 * @return the current open MSAnnotationMultiPageViewer
	 */
	@Override
	public MassSpecMultiPageViewer getCurrentViewer() {
		try {
			EPartService partService = getContextViewer().getPartService();
			for (MPart mPart: partService.getParts()) {
				if (mPart.getObject() instanceof MSAnnotationMultiPageViewer) {
					if (mPart.equals(mPart.getParent().getSelectedElement())) {
						MSAnnotationMultiPageViewer viewer = (MSAnnotationMultiPageViewer) mPart.getObject();
						if(viewer != null && viewer.getEntry().getProperty() != null && viewer.getEntry().getProperty() instanceof MSAnnotationEntityProperty ) {						
							return viewer;
						}
					}
				}
			}	
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	protected MassSpecUISettings getEntrySettings() {
		MSAnnotationProperty msp = (MSAnnotationProperty) getEntryParentProperty();
		MassSpecUISettings entrySettings = msp.getMSAnnotationMetaData();
		return entrySettings;
	}
	
	@Override
	protected List<MSPropertyDataFile> getQuantificationFiles() {
		MassSpecProperty msp = (MassSpecProperty) getMSEntryParentProperty();
		MassSpecUISettings entrySettings = msp.getMassSpecMetaData();
		return entrySettings.getQuantificationFiles();
	}
	
	/**
	 * Determines the current viewer and refreshes the GRITS tables that were updated with the quantitation.
	 * For MSAnnotation data, this includes the Scans tab and the Structure Annotation tab. 
	 */
	@Override
	protected void updateViewer() {
		try {
			MSAnnotationMultiPageViewer viewer = (MSAnnotationMultiPageViewer) getCurrentViewer();
			List<String> sKeyVals = getColumnKeyLabels();
//			viewer.reLoadScansTab(sKeyVals);
			viewer.reLoadStructureAnnotationTab(sKeyVals);			
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);			
		}
	}
	
	/**
	 * Updates the MS Settings for the Entry's mass spec property and then marshalls it to the workspace xml file
	 */
	@Override
	protected void updateSettings() {
		MSAnnotationProperty property = (MSAnnotationProperty) getEntryParentProperty();
		// need to save the projectEntry to cause the data files for the MassSpecProperty to be updated
		try {
			Entry projectEntry = getEntryForCurrentViewer();
			String sFileName = property.getFullyQualifiedMetaDataFileName(projectEntry);				
			MSAnnotationMetaData msSettings = property.getMSAnnotationMetaData();
			
			MSAnnotationProperty.marshallSettingsFile(sFileName, msSettings);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);			
		}
	}


}
