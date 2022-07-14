package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecScansView;

public class MSAnnotationScansView extends MassSpecScansView {

	private static final Logger logger = Logger.getLogger(MSAnnotationScansView.class);
	public static final String VIEW_ID = "plugin.ms.annotation.views.MSAnnotationScansView"; //$NON-NLS-1$
	
	@Inject
	public MSAnnotationScansView(Entry entry, Property msEntityProperty,
			@Named(MassSpecMultiPageViewer.MIN_MS_LEVEL_CONTEXT) int iMinMSLevel) {
		super(entry, msEntityProperty, iMinMSLevel);
	}
	
	@Override
	public String toString() {
		return "MSAnnotationPeaksView (" + entry + ")";
	}
	
	@Override
	protected void initResultsView( Composite parent ) throws Exception {
		this.parent = parent.getParent().getParent();    //CTabFolder
		compositeTop = new Composite(parent, SWT.BORDER);
		compositeTop.setLayout(new GridLayout(1,false));

		try {
			resultsComposite = getNewResultsComposite(compositeTop, SWT.NONE);
			( (MSAnnotationResultsComposite) resultsComposite).createPartControl(this.compositeTop, this, this.entityProperty, this.dataProcessor, FillTypes.Scans);
			resultsComposite.setLayout(new FillLayout());
			this.viewBase = resultsComposite.getBaseView();
		} catch( Exception e ) {
			viewBase = null;
			resultsComposite = null;
			logger.error("Error in MSAnnotationScansView: initResultsView");
			throw new Exception(e.getMessage());
		}		
	}
	

	@Override
	protected MSAnnotationResultsComposite getNewResultsComposite( Composite composite, int style ) {
		return new MSAnnotationResultsComposite(composite, style);
	}
		
	@Override
	protected TableDataProcessor getNewTableDataProcessor( Entry entry, Property entityProperty) {
		MSAnnotationTableDataProcessor proc = new MSAnnotationTableDataProcessor(
				entry, entityProperty, 
				FillTypes.Scans, getMinMSLevel() );
		proc.initializeTableDataObject(entityProperty);
//		proc.readDataFromFile();
		return proc;
	}

	@Override
	protected TableDataProcessor getNewTableDataProcessor(Property entityProperty) {		
		MSAnnotationMultiPageViewer parentViewer = MSAnnotationMultiPageViewer.getActiveViewerForEntry(context, getEntry().getParent());
		if( parentViewer == null || parentViewer.getScansView() == null ) {
			return null;
		}
		TableDataProcessor parentProc = parentViewer.getScansView().getTableDataProcessor();
//		if( parentProc == null ) 
//			return null;
//		if ( ! parentProc.getSourceProperty().equals(entityProperty) ) {
//			return null;
//		}
		MSAnnotationTableDataProcessor proc = new MSAnnotationTableDataProcessor(parentProc, entityProperty, 
				FillTypes.Scans, getMinMSLevel());
		proc.setEntry(getEntry());
		proc.initializeTableDataObject(entityProperty);
		return proc;		
	}	
}
