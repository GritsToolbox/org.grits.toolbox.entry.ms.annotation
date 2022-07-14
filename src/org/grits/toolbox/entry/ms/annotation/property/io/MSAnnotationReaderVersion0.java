package org.grits.toolbox.entry.ms.annotation.property.io;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.UnsupportedTypeException;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.jdom.Element;

import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationFileInfo;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationMetaData;

/**
 * 
 * @author Brent Weatherly
 *
 */
public class MSAnnotationReaderVersion0 {
	private static final Logger logger = Logger.getLogger(MSAnnotationReaderVersion0.class);

	public static Property read(Element propertyElement, MSAnnotationProperty msAnnotProperty) throws IOException, UnsupportedVersionException, UnsupportedTypeException {
		String t_attributeValue = null;
		Element entryElement = propertyElement.getDocument().getRootElement().getChild("entry");
		String projectName = entryElement == null ? null : entryElement.getAttributeValue("name");

		String workspaceFolder = PropertyHandler.getVariable("workspace_location");
		String msFolder = workspaceFolder.substring(0, workspaceFolder.length()-1) 
				+ File.separator
				+ projectName + File.separator
				+ msAnnotProperty.getArchiveFolder();

		Entry msEntry = new Entry();

		MSAnnotationMetaData model = new MSAnnotationMetaData();
		msAnnotProperty.setMSAnnotationMetaData(model);
		msEntry.setProperty(msAnnotProperty);

		Element child = propertyElement.getChild("ms-annotation");
		if( child != null ) {

			t_attributeValue = child.getAttributeValue("id");
			model.setAnnotationId(t_attributeValue);
		} 
		Element descriptionElement = propertyElement.getChild("descripton");
		String description = descriptionElement == null ? "" : descriptionElement.getValue();
		model.setDescription(description);
		
		MSAnnotationProperty.marshallSettingsFile(msFolder + File.separator + msAnnotProperty.getMetaDataFileName(), model);
		model.setVersion(MSAnnotationMetaData.CURRENT_VERSION);
		model.setName(msAnnotProperty.getMetaDataFileName());

		PropertyDataFile msMetaData = MSAnnotationProperty.getNewSettingsFile(msAnnotProperty.getMetaDataFileName(), model);
		msAnnotProperty.getDataFiles().add(msMetaData);
		
		String sAnnotationFile = model.getAnnotationId() + MSAnnotationProperty.ARCHIVE_EXTENSION;
		File file = new File( msFolder + File.separator + sAnnotationFile );
		PropertyDataFile pdf = null;
		if( file.exists() ) { // archive is a single zip file
			pdf = new PropertyDataFile(sAnnotationFile, MSAnnotationFileInfo.MS_ANNOTATION_CURRENT_VERSION, MSAnnotationFileInfo.MS_ANNOTATION_TYPE_FILE);
		} else { // should be a folder then
			String sAnnotationFolder = model.getAnnotationId();
			file = new File( msFolder + File.separator + sAnnotationFolder );
			if( file.exists() ) {
				pdf = new PropertyDataFile(sAnnotationFolder, MSAnnotationFileInfo.MS_ANNOTATION_CURRENT_VERSION, MSAnnotationFileInfo.MS_ANNOTATION_TYPE_FOLDER);
			}
		}
		
		if( pdf == null ) {
			throw new UnsupportedVersionException("Expecting an archive file or folder. Not found.", "Preversion");
		}
		msAnnotProperty.getDataFiles().add(pdf);
		return msAnnotProperty;
	}
}
