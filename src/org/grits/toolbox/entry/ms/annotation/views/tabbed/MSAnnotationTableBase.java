package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import java.io.File;

import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.editor.EntryEditorPart;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.display.control.table.tablecore.GRITSTable;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecSpectraView.MSSpectraViewerSashForm;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecTableBase;


public class MSAnnotationTableBase extends MassSpecTableBase {		
	public MSAnnotationTableBase() {
		super();
	}

	public MSAnnotationTableBase( Composite parent, EntryEditorPart parentEditor, 
			Property entityProperty, TableDataProcessor xmlExtractor, FillTypes fillType ) throws Exception {
		super(parent, parentEditor, entityProperty, xmlExtractor, fillType);
	}

	@Override
	public void initializeTable() throws Exception {
		this.natTable = (MSAnnotationTable) getNewSimianTable(this, dataProcessor);				
		//this.natTable.setMzXMLPathName( ( (MassSpecProperty) ( (MassSpecEntityProperty) getEntityProperty()).getMassSpecParentProperty() ).getFullyQualifiedMzXMLFileName(this.parentEditor.getEntry()));
		this.natTable.setMzXMLPathName(((MassSpecTableDataProcessor) dataProcessor).getMSPath() + File.separator +
				((MassSpecTableDataProcessor) dataProcessor).getMSSourceFile().getName());
		this.natTable.loadData();
		this.natTable.createMainTable();
	}

	@Override
	public GRITSTable getNewSimianTable( MassSpecTableBase _viewBase, TableDataProcessor _extractor ) throws Exception {
		return new MSAnnotationTable((MSAnnotationTableBase) _viewBase, _extractor);		
	}	

	protected static void propigateSharedCheckboxChanges( Object view, Entry parentEntry  ) {
		if ( view instanceof MSAnnotationMultiPageViewer ) {
			MSAnnotationMultiPageViewer overview = (MSAnnotationMultiPageViewer) view;
			if( overview.getEntry().getParent() != null && overview.getEntry().getParent().equals(parentEntry) ) {
				for( int j = 0; j < overview.getPageCount(); j++) {
					Object obj = overview.getPageItem(j);
					propigateSharedCheckboxChanges(obj, parentEntry);
				}
			}
		} else if( view instanceof MSAnnotationDetails ) {
			MSAnnotationDetails ad = (MSAnnotationDetails) view;
			if( ad.getEntry().getParent() != null && ad.getEntry().getParent().equals(parentEntry) && ad.getPeaksViews() != null ) {
				MSAnnotationEntityScroller entityScroller = (MSAnnotationEntityScroller) ad.getEntityScroller();
				entityScroller.reDrawLabel();
			}
		} else if( view instanceof MSSpectraViewerSashForm ) {
			MSSpectraViewerSashForm sv = (MSSpectraViewerSashForm) view;
			if( sv.getMSSpectraView().getEntry().getParent() != null && 
					sv.getMSSpectraView().getEntry().getParent().equals(parentEntry) ) {
				MSAnnotationSpectraControlPanelView cp = (MSAnnotationSpectraControlPanelView) sv.getMSSpectraView().getControlPanel();
				cp.updateView();
			}
		}
	}

}
