package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.editor.ScrollableEntryEditorPart;
import org.grits.toolbox.entry.ms.preference.IMSPreferenceWithCustomAnnotation;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotation;
import org.grits.toolbox.entry.ms.preference.xml.MassSpecCustomAnnotationPeak;

public abstract class MSAnnotationOtherSettingsView extends ScrollableEntryEditorPart {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSAnnotationOtherSettingsView.class);
		
	private MPart part;
	private Composite parent;
	private Composite container;

	protected Color sectionColor;
	protected Color backgroundColor;
	
	// keep track of created tables for each annotation
	Map<String, TableViewer> peaksTableList = new HashMap<>();
	// keep track of other created controls for each annotation
	Map<String, List<Control>> controlList = new HashMap<>();
	
	private Button editButton;
	private Composite sectionComposite;
	private Section section;
	
	protected IMSPreferenceWithCustomAnnotation localAnnotations;
	protected IMSPreferenceWithCustomAnnotation entryAnnotations;

	
	@Inject
	public MSAnnotationOtherSettingsView(Entry entry) {
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
		// add custom annotations table
		createCustomAnnotationSection();
	}
	
	protected void createCustomAnnotationSection() {
		section = new Section(getContainer(), Section.TREE_NODE | Section.TITLE_BAR | Section.EXPANDED);
		section.setText("Custom Annotations");
		
		section.setTitleBarBackground(sectionColor);
		section.setBackground(backgroundColor);
		section.setTitleBarForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE));
		
		GridData gridData = new GridData();
		gridData.horizontalSpan = 6;
		section.setLayoutData(gridData);
		
		sectionComposite = new Composite(section, SWT.WRAP);
		sectionComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 6, 4));
		sectionComposite.setLayout(new GridLayout(3, false));
		sectionComposite.setBackground(backgroundColor);
		sectionComposite.setBackgroundMode(SWT.INHERIT_FORCE);
		
		initializeCustomAnnotations();
		
		Set<String> processed = new HashSet<>();
		for (MassSpecCustomAnnotation customAnnotation: entryAnnotations.getCustomAnnotations()) {
			Set<MassSpecCustomAnnotationPeak> peaks = new HashSet<>();
			if (!processed.contains(customAnnotation.getAnnotationName())) {
				peaks.addAll(customAnnotation.getAnnotatedPeaks().values());
				createPeaksTable(sectionComposite, peaks, customAnnotation);
				processed.add(customAnnotation.getAnnotationName());
			}
		}
		for (MassSpecCustomAnnotation customAnnotation: localAnnotations.getCustomAnnotations()) {
			if (!processed.contains(customAnnotation.getAnnotationName())) {
				Set<MassSpecCustomAnnotationPeak> peaks = new HashSet<>();
				peaks.addAll(customAnnotation.getAnnotatedPeaks().values());
				createPeaksTable(sectionComposite, peaks, customAnnotation);
				processed.add(customAnnotation.getAnnotationName());
			}
		}
		
		createButton(sectionComposite);
		section.setClient(sectionComposite);
	}
	
	void createButton (Composite parent) {
		editButton = new Button(parent, SWT.PUSH);
		editButton.setText("Add/Modify Custom Annotations");
		GridData gd = new GridData(SWT.RIGHT, SWT.FILL, false, true, 3, 1);
		MSAnnotationMultiPageViewer curView = getCurrentViewer();
		if (curView == null)
			editButton.setEnabled(false);
		editButton.setLayoutData(gd);
		
		editButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {	
				openModifyDialog(curView);
			}	

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	protected abstract void openModifyDialog(MSAnnotationMultiPageViewer curView);
	protected abstract void initializeCustomAnnotations ();

	void createPeaksTable (Composite parent, Set<?> input, MassSpecCustomAnnotation annotation) {
		Label customAnnotLabel = new Label(parent, SWT.BOLD);
		customAnnotLabel.setText("Custom Annotation Name:");
		Text customAnnotName = new Text(parent, SWT.BORDER);
		customAnnotName.setEditable(false);
		customAnnotName.setText(annotation.getAnnotationName());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		customAnnotName.setLayoutData(gd);
		
		Label customAnnotDescLabel = new Label(parent, SWT.BOLD);
		customAnnotDescLabel.setText("Description");
		Text customAnnotDescription = new Text(parent, SWT.BORDER);
		customAnnotDescription.setText(annotation.getDescription());
		customAnnotDescription.setEditable(false);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 3);
		customAnnotDescription.setLayoutData(gd);
		
		List<Control> existing = controlList.get(annotation.getAnnotationName());
		if (existing == null) {
			existing = new ArrayList<>();
			existing.add(customAnnotLabel);
			existing.add(customAnnotName);
			existing.add(customAnnotDescLabel);
			existing.add(customAnnotDescription);
			controlList.put(annotation.getAnnotationName(), existing);
		}
		
		TableViewer peaksTable = new TableViewer(parent);
		GridData gd_table_2 = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gd_table_2.heightHint = 100;
		peaksTable.getTable().setLayoutData(gd_table_2);
		peaksTable.getTable().setHeaderVisible(true);
		peaksTable.getTable().setLinesVisible(true);
		TableViewerColumn peakMz = new TableViewerColumn(peaksTable, SWT.NONE);
		peakMz.getColumn().setText("Peak M/z");
		peakMz.getColumn().setWidth(100);
		peakMz.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof MassSpecCustomAnnotationPeak) {
					return ((MassSpecCustomAnnotationPeak) element).getPeakMz() == null ? 
							"" : ((MassSpecCustomAnnotationPeak) element).getPeakMz().toString();
				}
				return "";
			}
		});
			
		TableViewerColumn peakLabel = new TableViewerColumn(peaksTable, SWT.NONE);
		peakLabel.getColumn().setText("Peak Label");
		peakLabel.getColumn().setWidth(100);
		peakLabel.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof MassSpecCustomAnnotationPeak) {
					return ((MassSpecCustomAnnotationPeak) element).getPeakLabel();
				}
				return "";
			}
		});
		TableViewerColumn msLabel = new TableViewerColumn(peaksTable, SWT.NONE);
		msLabel.getColumn().setText("MS Level");
		msLabel.getColumn().setWidth(100);
		msLabel.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof MassSpecCustomAnnotationPeak) {
					return ((MassSpecCustomAnnotationPeak) element).getMSLevel() == null ? 
							"" : ((MassSpecCustomAnnotationPeak) element).getMSLevel().toString();
				}
				return "";
			}
		});
		
		peaksTable.setContentProvider(new ArrayContentProvider());
		peaksTable.setInput(input);
		peaksTableList.put(annotation.getAnnotationName(), peaksTable);	
	}
	
	protected void refreshCustomAnnotations() {
		// update existing tables' input
		boolean needLayout = false;
		String toBeRemoved = null;
		for (String customAnnotName: peaksTableList.keySet()) {
			TableViewer currentPeaksTable = peaksTableList.get(customAnnotName);
			boolean found = false;
			for (MassSpecCustomAnnotation customAnnotation: entryAnnotations.getCustomAnnotations()) {
				if (customAnnotName.equals(customAnnotation.getAnnotationName())) {
					found = true;
					if (!currentPeaksTable.getTable().isDisposed()) {
						currentPeaksTable.setInput(customAnnotation.getAnnotatedPeaks().values());
						currentPeaksTable.refresh();
					}
				}
			}
			if (!found) {
				// remove the old table, custom annotation is removed
				// clean up old controls
				currentPeaksTable.getControl().dispose();
				List<Control> controls = controlList.get(customAnnotName);
				if (controls != null) {
					for (Control control: controls) {
						control.dispose();
					}
				}
				toBeRemoved = customAnnotName;
				needLayout = true;
			}
		}
		
		if (toBeRemoved != null) {
			peaksTableList.remove(toBeRemoved);
			controlList.remove(toBeRemoved);
		}
		
		// add new ones
		for (MassSpecCustomAnnotation customAnnotation: entryAnnotations.getCustomAnnotations()) {
			TableViewer currentPeaksTable = peaksTableList.get(customAnnotation.getAnnotationName());
			if (currentPeaksTable == null) {
				// need to create a new table
				needLayout = true;
				Set<MassSpecCustomAnnotationPeak> peaks = new HashSet<>();
				peaks.addAll(customAnnotation.getAnnotatedPeaks().values());
				createPeaksTable(sectionComposite, peaks, customAnnotation);
			}
		}
		if (needLayout) {
			editButton.dispose();
			createButton(sectionComposite);
			sectionComposite.layout(true, true);
			sectionComposite.redraw();
			section.layout(true, true);
			section.setSize(sectionComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
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
		return MSAnnotationMultiPageViewer.getActiveViewerForEntry(MSAnnotationOtherSettingsView.this.getPart().getContext(), this.entry.getParent());
	}


}
