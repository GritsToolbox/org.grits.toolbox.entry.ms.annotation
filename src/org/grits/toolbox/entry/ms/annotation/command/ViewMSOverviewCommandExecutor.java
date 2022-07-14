package org.grits.toolbox.entry.ms.annotation.command;

import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.entry.ms.annotation.handler.ViewMSAnnotationResults;

@SuppressWarnings("restriction")
public class ViewMSOverviewCommandExecutor  {
	public static void showMSOverview(IEclipseContext context, Entry entry ) {	
		ECommandService commandService = context.get(ECommandService.class);
		EHandlerService handlerService = context.get(EHandlerService.class);
		
		context.set(ViewMSAnnotationResults.PARAMETER_ID, entry);
		handlerService.executeHandler(
			commandService.createCommand(ViewMSAnnotationResults.COMMAND_ID, null));
	}
}
