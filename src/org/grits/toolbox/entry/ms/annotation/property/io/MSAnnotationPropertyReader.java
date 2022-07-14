package org.grits.toolbox.entry.ms.annotation.property.io;

import java.io.IOException;

import org.grits.toolbox.core.datamodel.UnsupportedTypeException;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.io.PropertyReader;
import org.grits.toolbox.core.datamodel.property.Property;
import org.jdom.Element;

import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;

/**
 * Reader for sample entry. Should check for empty values
 * @author Brent Weatherly
 *
 */
public class MSAnnotationPropertyReader extends PropertyReader {
	@Override
	public Property read(Element propertyElement) throws IOException, UnsupportedVersionException
	{
		MSAnnotationProperty property = getNewMSAnnotationProperty();
		
		PropertyReader.addGenericInfo(propertyElement, property);

		if(property.getVersion() == null) {
			// we must also convert the meta-data to the model and write out. Do that here?
			try {
				MSAnnotationReaderVersion0.read(propertyElement, property);
				PropertyReader.UPDATE_PROJECT_XML = true;
				property.setVersion(MSAnnotationProperty.CURRENT_VERSION);
			} catch (UnsupportedTypeException e) {
				throw new IOException(e.getMessage(), e);
			}
		}
		else if(property.getVersion().equals("1.0")) {
			MSAnnotationReaderVersion1.read(propertyElement, property);
		}
		else 
			throw new UnsupportedVersionException("This version is currently not supported.", property.getVersion());
		
		return property;
	}
	
	protected MSAnnotationProperty getNewMSAnnotationProperty() {
		return new MSAnnotationProperty();
	}
}
