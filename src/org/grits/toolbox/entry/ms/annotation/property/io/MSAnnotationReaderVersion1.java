package org.grits.toolbox.entry.ms.annotation.property.io;

import java.io.File;

import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.property.Property;
import org.jdom.Element;

import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationMetaData;

/**
 * 
 * @author Brent Weatherly
 *
 */
public class MSAnnotationReaderVersion1
{
	public static Property read(Element propertyElement, MSAnnotationProperty property) {
		property.adjustPropertyFilePaths();
		Element entryElement = propertyElement.getDocument().getRootElement().getChild("entry");
		String projectName = entryElement == null ? null : entryElement.getAttributeValue("name");

		String workspaceFolder = PropertyHandler.getVariable("workspace_location");
		String msFolder = workspaceFolder.substring(0, workspaceFolder.length()-1) 
				+ File.separator
				+ projectName + File.separator
				+ property.getArchiveFolder();
		
		// lets read the settings file
		String settingsFile = property.getMetaDataFile().getName();
		String fullPath = msFolder + File.separator + settingsFile;
		MSAnnotationMetaData msMetaData = MSAnnotationProperty.unmarshallSettingsFile(fullPath);
		property.setMSAnnotationMetaData(msMetaData);
		return property;
	}
}
