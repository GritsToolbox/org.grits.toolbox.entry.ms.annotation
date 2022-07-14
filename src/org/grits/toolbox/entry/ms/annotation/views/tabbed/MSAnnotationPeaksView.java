package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.display.control.table.tablecore.DelayedResizeListener;
import org.grits.toolbox.entry.ms.annotation.command.ViewRowChooserInTabCommandExecutor;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecPeaksView;
import org.grits.toolbox.ms.om.data.Feature;

public class MSAnnotationPeaksView extends MassSpecPeaksView implements IMSAnnotationPeaksViewer {
	private static final Logger logger = Logger.getLogger(MSAnnotationPeaksView.class);
	public static final String VIEW_ID = "plugin.ms.annotation.views.MSAnnotationPeaks"; //$NON-NLS-1$
	public static final String EVENT_TOPIC_PEAKVIEW_MODIFIED = "peaksView_modified_in_a_tab";

	protected Composite compositeTop = null;
	protected Composite compositeBottom = null;
	protected Integer iRowNum = null;
	protected SashForm sashForm = null;
	
	@Inject IEventBroker eventBroker;

	protected MSAnnotationSelectionView selectionView = null;

	@Inject
	public MSAnnotationPeaksView (@Optional Entry entry, @Optional Property msEntityProperty,
			@Named(MassSpecMultiPageViewer.MIN_MS_LEVEL_CONTEXT) int iMinMSLevel) {
		super (entry, msEntityProperty, iMinMSLevel);
	}
	
	@Inject
	public MSAnnotationPeaksView (@Optional Entry entry, @Optional Property msEntityProperty,
			@Named(MSAnnotationMultiPageViewer.ROW_NUM_CONTEXT) int iRowNum, 
			@Named(MassSpecMultiPageViewer.MIN_MS_LEVEL_CONTEXT) int iMinMSLevel) {
		super (entry, msEntityProperty, iMinMSLevel);
		this.iRowNum = iRowNum;
	}

	@Override
	public String toString() {
		return "MSAnnotationPeaksView (" + entry + ")";
	}

	public int getRowNum() {
		return this.iRowNum;
	}

	@Override
	protected void initResultsView(Composite parent) throws Exception {
		this.parent = parent.getParent().getParent();    //CTabFolder
		compositeTop = new Composite(parent, SWT.BORDER);
		compositeTop.setLayout(new GridLayout(1, false));

		try {
			resultsComposite = getNewResultsComposite(compositeTop, SWT.NONE);
			((MSAnnotationResultsComposite) resultsComposite).createPartControl(
					this.compositeTop, this, this.entityProperty,
					this.dataProcessor, FillTypes.PeaksWithFeatures);
			// resultsView.createPartControl(this.compositeTop, this,
			// this.entityProperty, this.dataProcessor, this.fillType);
			resultsComposite.setLayout(new FillLayout());
			this.viewBase = (MSAnnotationTableBase) resultsComposite.getBaseView();
		} catch (Exception e) {
			viewBase = null;
			resultsComposite = null;
			logger.error("Error in MSAnnotationPeaksView: initResultsView");
			throw new Exception(e.getMessage());
		}
	}

	@Override
	protected void addListeners(Composite container) {
		DelayedResizeListener l = new DelayedResizeListener();
		if (resultsComposite != null) {
			l.addTable(resultsComposite.getBaseView().getNatTable());
		}
		if (selectionView != null) {
			l.addTable(selectionView.getSubTable());
		}
		container.addControlListener(l);
	}

	/*
	 * private void createMS1View(Composite container) {
	 * initResultsView(container); }
	 */
	@Override
	public void reInitializeView() throws Exception {
		try {
			sashForm.setVisible(false);
			compositeBottom.setVisible(false);
			createPeaksView(getParent());
			getParent().layout(true);
			sashForm.layout(true);
		} catch( Exception e) {
			viewBase = null;
			resultsComposite = null;
			logger.error("Error in MSAnnotationPeaksView: reInitializeView");
			throw new Exception(e.getMessage());
		}
	}

	@Override
	protected void createPeaksView(Composite container) throws Exception {

		try {
			sashForm = new SashForm(container, SWT.VERTICAL);
			initResultsView(sashForm);
			compositeBottom = new Composite(sashForm, SWT.BORDER);
			compositeBottom.setLayout(new GridLayout(1, false));
			sashForm.setWeights(new int[] { 10, 5 });
		} catch( Exception e) {
			viewBase = null;
			resultsComposite = null;
			logger.error("Error in MSAnnotationPeaksView: createPeaksView");
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * Create contents of the editor part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		try {
			final Composite container = new Composite(parent, SWT.NONE);
			container.setLayout(new FillLayout());
			this.entry = getEntry();
			createPeaksView(container);
			addListeners(container);
		} catch( Exception e ) {
			viewBase = null;
			resultsComposite = null;
		}
	}

	protected MSAnnotationResultsComposite getNewResultsComposite(
			Composite composite, int style) {
		return new MSAnnotationResultsComposite(composite, style);
	}

	public Composite getBottomPane() {
		return compositeBottom;
	}

	public Composite getTopPane() {
		return compositeTop;
	}

	public MSAnnotationSelectionView getSelectionView() {
		return this.selectionView;
	}

	public void setSelectionView(MSAnnotationSelectionView _viewer) {
		this.selectionView = _viewer;
	}

	@Override
	public Composite getSelectionArea() {
		return getBottomPane();
	}

	@Override
	public MSAnnotationSelectionView getCurrentSelectionView() {
		return getSelectionView();
	}

	@Override
	public void initNewSelectionView() {
		setSelectionView(new MSAnnotationSelectionView(getBottomPane()));
	}

	public static void showRowSelection(IEclipseContext context, Entry entry,
			MSAnnotationTable parentTable, int iRowIndex, int iParentScanNum, String sParentRowId ) {
		MSAnnotationMultiPageViewer parent = MSAnnotationMultiPageViewer
				.getActiveViewerForEntry(context, entry);
		if (parent != null) {
			MSAnnotationPeaksView me = (MSAnnotationPeaksView) parent
					.getPeaksView();
			MSAnnotationSelectionView viewer = ViewRowChooserInTabCommandExecutor
					.showRowChooser(me, parentTable, iRowIndex, iParentScanNum, sParentRowId);
			
			// DBW 08/22/16:  These two methods seemed to make no difference!
//			me.setSelectionView(viewer);
//			me.getBottomPane().layout();
		}
	}

	@Override
	protected TableDataProcessor getNewTableDataProcessor(Entry entry, Property entityProperty) {
		MSAnnotationTableDataProcessor proc = new MSAnnotationTableDataProcessor(
				entry, entityProperty, 
				FillTypes.PeaksWithFeatures, getMinMSLevel());
		proc.initializeTableDataObject(entityProperty);
		//		proc.readDataFromFile();
		return proc;
	}

	@Override
	protected TableDataProcessor getNewTableDataProcessor(
			Property entityProperty) {
		MSAnnotationMultiPageViewer parentViewer = MSAnnotationMultiPageViewer
				.getActiveViewerForEntry(context, getEntry().getParent());
		if (parentViewer == null || parentViewer.getPeaksView() == null) {
			return null;
		}
		MSAnnotationTableDataProcessor parentProc = (MSAnnotationTableDataProcessor) parentViewer.getScansView().getTableDataProcessor();
		if( parentProc.getGRITSdata().getAnnotation() == null || parentProc.getGRITSdata().getAnnotation().isEmpty() ) 
			return null;
		//		if( parentProc == null ) 
		//			return null;
		//		if ( ! parentProc.getSourceProperty().equals(entityProperty) ) {
		//			return null;
		//		}
		MSAnnotationTableDataProcessor proc = new MSAnnotationTableDataProcessor(
				parentProc, entityProperty, FillTypes.PeaksWithFeatures, getMinMSLevel());
		proc.setEntry(getEntry());
		proc.initializeTableDataObject(entityProperty);
		return proc;
	}

	public MSAnnotationTableBase getViewBase() {
		return (MSAnnotationTableBase) super.getViewBase();
	}

	@Override
	protected void updateProjectProperty() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void savePreference() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDirty(boolean d) {
		super.setDirty(d);
		eventBroker.send(EVENT_TOPIC_PEAKVIEW_MODIFIED, this);
	}

	@Persist
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		// super.doSave(monitor);
		this.viewBase.doSave(monitor);
		setDirty(false);
	}

	@Override
	public boolean isDirty() {
		return super.isDirty();
	}

	public Integer getiRowNum() {
		return iRowNum;
	}

	public void setiRowNum(Integer iRowNum) {
		this.iRowNum = iRowNum;
	}

	@Override
	public Feature getFeature(int iRowNum) {
		// TODO Auto-generated method stub
		return null;
	}	
}
