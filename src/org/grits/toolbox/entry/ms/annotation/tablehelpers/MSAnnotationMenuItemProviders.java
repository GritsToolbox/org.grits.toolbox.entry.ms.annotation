package org.grits.toolbox.entry.ms.annotation.tablehelpers;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.MenuItemProviders;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Adds items to the pop-up menu of the row header (left-hand side of row). 
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class MSAnnotationMenuItemProviders extends MenuItemProviders {	

	public static IMenuItemProvider hideRowMenuItemProvider() {
		return hideRowMenuItemProvider("Hide unannotated rows"); //$NON-NLS-1$
	}

	public static IMenuItemProvider hideRowMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {				
				MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
				menuItem.setText(menuLabel);
				menuItem.setEnabled(true);

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						MSAnnotationTable msTable = (MSAnnotationTable) natTable;
						msTable.hideUnannotatedRows();
						msTable.updatePreferenceSettingsFromCurrentView();
						msTable.getPreference().writePreference();		

					}
				});
			}
		};
	}
	
	public static IMenuItemProvider hideNoSelectionMenuItemProvider() {
		return hideNoSelectionMenuItemProvider("Hide rows with no selection"); //$NON-NLS-1$
	}
	
	public static IMenuItemProvider hideNoSelectionMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {				
				MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
				menuItem.setText(menuLabel);
				menuItem.setEnabled(true);

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						MSAnnotationTable msTable = (MSAnnotationTable) natTable;
						msTable.hideInvisibleRows();
						msTable.updatePreferenceSettingsFromCurrentView();
						msTable.getPreference().writePreference();		

					}
				});
			}
		};
	}
	public static IMenuItemProvider showNoSelectionMenuItemProvider() {
		return showNoSelectionMenuItemProvider("Show rows with no selection"); //$NON-NLS-1$
	}
	
	public static IMenuItemProvider showNoSelectionMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, Menu popupMenu) {
				MenuItem showAllColumns = new MenuItem(popupMenu, SWT.PUSH);
				showAllColumns.setText(menuLabel);
				showAllColumns.setEnabled(true);

				showAllColumns.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						MSAnnotationTable msTable = (MSAnnotationTable) natTable;
						msTable.showInvisibleRows();
						msTable.updatePreferenceSettingsFromCurrentView();
						msTable.getPreference().writePreference();		
					}
				});
			}
		};
	}

	public static IMenuItemProvider showAllRowMenuItemProvider() {
		return showAllRowMenuItemProvider("Show unannotated rows"); //$NON-NLS-1$
	}

	public static IMenuItemProvider showAllRowMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, Menu popupMenu) {
				MenuItem showAllColumns = new MenuItem(popupMenu, SWT.PUSH);
				showAllColumns.setText(menuLabel);
				showAllColumns.setEnabled(true);

				showAllColumns.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						MSAnnotationTable msTable = (MSAnnotationTable) natTable;
						msTable.showUnannotatedRows();
						msTable.updatePreferenceSettingsFromCurrentView();
						msTable.getPreference().writePreference();		
					}
				});
			}
		};
	}
	
	public static IMenuItemProvider lockSelectionMenuItemProvider() {
		return lockSelectionMenuItemProvider("Lock Selection");
	}

	private static IMenuItemProvider lockSelectionMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, Menu popupMenu) {
				MenuItem lockSelection = new MenuItem(popupMenu, SWT.PUSH);
				lockSelection.setText(menuLabel);
				lockSelection.setEnabled(true);
				
				lockSelection.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						MSAnnotationTable msTable = (MSAnnotationTable) natTable;
						msTable.lockSelection();
					}
				});
			}
		};
	}
	public static IMenuItemProvider unlockSelectionMenuItemProvider() {
		return unlockSelectionMenuItemProvider("Unlock Selection");
	}

	private static IMenuItemProvider unlockSelectionMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, Menu popupMenu) {
				MenuItem unlockSelection = new MenuItem(popupMenu, SWT.PUSH);
				unlockSelection.setText(menuLabel);
				unlockSelection.setEnabled(true);
				
				unlockSelection.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						MSAnnotationTable msTable = (MSAnnotationTable) natTable;
						msTable.unlockSelection();
					}
				});
			}
		};
	}
	
}	