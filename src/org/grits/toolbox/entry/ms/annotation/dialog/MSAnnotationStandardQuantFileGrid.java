package org.grits.toolbox.entry.ms.annotation.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.dialog.MassSpecStandardQuantFileGrid;
import org.grits.toolbox.entry.ms.property.MassSpecEntityProperty;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.grits.toolbox.entry.ms.property.datamodel.MassSpecUISettings;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;

public class MSAnnotationStandardQuantFileGrid extends MassSpecStandardQuantFileGrid {
	private static final Logger logger = Logger.getLogger(MSAnnotationStandardQuantFileGrid.class);

	public MSAnnotationStandardQuantFileGrid(Composite parent, MassSpecMultiPageViewer contextViewer) {
		super(parent, contextViewer);
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

	/**
	 * @return the MassSpecProperty from the Entry associated with the current open MassSpecViewer
	 */ 
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
	 * Returns the MassSpecUISettings object to be used to list which files are associated with the entry.
	 * @return the MassSpecUISettings for the current entry
	 */
	protected MassSpecUISettings getEntrySettings() {
		MSAnnotationProperty prop = (MSAnnotationProperty) getEntryParentProperty();
		MSAnnotationMetaData msSettings = prop.getMSAnnotationMetaData();
		return msSettings;
	}
	
	/**
	 * Returns the list of files from the source MS entry that the user may use for internal standard quant
	 * @return the list of available files in MS entry (external quant and annotation)
	 */
	@Override
	protected List<MSPropertyDataFile> getStandardQuantificationFiles() {
		MassSpecProperty msp = (MassSpecProperty) getMSEntryParentProperty();
		MassSpecUISettings entrySettings = msp.getMassSpecMetaData();
		List<MSPropertyDataFile> fileList = new ArrayList<>();
		fileList.addAll( entrySettings.getAnnotationFiles() );
		fileList.addAll( entrySettings.getQuantificationFiles() );
		return fileList;
	}
	
}
