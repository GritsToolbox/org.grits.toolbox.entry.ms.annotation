package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.editor.EntryEditorPart;

import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecResultsComposite;

public class MSAnnotationResultsComposite extends MassSpecResultsComposite {
	public MSAnnotationResultsComposite(Composite parent, int style) {
		super(parent, style);
	}
	
	public void createPartControl( Composite parent, EntryEditorPart parentEditor, 
			Property entityProperty, TableDataProcessor dataProcessor, FillTypes fillType) throws Exception {
		this.baseView = new MSAnnotationTableBase(parent, parentEditor, entityProperty, dataProcessor, fillType);		
		this.baseView.initializeTable();
		this.baseView.layout();
	}
}
