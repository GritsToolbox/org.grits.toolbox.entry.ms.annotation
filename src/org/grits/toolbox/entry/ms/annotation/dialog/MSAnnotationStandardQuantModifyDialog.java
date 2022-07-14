package org.grits.toolbox.entry.ms.annotation.dialog;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.dialog.MassSpecStandardQuantModifyDialog;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;

public class MSAnnotationStandardQuantModifyDialog extends MassSpecStandardQuantModifyDialog {
	private static final Logger logger = Logger.getLogger(MSAnnotationStandardQuantModifyDialog.class);

	public MSAnnotationStandardQuantModifyDialog(Shell parentShell, MassSpecMultiPageViewer contextViewer) {
		super(parentShell, contextViewer);
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

}
