package org.grits.toolbox.entry.ms.annotation.views.tabbed.content;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.grits.toolbox.ms.om.data.IonSettings;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.ms.om.data.Molecule;
import org.grits.toolbox.ms.om.data.MoleculeSettings;

public class AnnotationSettingsTableComposite extends Composite {
	
	protected Method method;

	public AnnotationSettingsTableComposite(Composite parent, int style) {
		super(parent, style);
		GridLayout layout = new GridLayout(1, true);
	    layout.marginWidth = 2;
	    layout.marginHeight = 2;
	    this.setLayout(layout);
	}
	
	public void setMethod(Method method) {
		this.method = method;
	}
	
	/**
	 * adds a table for general settings
	 */
	public void createGeneralSettingsTable() {
		Label label = new Label(this, SWT.NONE);
		label.setText("Method Settings");
	    TableViewer settingsTableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
	    Table settingsTable = settingsTableViewer.getTable();
		GridData gd_table_2 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_table_2.heightHint = 150;
		settingsTable.setLayoutData(gd_table_2);
		settingsTable.setHeaderVisible(true);
		settingsTable.setLinesVisible(true);
		
		TableViewerColumn nameColumn = new TableViewerColumn(settingsTableViewer, SWT.NONE);
		TableColumn dColumn = nameColumn.getColumn();
		dColumn.setText("Setting");
		dColumn.setWidth(200);
		
		TableViewerColumn valueColumn = new TableViewerColumn(settingsTableViewer, SWT.NONE);
		valueColumn.getColumn().setText("Value");
		valueColumn.getColumn().setWidth(150);
		
		addData(settingsTable);
	}
	
	/**
	 * add the table items to the general settings table
	 * @param settingsTable table to add data into
	 */
	private void addData(Table settingsTable) {
		TableItem item = new TableItem(settingsTable, SWT.NONE);
		String msType =  this.method.getMsType() != null ?
				this.method.getMsType() : "";
		if (!msType.isEmpty())
			msType = Method.getMsLabelByType(msType);
		item.setText(new String[] {"MS Type", msType});
		item = new TableItem(settingsTable, SWT.NONE);
		item.setText(new String[] {"Mass Type", method.getMonoisotopic() ? "Monoisotopic" : "Average"});
		item = new TableItem(settingsTable, SWT.NONE);
		String sText = this.method.getAccuracy() != null ? this.method.getAccuracy().toString() : null;
		sText = sText != null ? sText + " " + (this.method.getAccuracyPpm() ? "ppm" : "Da") : "";
		item.setText(new String[] {"Precursor Accuracy", sText});
		item = new TableItem(settingsTable, SWT.NONE);
		sText = this.method.getAccuracy() != null ? this.method.getFragAccuracy().toString() : null;
		sText = sText != null ? sText + " " + (this.method.getFragAccuracyPpm() ? "ppm" : "Da") : "";
		item.setText(new String[] {"Fragment Accuracy", sText});
		item = new TableItem(settingsTable, SWT.NONE);
		item.setText(new String[] {"Trust Charge", this.method.getTrustMzCharge() != null ? (this.method.getTrustMzCharge() ? "Yes" : "No") : null});
		item = new TableItem(settingsTable, SWT.NONE);
		item.setText(new String[] {"Shift", this.method.getShift()  != null ? this.method.getShift().toString() : null});
		item = new TableItem(settingsTable, SWT.NONE);
		item.setText(new String[] {"Fragment Intensity Cutoff", this.method.getIntensityCutoff()  != null ? this.method.getIntensityCutoff().toString() : ""});
		item = new TableItem(settingsTable, SWT.NONE);
		item.setText(new String[] {"Precursor Intensity Cutoff", this.method.getPrecursorIntensityCutoff()  != null ? this.method.getPrecursorIntensityCutoff().toString() : ""});
		item = new TableItem(settingsTable, SWT.NONE);
		item.setText(new String[] {"Fragment Intensity Cutoff Type", this.method.getIntensityCutoffType()  != null ? this.method.getIntensityCutoffType().toString() : ""});
		item = new TableItem(settingsTable, SWT.NONE);
		item.setText(new String[] {"Precursor Intensity Cutoff Type", this.method.getPrecursorIntensityCutoffType()  != null ? this.method.getPrecursorIntensityCutoffType().toString() : ""});
		item = new TableItem(settingsTable, SWT.NONE);
		item.setText(new String[] {"Max Ion Count", this.method.getMaxIonCount()  != null ? this.method.getMaxIonCount().toString() : ""});
		item = new TableItem(settingsTable, SWT.NONE);
		item.setText(new String[] {"Max Ion Exchange Count", this.method.getMaxIonExchangeCount()  != null ? this.method.getMaxIonExchangeCount().toString() : ""});
	}

	/**
	 * add a table to list all the ions, if any
	 */
	public void createIonsTable() {
		Label label = new Label(this, SWT.NONE);
		label.setText("Ions");
		if (method.getIons() != null )//&& !method.getIons().isEmpty())
			createMoleculeTable(true, method.getIons());
	}

	/**
	 * add a table to list all the ion exchanges, if any
	 */
	public void createIonExchangesTable() {
		Label label = new Label(this, SWT.NONE);
		label.setText("Ion Exhanges");
		if (method.getIonExchanges() != null )//&& !method.getIonExchanges().isEmpty())
			createMoleculeTable(true, method.getIonExchanges());
	}
	
	/**
	 * add a table to list neutral exchanges, if any
	 */
	public void createNeutralLossTable() {
		Label label = new Label(this, SWT.NONE);
		label.setText("Neutral Loss");
		if (method.getNeutralLoss() != null )//&& !method.getNeutralLoss().isEmpty())
			createMoleculeTable(false, method.getNeutralLoss());
	}
	
	/**
	 * method to create a table for a list of "Molecule"s (ion, ion exchange or neutral loss)
	 * @param ion whether the molecule is an ion or an ion exchange
	 * @param input list of molecules to display in the table
	 */
	private void createMoleculeTable (boolean ion, List<?> input) {
	    TableViewer moleculeSettingsTableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
	    Table table = moleculeSettingsTableViewer.getTable();
		GridData gd_table_2 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_table_2.heightHint = 50;
		table.setLayoutData(gd_table_2);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableViewerColumn nameColumn = new TableViewerColumn(moleculeSettingsTableViewer, SWT.NONE);
		nameColumn.getColumn().setText("Name");
		nameColumn.getColumn().setWidth(100);
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Molecule) {
					return ((Molecule) element).getName();
				}
				return "";
			}
		});
		
		TableViewerColumn labelColumn = new TableViewerColumn(moleculeSettingsTableViewer, SWT.NONE);
		labelColumn.getColumn().setText("Label");
		labelColumn.getColumn().setWidth(100);
		labelColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Molecule) {
					return ((Molecule) element).getLabel();
				}
				return "";
			}
		});
		
		TableViewerColumn massColumn = new TableViewerColumn(moleculeSettingsTableViewer, SWT.NONE);
		massColumn.getColumn().setText("Mass");
		massColumn.getColumn().setWidth(70);
		massColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Molecule) {
					if (((Molecule) element).getMass() != null)
						return ((Molecule) element).getMass().toString();
				}
				return "";
			}
		});
		
		TableViewerColumn countColumn = new TableViewerColumn(moleculeSettingsTableViewer, SWT.NONE);
		countColumn.getColumn().setText("Count");
		countColumn.getColumn().setWidth(70);
		countColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IonSettings) {
					return ((IonSettings) element).getCounts().toString();
				} else if (element instanceof MoleculeSettings)
					return ((MoleculeSettings) element).getCount().toString();
				return "";
			}
		});
		
		if (ion) {
			TableViewerColumn chargeColumn = new TableViewerColumn(moleculeSettingsTableViewer, SWT.NONE);
			chargeColumn.getColumn().setText("Charge");
			chargeColumn.getColumn().setWidth(70);
			chargeColumn.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					if (element instanceof IonSettings) {
						return ((IonSettings) element).getCharge().toString();
					}
					return "";
				}
			});
			
			TableViewerColumn polarityColumn = new TableViewerColumn(moleculeSettingsTableViewer, SWT.NONE);
			polarityColumn.getColumn().setText("Polarity");
			polarityColumn.getColumn().setWidth(200);
			polarityColumn.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					if (element instanceof IonSettings) {
						return ((IonSettings) element).getPolarity() ? "+" : "-";
					}
					return "";
				}
			});
		}
		
		moleculeSettingsTableViewer.setContentProvider(new ArrayContentProvider());
		moleculeSettingsTableViewer.setInput(input);
		moleculeSettingsTableViewer.getTable().computeSize(SWT.DEFAULT, moleculeSettingsTableViewer.getTable().getItemHeight());
	}
	
	/**
	 * add a table to list analyte settings (fragments, database info etc.)
	 * needs to be overridden by subclasses, this generic one does not add any data
	 */
	public void createAnalyteSettingsTable() {
		// nothing for the generic 
	}
}
