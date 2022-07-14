package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.widgets.Section;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.editor.ScrollableEntryEditorPart;
import org.grits.toolbox.datamodel.ms.tablemodel.MassSpecTableDataObject;
import org.grits.toolbox.entry.ms.annotation.dialog.MSAnnotationExternalQuantDialog;
import org.grits.toolbox.entry.ms.annotation.dialog.MSAnnotationStandardQuantApplyDialog;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.dialog.MassSpecPeakIntensityApplyDialog;
import org.grits.toolbox.entry.ms.preference.MassSpecPreference;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecStandardQuant;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecStandardQuantPeak;
import org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.ExternalQuantAlias;
import org.grits.toolbox.entry.ms.property.datamodel.ExternalQuantFileToAlias;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.grits.toolbox.entry.ms.property.datamodel.MassSpecUISettings;
import org.grits.toolbox.entry.ms.property.datamodel.QuantFilePeaksToCorrectedIntensities;
import org.grits.toolbox.entry.ms.property.datamodel.QuantFileToCorrectedPeaks;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;

public abstract class MSAnnotationQuantificationView extends ScrollableEntryEditorPart implements IPropertyChangeListener {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSAnnotationQuantificationView.class);
		
	private MPart part;
	private Composite parent;
	private Composite container;

	protected Color sectionColor;
	protected Color backgroundColor;

	private TableViewer extQuantTableViewer;

	private MassSpecUISettings entrySettings;
	protected MassSpecPreference localStandardQuant;
	protected MassSpecPreference entryStandardQuant;

	private TableViewer standardPeaksTable;

	private TreeViewer standardFilesTable;

	private Table peakIntensityTable;
	
	private static String[] columnHeaders = { "File Name", "Type", "m/z", "Corrected Intensity" };
	
	@Inject
	public MSAnnotationQuantificationView(Entry entry) {
		this.entry = entry;
	}
	
	@PostConstruct 
	public void postConstruct(MPart part) {
		this.setPart(part);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		final ScrolledComposite sc = new ScrolledComposite(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		final Composite c = new Composite(sc, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 10;
		layout.verticalSpacing = 10;
		layout.numColumns = 6;
		c.setLayout(layout);

		this.parent = parent.getParent().getParent();    //CTabFolder
		this.container = c;
		
		try {
			addOtherSettings();
		} catch( Exception e) {
			logger.error("Error adding property elements.", e);
		}
		sc.setContent(c);
		sc.setMinSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	protected void addOtherSettings () {
		sectionColor = new Color(Display.getCurrent(), 20, 199, 255);
		backgroundColor = Display.getCurrent().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND);
		// add external quantification
		createExternalQuantificationSection();
		// add standard quantification
		createStandardQuantificationSection();
		// add overwritten intensities
		createIntensitiesSection();
	}
	
	protected void createExternalQuantificationSection() {
		Section section = new Section(getContainer(), Section.TREE_NODE | Section.TITLE_BAR | Section.EXPANDED);
		section.setText("External Quantifications");
		
		section.setTitleBarBackground(sectionColor);
		section.setBackground(backgroundColor);
		section.setTitleBarForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE));
		
		GridData gridData = new GridData();
		gridData.horizontalSpan = 6;
		section.setLayoutData(gridData);
		
		Composite sectionComposite = new Composite(section, SWT.WRAP);
		sectionComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 6, 4));
		sectionComposite.setLayout(new GridLayout(3, false));
		sectionComposite.setBackground(backgroundColor);
		sectionComposite.setBackgroundMode(SWT.INHERIT_FORCE);
	
		List<MSPropertyDataFile> quantFiles = getQuantificationFiles();
		entrySettings = getEntrySettings();
		
		extQuantTableViewer = new TableViewer(sectionComposite);
		extQuantTableViewer.getTable().setHeaderVisible(true);
		extQuantTableViewer.getTable().setLinesVisible(true);
		GridData gd_table_2 = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gd_table_2.heightHint = 100;
		extQuantTableViewer.getTable().setLayoutData(gd_table_2);
		
		TableViewerColumn fileNameCol = new TableViewerColumn(extQuantTableViewer, SWT.NONE);
		fileNameCol.getColumn().setText("File");
		fileNameCol.getColumn().setWidth(250);
		fileNameCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof MSPropertyDataFile) {
					MSPropertyDataFile mspdf = (MSPropertyDataFile) element;
					String sFileName = MSPropertyDataFile.getFormattedName(mspdf);
					return sFileName;
				}
				return "";
			}
		});
		
		TableViewerColumn fileTypeCol = new TableViewerColumn(extQuantTableViewer, SWT.NONE);
		fileTypeCol.getColumn().setText("Type");
		fileTypeCol.getColumn().setWidth(100);
		fileTypeCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof MSPropertyDataFile) {
					return ((MSPropertyDataFile) element).getType();
				}
				return "";
			}
		});
		
		TableViewerColumn fileAliasCol = new TableViewerColumn(extQuantTableViewer, SWT.NONE);
		fileAliasCol.getColumn().setText("Alias");
		fileAliasCol.getColumn().setWidth(200);
		fileAliasCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof MSPropertyDataFile) {
					MSPropertyDataFile mspdf = (MSPropertyDataFile) element;
					String sExtQuantType = MassSpecUISettings.getExternalQuantType(mspdf);
					String sFilePath = mspdf.getName();
					ExternalQuantFileToAlias mAliases = entrySettings.getExternalQuantToAliasByQuantType(sExtQuantType);
					String sAlias = MSPropertyDataFile.getFormattedName(mspdf);
					if( mAliases != null && mAliases.getSourceDataFileNameToAlias().containsKey(sFilePath) ) {
						ExternalQuantAlias aliasInfo = mAliases.getSourceDataFileNameToAlias().get(sFilePath);
						sAlias = aliasInfo.getAlias();
					}
					return sAlias;
				}
				return "";
			}
		});
		
		TableViewerColumn fileInUseCol = new TableViewerColumn(extQuantTableViewer, SWT.NONE);
		fileInUseCol.getColumn().setText("Applied?");
		fileInUseCol.getColumn().setWidth(80);
		fileInUseCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof MSPropertyDataFile) {
					MSPropertyDataFile mspdf = (MSPropertyDataFile) element;
					String sExtQuantType = MassSpecUISettings.getExternalQuantType(mspdf);
					String sFilePath = mspdf.getName();
					ExternalQuantFileToAlias mAliases = entrySettings.getExternalQuantToAliasByQuantType(sExtQuantType);
					boolean isInUse = mAliases != null && mAliases.getSourceDataFileNameToAlias().containsKey(sFilePath);
					return isInUse ? "Yes" : "No";
				}
				return "N/A";
			}
		});
		
		extQuantTableViewer.setContentProvider(new ArrayContentProvider());
		extQuantTableViewer.setInput(quantFiles);
		
		createButton(sectionComposite);
		
		section.setClient(sectionComposite);
	}
	
	void createButton (Composite parent) {
		Button editButton = new Button(parent, SWT.PUSH);
		editButton.setText("Add/Modify External Quantifications");
		GridData gd = new GridData(SWT.RIGHT, SWT.FILL, false, true, 3, 1);
		if (this.entry.getParent() != null && this.entry.getParent().getProperty() instanceof MSAnnotationEntityProperty)
			editButton.setEnabled(true);
		else
			editButton.setEnabled(false);
		editButton.setLayoutData(gd);
		
		editButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {	
				MSAnnotationMultiPageViewer curView = getCurrentViewer();
				if (curView == null)
					return;
				openExternalQuantDialog (curView);
			}	

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	protected abstract void openExternalQuantDialog(MSAnnotationMultiPageViewer curView);
	protected abstract void openStandardQuantDialog(MSAnnotationMultiPageViewer curView);
	protected abstract void initializeStandardQuantifications();

	/**
	 * Returns the list of external quantification files from the source MS entry  
	 * @return the list of available external quantification files in MS entry
	 */
	protected List<MSPropertyDataFile> getQuantificationFiles() {
		MassSpecProperty msp = (MassSpecProperty) ((MSAnnotationEntityProperty) this.entry.getProperty()).getMassSpecParentProperty();
		MassSpecUISettings entrySettings = msp.getMassSpecMetaData();
		return entrySettings.getQuantificationFiles();
	}
	
	/**
	 * Returns the list of files from the source MS entry that the user may use for internal standard quant
	 * @return the list of available files in MS entry (external quant and annotation)
	 */
	protected List<MSPropertyDataFile> getStandardQuantificationFiles() {
		MassSpecProperty msp = (MassSpecProperty) ((MSAnnotationEntityProperty) this.entry.getProperty()).getMassSpecParentProperty();
		MassSpecUISettings entrySettings = msp.getMassSpecMetaData();
		List<MSPropertyDataFile> fileList = new ArrayList<>();
		fileList.addAll( entrySettings.getAnnotationFiles() );
		fileList.addAll( entrySettings.getQuantificationFiles() );
		return fileList;
	}
	
	/**
	 * Returns the MassSpecUISettings object to be used to list which external quantification files are associated with the entry.
	 * @return the MassSpecUISettings for the current entry
	 */
	protected MassSpecUISettings getEntrySettings() {
		MSAnnotationEntityProperty msep = (MSAnnotationEntityProperty) entry.getProperty();
		MSAnnotationProperty msap = msep.getMSAnnotationParentProperty();
		MassSpecUISettings entrySettings = msap.getMSAnnotationMetaData();
		return entrySettings;
	}

	protected void createStandardQuantificationSection() {
		Section section = new Section(getContainer(), Section.TREE_NODE | Section.TITLE_BAR | Section.EXPANDED);
		section.setText("Standard Quantifications");
		
		section.setTitleBarBackground(sectionColor);
		section.setBackground(backgroundColor);
		section.setTitleBarForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE));
		
		GridData gridData = new GridData();
		gridData.horizontalSpan = 6;
		section.setLayoutData(gridData);
		
		Composite sectionComposite = new Composite(section, SWT.WRAP);
		sectionComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 6, 4));
		sectionComposite.setLayout(new GridLayout(6, false));
		sectionComposite.setBackground(backgroundColor);
		sectionComposite.setBackgroundMode(SWT.INHERIT_FORCE);
		
		initializeStandardQuantifications();
		
		List<MassSpecStandardQuantPeak> peakList = createPeakList ();
		
		standardPeaksTable = new TableViewer(sectionComposite);
		standardPeaksTable.getTable().setLinesVisible(true);
		standardPeaksTable.getTable().setHeaderVisible(true);
		GridData gd_table_2 = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_table_2.heightHint = 100;
		standardPeaksTable.getTable().setLayoutData(gd_table_2);
		
		TableViewerColumn stdQuantName = new TableViewerColumn (standardPeaksTable, SWT.NONE);
		stdQuantName.getColumn().setText("Standard Quantification");
		stdQuantName.getColumn().setWidth(150);
		stdQuantName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof MassSpecStandardQuantPeak) {
					return findStandardQuantNameForPeak ((MassSpecStandardQuantPeak)element);
				}
				return "";
			}
		});
		TableViewerColumn peakMz = new TableViewerColumn(standardPeaksTable, SWT.NONE);
		peakMz.getColumn().setText("Peak M/z");
		peakMz.getColumn().setWidth(100);
		peakMz.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof MassSpecStandardQuantPeak) {
					return ((MassSpecStandardQuantPeak) element).getPeakMz() == null ? 
							"" : ((MassSpecStandardQuantPeak) element).getPeakMz().toString();
				}
				return "";
			}
		});
			
		TableViewerColumn peakLabel = new TableViewerColumn(standardPeaksTable, SWT.NONE);
		peakLabel.getColumn().setText("Peak Label");
		peakLabel.getColumn().setWidth(100);
		peakLabel.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof MassSpecStandardQuantPeak) {
					return ((MassSpecStandardQuantPeak) element).getPeakLabel();
				}
				return "";
			}
		});
		TableViewerColumn msLabel = new TableViewerColumn(standardPeaksTable, SWT.NONE);
		msLabel.getColumn().setText("MS Level");
		msLabel.getColumn().setWidth(100);
		msLabel.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof MassSpecStandardQuantPeak) {
					return ((MassSpecStandardQuantPeak) element).getMSLevel() == null ? 
							"" : ((MassSpecStandardQuantPeak) element).getMSLevel().toString();
				}
				return "";
			}
		});
		standardPeaksTable.setContentProvider(new ArrayContentProvider());
		standardPeaksTable.setInput(peakList);
		
		standardFilesTable = new TreeViewer(sectionComposite);
		standardFilesTable.getTree().setLinesVisible(true);
		standardFilesTable.getTree().setHeaderVisible(true);
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
		gd_table.heightHint = 100;
		standardFilesTable.getTree().setLayoutData(gd_table);
		
		TreeViewerColumn stdQuantName2 = new TreeViewerColumn (standardFilesTable, SWT.NONE);
		stdQuantName2.getColumn().setText("Standard Quantification");
		stdQuantName2.getColumn().setWidth(150);
		stdQuantName2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Map.Entry) {
					return (String) ((Map.Entry) element).getKey();
				}
				return "";
			}
		});
		
		TreeViewerColumn fileName = new TreeViewerColumn(standardFilesTable, SWT.NONE);
		fileName.getColumn().setText("Quantification File");
		fileName.getColumn().setWidth(350);
		fileName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof MSPropertyDataFile) {
					return MSPropertyDataFile.getFormattedName((MSPropertyDataFile) element);
				}
				return "";
			}
		});
		
		standardFilesTable.setContentProvider(new ITreeContentProvider() {
			@Override
			public boolean hasChildren(Object element) {
				if (element instanceof Map.Entry) {
					if ((List)((Map.Entry) element).getValue() == null )
						return false;
					else
						return ((List)((Map.Entry) element).getValue()).size() == 0 ? false : true;
				}
				return false;
			}
			
			@Override
			public Object getParent(Object element) {
				return null;
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Set)
					return ((Set)inputElement).toArray();
				return null;
			}
			
			@Override
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof Set)
					((Set)parentElement).toArray();
				else if (parentElement instanceof Map.Entry)
					return ((List)((Map.Entry) parentElement).getValue()).toArray();
				return null;
			}

			@Override
			public void dispose() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub
				
			}
		});
		
		initializeStandardFilesTable();
		
		createStandardButton(sectionComposite);
		section.setClient(sectionComposite);
	}
	
	private void initializeStandardFilesTable() {
		List<MSPropertyDataFile> allFiles = getStandardQuantificationFiles();
		HashMap<String, List<MSPropertyDataFile>> usedFiles = getFilesWithStandardQuant(allFiles);
		standardFilesTable.setInput(usedFiles.entrySet());
		standardFilesTable.expandAll();
	}

	private HashMap<String, List<MSPropertyDataFile>> getFilesWithStandardQuant(List<MSPropertyDataFile> allFiles) {
		HashMap<String, List<MSPropertyDataFile>> map = new HashMap<>();
		for (MassSpecStandardQuant std: entryStandardQuant.getStandardQuant()) {
			List<MSPropertyDataFile> files = map.get(std.getStandardQuantName());
			if (files == null) {
				files = new ArrayList<>();
			}
			for (MSPropertyDataFile f: allFiles) {
				ExternalQuantFileToAlias mAliases = entrySettings.getInternalQuantFileToAlias(std.getStandardQuantName(), f);					
				if( mAliases != null && mAliases.getSourceDataFileNameToAlias().containsKey(f.getName()) ) {
					if (!files.contains(f))
						files.add(f);
				}
			}
			map.put(std.getStandardQuantName(), files);
		}
		
		for (MassSpecStandardQuant std: localStandardQuant.getStandardQuant()) {
			List<MSPropertyDataFile> files = map.get(std.getStandardQuantName());
			if (files == null) {
				files = new ArrayList<>();
			}
			for (MSPropertyDataFile f: allFiles) {
				ExternalQuantFileToAlias mAliases = entrySettings.getInternalQuantFileToAlias(std.getStandardQuantName(), f);					
				if( mAliases != null && mAliases.getSourceDataFileNameToAlias().containsKey(f.getName()) ) {
					if (!files.contains(f))
						files.add(f);
				}
			}
			map.put(std.getStandardQuantName(), files);
		}
		return map;
	}

	protected String findStandardQuantNameForPeak(MassSpecStandardQuantPeak peak) {
		String name = "";
		for (MassSpecStandardQuant std: localStandardQuant.getStandardQuant()) {
			if (std.getStandardQuantPeaks().values().contains(peak)) {
				name = std.getStandardQuantName();
				break;
			}
		}
		if (name.isEmpty()) {
			for (MassSpecStandardQuant std: entryStandardQuant.getStandardQuant()) {
				if (std.getStandardQuantPeaks().values().contains(peak)) {
					name = std.getStandardQuantName();
					break;
				}
			}
		}
		
		return name;
	}

	private List<MassSpecStandardQuantPeak> createPeakList() {
		List<MassSpecStandardQuantPeak> peakList = new ArrayList<>();
		
		for (MassSpecStandardQuant std: localStandardQuant.getStandardQuant()) {
			for (MassSpecStandardQuantPeak peak: std.getStandardQuantPeaks().values()) {
				if (!peakList.contains(peak))
					peakList.add(peak);
			}
		}
		
		for (MassSpecStandardQuant std: entryStandardQuant.getStandardQuant()) {
			for (MassSpecStandardQuantPeak peak: std.getStandardQuantPeaks().values()) {
				if (!peakList.contains(peak))
					peakList.add(peak);
			}
		}
		
		return peakList;
	}

	void createStandardButton (Composite parent) {
		Button editButton = new Button(parent, SWT.PUSH);
		editButton.setText("Add/Modify Standard Quantifications");
		GridData gd = new GridData(SWT.RIGHT, SWT.FILL, false, true, 6, 1);
		if (this.entry.getParent() != null && this.entry.getParent().getProperty() instanceof MSAnnotationEntityProperty)
			editButton.setEnabled(true);
		else
			editButton.setEnabled(false);
		editButton.setLayoutData(gd);
		
		editButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {	
				MSAnnotationMultiPageViewer curView = getCurrentViewer();
				if (curView == null)
					return;
				openStandardQuantDialog(curView);
			}	

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	protected void createIntensitiesSection() {
		Section section = new Section(getContainer(), Section.TREE_NODE | Section.TITLE_BAR | Section.EXPANDED);
		section.setText("Overridden Intensities");
		
		section.setTitleBarBackground(sectionColor);
		section.setBackground(backgroundColor);
		section.setTitleBarForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE));
		
		GridData gridData = new GridData();
		gridData.horizontalSpan = 6;
		section.setLayoutData(gridData);
		
		Composite sectionComposite = new Composite(section, SWT.WRAP);
		sectionComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 6, 4));
		sectionComposite.setLayout(new GridLayout(3, false));
		sectionComposite.setBackground(backgroundColor);
		sectionComposite.setBackgroundMode(SWT.INHERIT_FORCE);
		
		peakIntensityTable = new Table(sectionComposite, SWT.NONE);
		peakIntensityTable.setLinesVisible(true);
		peakIntensityTable.setHeaderVisible(true);
		GridData gd_table_2 = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gd_table_2.heightHint = 100;
		peakIntensityTable.setLayoutData(gd_table_2);
		
		int i=0;
		for (String string : columnHeaders) {
			TableColumn col = new TableColumn(peakIntensityTable, SWT.NONE);
			col.setText(string);
			if (i ==0) col.setWidth(250);
			else col.setWidth(100);
			i++;
		}
		initializeCorrectedIntensities();
		
		createIntensityButton(sectionComposite);
		section.setClient(sectionComposite);
	}
	
	private void initializeCorrectedIntensities() {
		MassSpecUISettings entrySettings = getSourceMassSpecEntrySettings();
		if (entrySettings.getQuantFileToCorrectedPeaks() != null) {
			for (String file: entrySettings.getQuantFileToCorrectedPeaks().keySet()) {
				QuantFileToCorrectedPeaks correctedPeak = entrySettings.getQuantFileToCorrectedPeaks().get(file);
				for (String type: correctedPeak.getPeakTypeToMZs().keySet()) {
					QuantFilePeaksToCorrectedIntensities correctedIntensity = correctedPeak.getPeakTypeToMZs().get(type);
					for (Double peakMz: correctedIntensity.getPeakMzToIntensity().keySet()) {
						Double intensity = correctedIntensity.getPeakMzToIntensity().get(peakMz);
						TableItem item = new TableItem(peakIntensityTable, SWT.NONE);
						item.setText(0, file);
						item.setText(1, type);
						item.setText(2, peakMz.toString());
						item.setText(3, intensity.toString());
					}
				}
			}
		}
	}
	
	protected void openIntensityDialog(MSAnnotationMultiPageViewer curView) {
		if( MassSpecMultiPageViewer.massSpecPeakIntensityApplyDialog == null || 
				MassSpecMultiPageViewer.massSpecPeakIntensityApplyDialog.getShell() == null || 
				MassSpecMultiPageViewer.massSpecPeakIntensityApplyDialog.getShell().isDisposed() ) {
			MassSpecTableDataObject tdo = ((MassSpecTableDataProcessor) curView.getScansView().getTableDataProcessor()).getSimianTableDataObject();
			MassSpecMultiPageViewer.massSpecPeakIntensityApplyDialog  = 
					new MassSpecPeakIntensityApplyDialog(Display.getCurrent().getActiveShell(), curView, tdo);
			MassSpecMultiPageViewer.massSpecPeakIntensityApplyDialog.addListener(curView);
			MassSpecMultiPageViewer.massSpecPeakIntensityApplyDialog.addListener(this);
			MassSpecMultiPageViewer.massSpecPeakIntensityApplyDialog.open();				
		} else {
			MassSpecMultiPageViewer.massSpecPeakIntensityApplyDialog.getShell().forceActive();
		}
	}
	
	void createIntensityButton (Composite parent) {
		Button editButton = new Button(parent, SWT.PUSH);
		editButton.setText("Edit Corrected Intensities");
		GridData gd = new GridData(SWT.RIGHT, SWT.FILL, false, true, 6, 1);
		if (this.entry.getParent() != null && this.entry.getParent().getProperty() instanceof MSAnnotationEntityProperty)
			editButton.setEnabled(true);
		else
			editButton.setEnabled(false);
		editButton.setLayoutData(gd);
		
		editButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {	
				MSAnnotationMultiPageViewer curView = getCurrentViewer();
				if (curView == null)
					return;
				openIntensityDialog(curView);
			}	

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	/**
	 * Returns the MassSpecUISettings object to be used to list which files are associated with the entry.
	 * @return the MassSpecUISettings for the current entry
	 */
	public MassSpecUISettings getSourceMassSpecEntrySettings() {
		MassSpecProperty msp = (MassSpecProperty) ((MSAnnotationEntityProperty) this.entry.getProperty()).getMassSpecParentProperty();
		MassSpecUISettings entrySettings = msp.getMassSpecMetaData();
		return entrySettings;
	}
	
	@Override
	protected Composite getParent() {
		return this.parent;
	}
	
	@Override
	protected void initializeComponents() {
		// nothing to do
	}

	@Override
	protected void updateProjectProperty() {
		// nothing to save
	}

	@Override
	protected void savePreference() {
		// nothing to save
	}
	
	public void setPart(MPart part) {
		this.part = part;
	}
	
	public MPart getPart() {
		return part;
	}
	
	public Composite getContainer() {
		return container;
	}

	protected MSAnnotationMultiPageViewer getCurrentViewer() {
		return MSAnnotationMultiPageViewer.getActiveViewerForEntry(MSAnnotationQuantificationView.this.getPart().getContext(), this.entry);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// refresh table
		if (event.getSource() instanceof MSAnnotationExternalQuantDialog) {
			List<MSPropertyDataFile> quantFiles = getQuantificationFiles();
			entrySettings = getEntrySettings();
			extQuantTableViewer.setInput(quantFiles);
			extQuantTableViewer.refresh();
		} else if (event.getSource() instanceof MSAnnotationStandardQuantApplyDialog) {
			initializeStandardQuantifications();
			standardPeaksTable.setInput(createPeakList());
			standardPeaksTable.refresh();
			initializeStandardFilesTable();
			standardFilesTable.refresh();
		} else if (event.getSource() instanceof MassSpecPeakIntensityApplyDialog) {
			peakIntensityTable.removeAll();
			initializeCorrectedIntensities();
		}
	}
}
