package org.grits.toolbox.entry.ms.annotation.property;

import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;

import org.grits.toolbox.entry.ms.property.MassSpecEntityProperty;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.MassSpecUISettings;

public class MSAnnotationEntityProperty extends MassSpecEntityProperty {
	
	public static final String TYPE = MSAnnotationEntityProperty.class.getName();
	protected Integer annotationId = null;
	protected String featureId = null;
	protected String parentFeatureId = null;
	private MSAnnotationProperty parentProperty = null;
	
	public MSAnnotationEntityProperty(MassSpecProperty msParentProperty, MSAnnotationProperty annotParentProperty)
	{
		super(msParentProperty);
		this.parentProperty = annotParentProperty;
	}
			
	@Override
	public boolean equals(Object obj) {
		if ( ! (obj instanceof MSAnnotationEntityProperty) )
			return false;
		
		MSAnnotationEntityProperty castObj = (MSAnnotationEntityProperty) obj;
		boolean bRes = getAnnotationId() != null && getAnnotationId().equals( castObj.getAnnotationId() );
		bRes &= getFeatureId() != null && getFeatureId().equals( castObj.getFeatureId() );
		bRes &= getParentFeatureId() != null && getParentFeatureId().equals( castObj.getParentFeatureId() );
		return bRes && super.equals(castObj);
	}
		
	@Override
	public Property getParentProperty() {
		return parentProperty;
	}	
	
	public MSAnnotationProperty getMSAnnotationParentProperty() {
		return parentProperty;
	}
	
	public void setMSAnnotationParentProperty(MSAnnotationProperty parentProperty) {
		this.parentProperty = parentProperty;
	}
	
	public String getParentFeatureId() {
		return parentFeatureId;
	}
	public void setParentFeatureId(String parentFeatureId) {
		this.parentFeatureId = parentFeatureId;
	}
	
	public String getFeatureId() {
		return featureId;
	}
	
	public void setFeatureId(String featureId) {
		this.featureId = featureId;
	}
	
	public Integer getAnnotationId() {
		return annotationId;
	}
	
	public void setAnnotationId(Integer annotationId) {
		this.annotationId = annotationId;
	}	
	
	public static Entry getTableCompatibleEntry( Entry parentEntry ) {
		Entry newEntry = MassSpecEntityProperty.getTableCompatibleEntry(parentEntry);
				
		Entry msAnnotEntry = MSAnnotationProperty.getFirstAnnotEntry(parentEntry);
		MSAnnotationProperty msAnnotProp = null;
		MSAnnotationEntityProperty msAnnotEntityProp = null;
		if( msAnnotEntry != null ) {
			msAnnotProp = (MSAnnotationProperty) msAnnotEntry.getProperty();
			MassSpecEntityProperty msEntityProp = (MassSpecEntityProperty) newEntry.getProperty();
			msAnnotEntityProp = new MSAnnotationEntityProperty((MassSpecProperty) msEntityProp.getMassSpecParentProperty(), msAnnotProp);
			newEntry.setProperty(msAnnotEntityProp);
			newEntry.setDisplayName(parentEntry.getDisplayName());
		} 		
		return newEntry;
	}
	
	@Override
	public Object clone() {
		MSAnnotationEntityProperty newProp = new MSAnnotationEntityProperty(this.getMassSpecParentProperty(), this.getMSAnnotationParentProperty());
		newProp.setDescription(this.getDescription());
		newProp.setId(this.getId());
		newProp.setAnnotationId(this.getAnnotationId());
		newProp.setScanNum(this.getScanNum());
		newProp.setMsLevel(this.getMsLevel());
		newProp.setParentScanNum(this.getParentScanNum());
		newProp.setFeatureId(this.getFeatureId());
		newProp.setParentFeatureId(this.parentFeatureId);
		return newProp;
	}
	
	@Override
	public String getType() {
		return MSAnnotationEntityProperty.TYPE;
	}
	
	@Override
	public MassSpecUISettings getMassSpecUISettings() {
		return getMSAnnotationParentProperty().getMSAnnotationMetaData();
	}

}
