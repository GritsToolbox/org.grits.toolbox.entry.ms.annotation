package org.grits.toolbox.entry.ms.annotation.tablehelpers;

import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.Feature;

/**
 * Essentially a complex data structure to use by the event broker to notify
 * appropriate classes that a particular MSAnnotationTable has changed its selections.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * @see MSAnnotationTable
 * @see IEventBroker
 * @see IGritsDataModelService
 */
public class MSAnnotationTableDataChangedMessage {
	protected MSAnnotationTable parentTable = null;
	protected Annotation annotation = null;
	protected Integer iPeakId = null;
	protected Feature feature = null;
	
	public MSAnnotationTableDataChangedMessage(MSAnnotationTable parentTable, Integer iPeakId,
			Feature feature, Annotation annotation ) {
		this.parentTable = parentTable;
		this.iPeakId = iPeakId;
		this.annotation = annotation;
		this.feature = feature;
	}
	
	public Annotation getAnnotation() {
		return annotation;
	}
	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}
	
	public Feature getFeature() {
		return feature;
	}
	public void setFeature(Feature feature) {
		this.feature = feature;
	}
	
	public Integer getPeakId() {
		return iPeakId;
	}
	public void setPeakId(Integer iPeakId) {
		this.iPeakId = iPeakId;
	}
	
	public MSAnnotationTable getParentTable() {
		return parentTable;
	}
	
	public void setParentTable(MSAnnotationTable parentTable) {
		this.parentTable = parentTable;
	}
	
}
