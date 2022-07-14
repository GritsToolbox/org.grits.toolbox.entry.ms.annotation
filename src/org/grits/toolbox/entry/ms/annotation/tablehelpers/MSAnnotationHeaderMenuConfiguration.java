package org.grits.toolbox.entry.ms.annotation.tablehelpers;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;

import org.grits.toolbox.display.control.table.tablecore.GRITSHeaderMenuConfiguration;

/**
 * Builds the row pop-up menu to provide options for showing/hiding rows.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * @see MSAnnotationMenuItemProviders
 */
public class MSAnnotationHeaderMenuConfiguration extends GRITSHeaderMenuConfiguration {	
	public MSAnnotationHeaderMenuConfiguration(NatTable natTable) {
		super(natTable);
	}

	@Override
	protected PopupMenuBuilder createRowHeaderMenu(NatTable natTable) {
		PopupMenuBuilder pmb = new PopupMenuBuilder(natTable);
		pmb.withAutoResizeSelectedRowsMenuItem();
		pmb.withSeparator();
		pmb.withMenuItemProvider(MSAnnotationMenuItemProviders.showAllRowMenuItemProvider());
		pmb.withMenuItemProvider(MSAnnotationMenuItemProviders.hideRowMenuItemProvider());
		pmb.withSeparator();
		pmb.withMenuItemProvider(MSAnnotationMenuItemProviders.showNoSelectionMenuItemProvider());
		pmb.withMenuItemProvider(MSAnnotationMenuItemProviders.hideNoSelectionMenuItemProvider());
		return pmb;
								
	}
}
