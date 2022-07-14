package org.grits.toolbox.entry.ms.annotation.tablehelpers;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;

public class MSAnnotationSelectionTableHeaderMenuConfiguration extends AbstractUiBindingConfiguration {

	final Menu lockMenu;
	
	public MSAnnotationSelectionTableHeaderMenuConfiguration(NatTable natTable) {
		PopupMenuBuilder pmb = new PopupMenuBuilder(natTable);
		pmb.withMenuItemProvider(MSAnnotationMenuItemProviders.lockSelectionMenuItemProvider());
		pmb.withMenuItemProvider(MSAnnotationMenuItemProviders.unlockSelectionMenuItemProvider());
		this.lockMenu = pmb.build();
	}

	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerMouseDownBinding(
				new MouseEventMatcher(
				SWT.NONE,
				GridRegion.CORNER,
				MouseEventMatcher.RIGHT_BUTTON),
				new PopupMenuAction(this.lockMenu));
	}
}
