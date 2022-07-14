package org.grits.toolbox.entry.ms.annotation.property.datamodel;

import java.util.Iterator;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.grits.toolbox.entry.ms.property.datamodel.MassSpecUISettings;
import org.grits.toolbox.ms.file.FileCategory;

/**
 * The meta-data associated with the annotation of MS data. Extends MassSpecUISettings.
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * @see MassSpecUISettings
 *
 */
@XmlRootElement(name = "msMetaData")
@XmlType(propOrder={"name", "description", "version", "annotationId"})
public class MSAnnotationMetaData extends MassSpecUISettings {
	public static final String CURRENT_VERSION = "1.0";
	private String name = null;
	private String version = null;
	private String description = null;
	private String annotationId = null;
	//	protected Integer annotationId = null;

	public MSAnnotationMetaData() {
		super();
	}
	
	/**
	 * @return the name
	 */
	@XmlAttribute(name = "name", required= true)
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the description
	 */
	@XmlElement(name = "description", required= false)
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the version
	 */
	@XmlAttribute(name = "version", required= true)
	public String getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the annotationId
	 */
	@XmlAttribute(name = "annotationId", required= true)
	public String getAnnotationId() {
		return annotationId;
	}

	/**
	 * @param annotationId the annotationId to set
	 */
	public void setAnnotationId(String annotationId) {
		this.annotationId = annotationId;
	}

	/**
	 * @param annotationFile a source MS file to be used for annotation
	 */
	public void addAnnotationFile( MSPropertyDataFile annotationFile ) {
		addSourceFile(annotationFile);
	}	
	
	/**
	 * Find the file used for annotation. Note this assumes there is only 1 result file for now!!
	 *  
	 * @return a the annotation file
	 */
	public MSPropertyDataFile getAnnotationFile() {
		// TODO: support multiple files as necessary!
		if (getSourceDataFileList() == null)
			return null;
		Iterator<MSPropertyDataFile> itr = getSourceDataFileList().iterator();
		while( itr.hasNext() ) {
			PropertyDataFile file = itr.next();			
			if (file instanceof MSPropertyDataFile) {
				if( ((MSPropertyDataFile) file).getCategory().equals( FileCategory.ANNOTATION_CATEGORY ) ) {
					return (MSPropertyDataFile) file;
				}
			}
		}
		return null; // not found!
	}


	@Override
	public Object clone() {
		MSAnnotationMetaData newSettings = new MSAnnotationMetaData();
		cloneSettings(newSettings);
		return newSettings;
	}

	@Override
	public void cloneSettings(MassSpecUISettings settings) {
		MSAnnotationMetaData newSettings = (MSAnnotationMetaData) settings;
		newSettings.setDescription(this.getDescription());
		newSettings.setAnnotationId(this.getAnnotationId());
		newSettings.setVersion(this.getVersion());
		newSettings.setDescription(this.getDescription());
		super.cloneSettings(settings);
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( ! (obj instanceof MSAnnotationMetaData) )
			return false;

		MSAnnotationMetaData castObj = (MSAnnotationMetaData) obj;
		boolean bRes = getDescription() != null && getDescription().equals( castObj.getDescription() );
		bRes &= getAnnotationId() != null && getAnnotationId().equals( castObj.getAnnotationId() );
		return bRes;
	}


}
