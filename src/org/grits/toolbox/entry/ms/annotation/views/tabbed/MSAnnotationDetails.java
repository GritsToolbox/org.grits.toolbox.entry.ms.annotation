package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.editor.EntryEditorPart;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.display.control.table.process.TableDataProcessorRunner;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessorDialogRunner;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.MSAnnotationTable;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.SharedCheckboxWidget;
import org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessor;
import org.grits.toolbox.entry.ms.tablehelpers.MassSpecTable;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecPeaksView;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecTableBase;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;

/**
 * EntryEditorPart providing the "Details" view of annotated MS data. The Details view provides a detailed view of the candidate
 * annotations from the parent "Structure Annotation" table. The view is a split pane w/ the top providing a scrollable list of
 * the candidate objects, and the bottom another split pane where the annotated peaks of the sub-scan (fragment view) are shown. If there
 * are multiple fragment candidates for a peak, then clicking a row displays a "Selection" view at the very bottom so the user can 
 * select/deselect the fragments associated with a peak.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class MSAnnotationDetails extends EntryEditorPart implements IMSAnnotationPeaksViewer {
	private static final Logger logger = Logger.getLogger(MSAnnotationDetails.class);
	public static final String VIEW_ID = "ms.annotation.glycan.views.MSAnnotationDetails";
	protected MSAnnotationEntityScroller entityScroller = null;
	protected SashForm sashForm = null;
	protected Composite compositeTop = null;
	protected Composite compositeBottom = null;
	protected MSAnnotationEntityProperty msEntityProperty = null;

	protected List<Feature> lFeatures = null;
	protected HashMap<Feature, Peak> htFeatureToPeak = null;
	protected List<MSAnnotationPeaksView> lPeaksViews = null;
	protected List<Composite> lPeakComposites = null;
//	protected Entry entry = null;

	protected int iCurView = -1;
	protected MSAnnotationMultiPageViewer parentViewer = null;
	protected int iMinMSLevel = -1;
	
	// hold copies of a parent subset table data. To be used for interaction between editors
	protected MSAnnotationTable parentSubsetTableData = null;
	protected String parentViewRowId = null;
	protected int parentViewScanNum = -1;
	protected int parentViewRowIndex = -1;
	protected MPart part;
	
	@Inject
	public MSAnnotationDetails(MSAnnotationMultiPageViewer parentViewer, Entry entry, Property msEntityProperty,
			@Named(MassSpecMultiPageViewer.MIN_MS_LEVEL_CONTEXT) int iMinMSLevel) {
		this.entry = entry;
		this.msEntityProperty = (MSAnnotationEntityProperty) msEntityProperty;
		this.iMinMSLevel = iMinMSLevel;
		this.parentViewer = parentViewer;
	}
	
	@PostConstruct
	public void postConstruct (MPart part) {
		this.part = part;
		setParentSubsetTableData();
	}

	public MPart getPart() {
		return part;
	}
	
	public int getParentViewRowIndex() {
		return parentViewRowIndex;
	}
	
	public String getParentViewRowId() {
		return parentViewRowId;
	}
	
	public int getParentViewScanNum() {
		return parentViewScanNum;
	}

	public MSAnnotationTable getParentSubsetTable() {
		return parentSubsetTableData;
	}

	public void setParentSubsetTableData() {
		MSAnnotationMultiPageViewer viewer = getParentMultiPageViewer();
		if (viewer != null) {
			Object activePage = viewer.getPageItem(viewer.getActivePage());
			MassSpecPeaksView peaksView = null;
			if( activePage instanceof MSAnnotationPeaksView ) {
				peaksView = viewer.getPeaksView().get(0);
			} else if ( activePage instanceof MSAnnotationDetails ) {
				MSAnnotationDetails detailsView = viewer.getDetailsView();
				peaksView = detailsView.getCurrentPeaksView();
			}
			MassSpecTableBase tableBase = peaksView.getViewBase();
			MassSpecTable natTable = tableBase.getNatTable();
			parentSubsetTableData = ((MSAnnotationTable) natTable).getCurrentSubsetTable();
			parentViewRowId = parentSubsetTableData.getParentTableRowId();
			parentViewRowIndex = parentSubsetTableData.getParentTableRowIndex();
			parentViewScanNum = parentSubsetTableData.getParentTableParentScanNum();
			logger.debug("Opening new viewer. Parent peak id: " + parentViewRowId);
		}
	}
	
	public void setParentSubsetTableData(MSAnnotationTable parentSubsetTableData, int parentViewRowIndex, int parentViewScanNum, String parentViewRowId) {
		this.parentSubsetTableData = parentSubsetTableData;
		this.parentViewRowId = parentViewRowId;
		this.parentViewRowIndex = parentViewRowIndex;
		this.parentViewScanNum = parentViewScanNum;		
	}
	
	@Override
	public void reInitializeView() {
		// TODO Auto-generated method stub
		
	}
		
	@Override
	public String toString() {
		return "MSAnnotationDetails (" + entry + ")";
	}
	
	public List<Composite> getPeakComposites() {
		return lPeakComposites;
	}
	
	public int getMinMSLevel() {
		return iMinMSLevel;
	}
	
	public void setMinMSLevel(int iMinMSLevel) {
		this.iMinMSLevel = iMinMSLevel;
	}
	
	public void setCurViewIndex( int iCurView ) {
		this.iCurView = iCurView;		
	}

	public int getCurViewIndex() {
		return this.iCurView;
	}

	public MSAnnotationEntityProperty getMsEntityProperty() {
		return msEntityProperty;
	}
	
	public int getNumFeatures() {
		return this.lFeatures.size();
	}

	public MSAnnotationPeaksView getCurrentPeaksView() {
		return this.lPeaksViews.get(this.iCurView);
	}

	public List<MSAnnotationPeaksView> getPeaksViews() {
		return lPeaksViews;
	}

	public MSAnnotationEntityScroller getEntityScroller() {
		return entityScroller;
	}

	private void updateView() {
		try {
			if( compositeBottom != null )
				compositeBottom.setVisible(false);
			if( this.iCurView < 0 ) {
				return;
			}
			compositeBottom = this.lPeakComposites.get(this.iCurView);
			compositeBottom.setVisible(true);
			entityScroller.reDraw();
		} catch ( Exception e ) {
			logger.error("Failed attempt to update view.", e);
		}		
	}

	public void goNext() {
		try {
			this.iCurView++;

			if( this.iCurView >= this.lPeakComposites.size() ) {
				this.iCurView = 0;
			}
			updateView();
			sashForm.layout(true);
		} catch ( Exception e ) {
			logger.error("Attempted to go to next page when at end of list", e);
		}
	}

	public void goPrev() {
		try {
			this.iCurView--;
			if( this.iCurView < 0 ) {
				this.iCurView = this.lPeakComposites.size() - 1;
			}
			updateView();
			sashForm.layout(true);
		} catch ( Exception e ) {
			logger.error("Attempted to go to prev page when at end of list", e);
		}
	}

	protected MSAnnotationPeaksView getNewPeaksView( Entry entry, MSAnnotationEntityProperty msEntityProperty ) {
		getPart().getContext().set(MassSpecMultiPageViewer.MIN_MS_LEVEL_CONTEXT, getMinMSLevel());
		getPart().getContext().set(Property.class, msEntityProperty);
		getPart().getContext().set(Entry.class, entry);
		MSAnnotationPeaksView view = ContextInjectionFactory.make(MSAnnotationPeaksView.class, getPart().getContext());
		return view;
	}

	protected int[] getSashWeights() {
		int[] dWeights = new int[lPeaksViews.size() + 1];
		int dPrefTopWeight = getPrefEntityScrollerWeight();
		int dPrefBotWeight = 1000 - dPrefTopWeight;
		int iInx = 0;
		dWeights[iInx++] = dPrefTopWeight;
		for(int i = 0; i < lPeaksViews.size(); i++ ) {
			dWeights[iInx++] = dPrefBotWeight;
		}
		return dWeights;
	}

	protected int getPrefEntityScrollerWeight() {
		if( entityScroller.compositeTop == null ) 
			return 1;
		int iTopHeight = entityScroller.compositeTop.getSize().y;
		int iFormHeight = sashForm.getSize().y;
		int iTopWeight = (int) Math.ceil( ((double) iTopHeight / (double) iFormHeight) * 1000.0) + 30;
		return iTopWeight;		
	}

	protected int[] getWeights( int[] iPrefWeights ) {
		int[] iWeights = new int[lFeatures.size() + 1 ];
		iWeights[0] = iPrefWeights[0];
		for(int i = 0; i < lFeatures.size(); i++ ) {
			iWeights[i+1] = iPrefWeights[1];;
		}
		return iWeights;
	}
	
	protected int openReadWriteDialog( Integer processType ) {
		TableDataProcessorRunner progressDialog = new TableDataProcessorRunner( (MassSpecTableDataProcessor) getTableDataProcessor()); 	
		try {
			getTableDataProcessor().setProcessType(processType);
			int iStatus = progressDialog.startJob();
			return iStatus;
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return GRITSProcessStatus.ERROR;
	}
	

	protected HashMap<Feature, Peak> loadFeatures( MSAnnotationEntityProperty msProp ) {
		TableDataProcessor proc =  parentViewer.getPeaksView().get(0).getTableDataProcessor();
		MSAnnotationTableDataProcessorDialogRunner progressDialog = new MSAnnotationTableDataProcessorDialogRunner(proc); 	
		try {
			( (MSAnnotationTableDataProcessor) proc).setCurEntityProperty(msProp);
			proc.setProcessType( MSAnnotationTableDataProcessor.READ_FEATURES);
			int iStatus = progressDialog.startJob();
			if( iStatus == GRITSProcessStatus.OK ) {
				return progressDialog.getScanFeatures();
			}
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return null;	
	}

	public int getStatus() {
		if( this.lPeaksViews == null || this.lPeaksViews.isEmpty() ) {
			return GRITSProcessStatus.ERROR;
		} 
		for( MSAnnotationPeaksView view : this.lPeaksViews ) {
			if ( view.getStatus() == GRITSProcessStatus.CANCEL ) {
				return GRITSProcessStatus.CANCEL;
			}
		}
		return GRITSProcessStatus.OK;
//		return this.lPeaksViews != null && ! this.lPeaksViews.isEmpty();
	}
	
	protected void initPeakViews() {
		try {
//			lFeatures = ( (MSAnnotationTableDataProcessor) parentViewer.getPeaksView().get(0).getTableDataProcessor()).getFeaturesForPeak(this.msEntityProperty);
			MSAnnotationTableDataProcessor proc = (MSAnnotationTableDataProcessor) parentViewer.getPeaksView().get(0).getTableDataProcessor();
			htFeatureToPeak = loadFeatures(this.msEntityProperty);
			if( htFeatureToPeak.isEmpty() ) 
				return;
			lFeatures = new ArrayList<>();
			lPeaksViews = new ArrayList<>();
			lPeakComposites = new ArrayList<>();		
			String sDialogText = parentViewer.getThreadedDialog().getMajorProgressBarListener().getCurText();
			int iFeatureCnt = 0;
			int iNumFeatures = htFeatureToPeak.size();
			int iDialogValue = parentViewer.getThreadedDialog().getMajorProgressBarListener().getCurValue();
			for( Feature curFeature : htFeatureToPeak.keySet() ) {
				if( msEntityProperty.getMsLevel() > 3 && 
						msEntityProperty.getAnnotationId() != null && ! msEntityProperty.getAnnotationId().equals(curFeature.getAnnotationId() ) ) {
					continue;
				}
				Composite comp = new Composite(sashForm, SWT.BORDER);
				comp.setVisible(false);
				comp.setLayout(new FillLayout());
				lPeakComposites.add(comp);

				MSAnnotationEntityProperty msProp = (MSAnnotationEntityProperty) this.msEntityProperty.clone();
				int iAnnotId = curFeature.getAnnotationId();
				msProp.setAnnotationId( iAnnotId );
				msProp.setParentScanNum(msProp.getScanNum());
				msProp.setFeatureId(curFeature.getId());
				MSAnnotationPeaksView peaksView = getNewPeaksView(entry, msProp);
				peaksView.setTableDataProcessor( parentViewer.getThreadedDialog() );
			//	peaksView.setInput(getEditorInput());
				( (MSAnnotationTableDataProcessor) peaksView.getTableDataProcessor()).setCurScanFeature(proc.getCurScanFeature());
				( (MSAnnotationTableDataProcessor) peaksView.getTableDataProcessor()).setCurEntityProperty(msProp);
				parentViewer.getThreadedDialog().getMajorProgressBarListener().setProgressMessage(sDialogText + ", Feature " + (iFeatureCnt+1) + " of " + iNumFeatures);
				parentViewer.getThreadedDialog().getMajorProgressBarListener().setProgressValue(iDialogValue + iFeatureCnt++);
				peaksView.createPartControl(comp);
				lPeaksViews.add(peaksView);
				if( peaksView.getStatus() == GRITSProcessStatus.CANCEL ) {
					return;
				}
				/*peaksView.addPropertyListener(new IPropertyListener() {

					@Override
					public void propertyChanged(Object source, int propId) {
						if ( propId == IWorkbenchPartConstants.PROP_DIRTY )
							setDirty( ((MSAnnotationPeaksView) source).isDirty() );

					}
				});*/
				if( iAnnotId == this.msEntityProperty.getAnnotationId() ) {
					this.iCurView = lFeatures.size();
				}
				lFeatures.add(curFeature);
			}	
			parentViewer.getThreadedDialog().getMajorProgressBarListener().setProgressValue(iDialogValue + iFeatureCnt);
			assert this.iCurView >= 0;
		} catch ( Exception e ) {
			logger.error("Error initializing peak views", e);
		}
	}
	
	/** 
	 * this method is called whenever peaksView is updated. This view needs to get dirty whenever one of its
	 * peaks views get dirty.
	 * 
	 * @param the peaks view that has been modified
	 */
	@Optional @Inject
	public void peakViewChanged (@UIEventTopic
			(MSAnnotationPeaksView.EVENT_TOPIC_PEAKVIEW_MODIFIED) MSAnnotationPeaksView peaksView) {
		if (lPeaksViews.contains(peaksView))
			setDirty (peaksView.isDirty());
	}

	protected MSAnnotationEntityScroller getNewMSAnnotationEntityScroller() {
		MSAnnotationEntityScroller es = new MSAnnotationEntityScroller(compositeTop, SWT.NONE, this);
		return es;
	}
	
	protected void addScrollerListeners( MSAnnotationEntityScroller es ) {
		es.getPrevButton().addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {}

			@Override
			public void mouseDown(MouseEvent e) {
				goPrev();
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		es.getNextButton().addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {}

			@Override
			public void mouseDown(MouseEvent e) {
				goNext();	
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {}
		});
		
	}

	protected void createView() {		
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
	protected Composite getParent() {
		return this.parentViewer.getContainer();
	}

	@Override
	public void createPartControl(Composite parent) {
		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.SASH_WIDTH = 5;
		
		compositeTop = new Composite(sashForm, SWT.BORDER);
		RowLayout rowLayout = new RowLayout();
		rowLayout.marginLeft = -50;
		compositeTop.setLayout(rowLayout);
		initPeakViews(); // creates list of peak views
		entityScroller = getNewMSAnnotationEntityScroller();
		entityScroller.createPartControl(compositeTop);
		addScrollerListeners(entityScroller);
		updateView(); //sets bottom to current peak view and instantiates entityScroller
		compositeTop.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				setWeights();
				compositeTop.removePaintListener(this);
			}
		});
		sashForm.addControlListener(new ControlListener() {

			@Override
			public void controlResized(ControlEvent e) {
				setWeights();				
			}

			@Override
			public void controlMoved(ControlEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}

	public List<Feature> getFeatures() {
		return lFeatures;
	}
	
	public Peak getPeakFromFeature( Feature feature ) {
		return htFeatureToPeak.get(feature);
	}
	
	protected void setWeights() {
		int[] dWeights = getSashWeights();
		sashForm.setWeights(dWeights);	
	}

	@Focus
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public MassSpecTableBase getViewBase() {
		return getCurrentPeaksView().getViewBase();
	}

	@Override
	public Composite getSelectionArea() {
		return getCurrentPeaksView().getSelectionArea();
	}

	@Override
	public MSAnnotationSelectionView getCurrentSelectionView() {
		return getCurrentPeaksView().getCurrentSelectionView();
	}

	@Override
	public void initNewSelectionView() {
		getCurrentPeaksView().initNewSelectionView();	
	}

	@Override
	public Feature getFeature(int iRowNum) {
//		return getCurrentPeaksView().getFeature(iRowNum);
		return lFeatures.get(iRowNum);
	}		

	@Override
	public void setDirty(boolean d) {
		super.setDirty(d);
	}

	@Override
	public boolean isDirty() {
		return super.isDirty();
	}

	@Persist
	public void doSave(IProgressMonitor monitor) {
		for( MSAnnotationPeaksView pv : lPeaksViews ) {
			if( pv.isDirty() ) {
				pv.doSave(monitor);
			}
		}
	}

	@Override
	public TableDataProcessor getTableDataProcessor() {
		// TODO Auto-generated method stub
		return null;
	}

	protected MSAnnotationMultiPageViewer getParentMultiPageViewer() {
		Entry parentEntry = getEntry().getParent();
		MSAnnotationMultiPageViewer viewer = MSAnnotationMultiPageViewer.getActiveViewerForEntry(context, parentEntry);		
		return viewer;
	}
	
	protected SharedCheckboxWidget getParentSharedCheckboxWidget() {
		SharedCheckboxWidget scw = parentSubsetTableData.getSharedCheckboxWidget();
		return scw;
	}

	public void toggleParentSelectedRow(String sKey) {
		MSAnnotationMultiPageViewer parentViewer = getParentMultiPageViewer();
		if( parentViewer != null ) {
			MassSpecPeaksView peakView = null;
			if( parentViewer.getAnnotationDetails() == null ) {
				peakView = parentViewer.getPeaksView().get(0);				
			} else {
				int iCurViewInx = parentViewer.getAnnotationDetails().getCurViewIndex();
				peakView = parentViewer.getAnnotationDetails().getPeaksViews().get(iCurViewInx);
			}
			MSAnnotationTable parentTable = (MSAnnotationTable) peakView.getViewBase().getNatTable();
//			if( parentTable.getCurrentRowIndex() != summaryViewer.getParentViewRowIndex() ) { // refresh the subset table in the parent first
			if( ! parentTable.getCurrentRowId().equals(getParentViewRowId()) ) { // refresh the subset table in the parent first
				MSAnnotationMultiPageViewer.showRowSelection(context, parentViewer.getEntry(), parentTable, 
						getParentViewRowIndex(), getParentViewScanNum(), getParentViewRowId());
				parentTable.setCurrentRowId(getParentViewRowId());
			}
			parentTable.getCurrentSubsetTable().toggleSubsetTableRowsForClickedItem(sKey);
			boolean bDirty = parentTable.startUpdateHiddenRowsAfterEdit(parentTable.getCurrentSubsetTable());
			parentTable.finishUpdateHiddenRowsAfterEdit(bDirty);		
			
//			ExtCheckBoxPainter ecbp = getParentSubsetTable().getSharedCheckboxWidget().getHtGlycanToCheckBox().get(sKey);
//			ecbp.setCurStatus( ! ecbp.getCurStatus() );
			
//			MSAnnotationTableBase.propigateSharedCheckboxChanges( getEntry().getParent() );

		}
	}

	@Override
	public void reLoadView() {
		// TODO Auto-generated method stub
		
	}	
	
	public static String getLabelForCheckbox(String sAnnotDesc, String sFeatureId, int iMSLevel) {
		if (iMSLevel > 3) {
			return sAnnotDesc + "-" + sFeatureId;   
		}		
		return sAnnotDesc;
	}
	
	/**
	 * This method handles propagation of check box changes to this page
	 * Event topic @see {@link MSAnnotationMultiPageViewer.PARENT_ENTRY_VALUE_MODIFIED} is sent from 
	 * @see {@link MSAnnotationTable}'s finishUpdateHiddenRowsAfterEdit method
	 * 
	 * @param parentEntry is the entry to be matched to see if the changes should affect this page
	 */
	@Optional
	@Inject
	void refreshCheckboxes(
			@UIEventTopic(MSAnnotationMultiPageViewer.EVENT_PARENT_ENTRY_VALUE_MODIFIED) Entry parentEntry) {
		if( getEntry().getParent() != null && getEntry().getParent().equals(parentEntry) && getPeaksViews() != null ) {
			MSAnnotationEntityScroller entityScroller = (MSAnnotationEntityScroller) getEntityScroller();
			entityScroller.reDrawLabel();
		}
	}
	
}
