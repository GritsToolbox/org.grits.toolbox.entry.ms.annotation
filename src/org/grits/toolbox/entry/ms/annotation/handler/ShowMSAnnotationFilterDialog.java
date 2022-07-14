package org.grits.toolbox.entry.ms.annotation.handler;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationFilterWindow;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;

public class ShowMSAnnotationFilterDialog {
	private static final Logger logger = Logger.getLogger(ShowMSAnnotationFilterDialog.class);
	
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_PART) MPart part,
			@Named (IServiceConstants.ACTIVE_SHELL) Shell shell, 
			EPartService partService) {
		MassSpecMultiPageViewer curView = null;
		if (part != null && part.getObject() instanceof MassSpecMultiPageViewer ) {
			curView = (MassSpecMultiPageViewer) part.getObject();
		}
		else { // try to find an open part of the required type
			for (MPart mPart: partService.getParts()) {
				if (mPart.getObject() instanceof MassSpecMultiPageViewer) {
					if (mPart.equals(mPart.getParent().getSelectedElement())) {
						curView = (MassSpecMultiPageViewer) mPart.getObject();
						if (curView.getEntry().getProperty() instanceof MSAnnotationEntityProperty)
							break;
					}
				}
			}
		}
		if( curView == null || ! (curView.getEntry().getProperty() instanceof MSAnnotationEntityProperty) ) {
			logger.warn("No MS Annotation Results are open.\nPlease open the view and then apply the filter.");
			ErrorUtils.createWarningMessageBox(
					shell,
					"Unable to Perform Filter", "No MS Annotation Results are open.\nPlease open the view and then apply the filter.");
			return;
		}
		
		final MSAnnotationFilterWindow win = new MSAnnotationFilterWindow(shell, curView.getEntry(), part);
		win.open();
	}
}
