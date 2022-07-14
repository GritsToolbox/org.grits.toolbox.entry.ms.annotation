package org.grits.toolbox.entry.ms.annotation.adaptor;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DatabaseDialog extends TitleAreaDialog {

	String name;
	String description;
	String version = "1.0";
	private Text dbName;
	private Text dbDescription;
	private Text dbVersion;
	
	protected DatabaseDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Database Information");
		setMessage("Please enter the details for the database");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		//find the center of a main monitor
		Monitor primary = getShell().getDisplay().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = getShell().getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		getShell().setLocation(x, y);
		
		GridLayout gd = new GridLayout (2, false);
		gd.marginRight = 8;
		gd.verticalSpacing = 20;
		gd.horizontalSpacing = 8;
		container.setLayout(gd);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label dbNameLabel = new Label(container, SWT.LEFT);
		dbNameLabel.setText("Database Name");
		
		dbName = new Text(container, SWT.BORDER);
		
		Label dbDescriptionLabel = new Label(container, SWT.LEFT);
		dbDescriptionLabel.setText("Description");
		
		dbDescription = new Text(container, SWT.BORDER);
		
		Label dbVersionLabel = new Label(container, SWT.LEFT);
		dbVersionLabel.setText("Database Version");
		
		dbVersion = new Text(container, SWT.BORDER);
		dbVersion.setText(version);
		
		return area;
	}
	
	@Override
	protected void okPressed() {
		
		if (dbName.getText().isEmpty())
			setErrorMessage("Database name cannot be blank");
		else {
			name = dbName.getText();
			description = dbDescription.getText();
			version = dbVersion.getText();
			super.okPressed();
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
}
