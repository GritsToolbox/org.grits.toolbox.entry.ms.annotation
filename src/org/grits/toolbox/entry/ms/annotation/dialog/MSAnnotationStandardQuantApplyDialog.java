package org.grits.toolbox.entry.ms.annotation.dialog;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.dialog.MassSpecStandardQuantApplyDialog;
import org.grits.toolbox.entry.ms.dialog.MassSpecStandardQuantFileGrid;
import org.grits.toolbox.entry.ms.dialog.MassSpecStandardQuantModifyDialog;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;

public class MSAnnotationStandardQuantApplyDialog extends MassSpecStandardQuantApplyDialog {
	private static final Logger logger = Logger.getLogger(MSAnnotationStandardQuantApplyDialog.class);

	public MSAnnotationStandardQuantApplyDialog(Shell parentShell, MassSpecMultiPageViewer contextViewer) {
		super(parentShell, contextViewer);
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
	
	@Override
	protected MassSpecStandardQuantFileGrid getNewMassSpecStandardQuantFileGrid( Composite parent, MassSpecMultiPageViewer curView ) {
		return new MSAnnotationStandardQuantFileGrid(parent, curView);
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
	 * Determines the current viewer and refreshes the GRITS tables that were updated with the quantitation.
	 * For MSAnnotation data, this includes the Scans tab and the Structure Annotation tab. 
	 */
	protected void updateViewer(List<String> sKeyVals) {
		try {
			MSAnnotationMultiPageViewer viewer = (MSAnnotationMultiPageViewer) getCurrentViewer();
			viewer.reLoadStructureAnnotationTab(sKeyVals);			
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);			
		}
	}
	
	protected MassSpecStandardQuantModifyDialog getNewQuantModifyDialog( Shell shell, MassSpecMultiPageViewer viewer ) {
		return new MSAnnotationStandardQuantModifyDialog(shell, viewer);
	}

	/**
	 * If the currently selected item in the standard quantification combo is applied to the current entry, then the 
	 * data is removed from the MS entry and reloads the scan table.
	 * 
	 */
	@Override
	protected void clearStdQuant() {
		MSAnnotationProperty prop = (MSAnnotationProperty) getEntryParentProperty();
		MSAnnotationMetaData msSettings = prop.getMSAnnotationMetaData();
		try {
			fileGrid.updateIntQuantFileSettings();
			if( msSettings.getStandardQuant().contains(selStandards) ) {
				msSettings.getStandardQuant().remove(selStandards);
			}
			String sFileName = prop.getFullyQualifiedMetaDataFileName(getMassSpecEntry());
			msSettings.updateStandardQuantData();
			MSAnnotationProperty.updateMSSettings(msSettings, sFileName);
			initLocalStandardQuant();
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);
		}
		try {
			updateViewer(null);
			initStoredStandardQuantList();
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}		
	}
		
	@Override
	protected void clearBeforeReApply() {
		MSAnnotationProperty prop = (MSAnnotationProperty) getEntryParentProperty();
		MSAnnotationMetaData msSettings = prop.getMSAnnotationMetaData();
		if( msSettings.getStandardQuant().contains(selStandards) ) {
			try {
				msSettings.getStandardQuant().remove(selStandards);
				String sFileName = prop.getFullyQualifiedMetaDataFileName(getMassSpecEntry());				
				msSettings.updateStandardQuantData();
				MSAnnotationProperty.updateMSSettings(msSettings, sFileName);
				updateViewer(null);

			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}		
		}
	}

	/**
	 * Applies the standard quantification data for the currently selected item to the current entry and reloads the scan table.
	 */
	@Override
	protected void performStdQuant() {
		clearBeforeReApply();
		MSAnnotationProperty prop = (MSAnnotationProperty) getEntryParentProperty();
		MSAnnotationMetaData msSettings = prop.getMSAnnotationMetaData();
		try {
			fileGrid.updateIntQuantFileSettings();
			if( selStandards != null ) { // nice place to fix the possible scenario where a null std quant was added before..ugh
				msSettings.getStandardQuant().add(selStandards);
			}
			String sFileName = prop.getFullyQualifiedMetaDataFileName(getMassSpecEntry());				
			msSettings.updateStandardQuantData();
			MSAnnotationProperty.updateMSSettings(msSettings, sFileName);
			initLocalStandardQuant();
		} catch( Exception e ) {
			logger.error(e.getMessage(), e);
		}
		try {
			List<String> sKeyVals = getColumnKeyLabels();
			updateViewer(sKeyVals);
			initStoredStandardQuantList();
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}		
	}

}
