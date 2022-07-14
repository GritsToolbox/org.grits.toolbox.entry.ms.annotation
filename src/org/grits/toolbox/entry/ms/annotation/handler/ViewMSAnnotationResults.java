package org.grits.toolbox.entry.ms.annotation.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.editor.ICancelableEditor;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;


/**
 * Create a new MS dialog
 * @author dbrentw
 *
 */
public class ViewMSAnnotationResults {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(ViewMSAnnotationResults.class);
	
	public static final String PARAMETER_ID = "viewSpecResults_Entry";
	public static final String COMMAND_ID = "org.grits.toolbox.entry.ms.handler.viewMSAnnotationResults";
	
	@Inject static IGritsDataModelService gritsDataModelService = null;
    @Inject static IGritsUIService gritsUIService = null;
    @Inject EPartService partService;

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			@Named (IServiceConstants.ACTIVE_SHELL) Shell shell, 
			@Optional @Named (PARAMETER_ID) Entry entry) {
		if ( entry == null ) {
			Entry selectedEntry = null;
			if(object instanceof Entry)
			{
				selectedEntry = (Entry) object;
			}
			else if (object instanceof StructuredSelection)
			{
				if(((StructuredSelection) object).getFirstElement() instanceof Entry)
				{
					selectedEntry = (Entry) ((StructuredSelection) object).getFirstElement();
				}
			}
			// try getting the last selection from the data model
			if(selectedEntry == null
					&& gritsDataModelService.getLastSelection() != null
					&& gritsDataModelService.getLastSelection().getFirstElement() instanceof Entry)
			{
				selectedEntry = (Entry) gritsDataModelService.getLastSelection().getFirstElement();
			}
			entry = selectedEntry;
		}
		showPlugInView(shell, entry);
	}
		
	private void showPlugInView(Shell shell, Entry entry) {
		
		if(entry != null)
		{
			MPart part = null;
			try {
				part = gritsUIService.openEntryInPart(entry);
				if (part != null && part.getObject() != null && part.getObject() instanceof ICancelableEditor) {
					if ( ((ICancelableEditor) part.getObject()).isCanceled()) {
						partService.hidePart(part, true);
					}
				}
			}
			catch (Exception e) {
				Exception pie = new Exception("There was an error converting the XML to a table.", e);
				logger.error(pie.getMessage(),pie);
				ErrorUtils.createErrorMessageBox(shell, "Unable to open the results viewer", pie);
				if (part != null)
					partService.hidePart(part, true);
			}
		}
	}

}
