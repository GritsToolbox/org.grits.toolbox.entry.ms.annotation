package org.grits.toolbox.entry.ms.annotation.tablehelpers;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.sort.SortStatePersistor;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.datamodel.ms.annotation.preference.MSAnnotationViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.preference.MSAnnotationViewerPreferenceLoader;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.dmtranslate.DMAnnotation;
import org.grits.toolbox.datamodel.ms.tablemodel.FillTypes;
import org.grits.toolbox.display.control.table.datamodel.GRITSColumnHeader;
import org.grits.toolbox.display.control.table.datamodel.GRITSListDataProvider;
import org.grits.toolbox.display.control.table.datamodel.GRITSListDataRow;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.display.control.table.tablecore.GRITSHeaderMenuConfiguration;
import org.grits.toolbox.display.control.table.tablecore.GRITSSingleClickConfiguration;
import org.grits.toolbox.entry.ms.annotation.command.MSAnnotationViewColumnChooserCommandHandler;
import org.grits.toolbox.entry.ms.annotation.command.ViewMSOverviewCommandExecutor;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationEntityScroller;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationMultiPageViewer;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationPeaksView;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.MSAnnotationTableBase;
import org.grits.toolbox.entry.ms.property.MassSpecEntityProperty;
import org.grits.toolbox.entry.ms.tablehelpers.MassSpecTable;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecTableBase;
import org.grits.toolbox.ms.annotation.utils.AnnotationRowExtraction;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.ms.om.data.ScanFeatures;

/**
 * Extension of MassSpecTableBase specifically for annotation of mass spec data. 
 * Adds support for a sub-table which can alter the "selections" for annotated scans.
 * MS scans can be unannotated or, if annotated, selected, hidden, or invisible.
 * Includes generic support for displaying images.
 * 
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * @see ImagePainter
 * @see SharedCheckboxWidget
*/
public class MSAnnotationTable extends MassSpecTable {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSAnnotationTable.class);
	
	public static final Image LOCKED_IMAGE = ImageDescriptor.createFromURL(FileLocator.find(
			Platform.getBundle(org.grits.toolbox.entry.ms.annotation.Activator.PLUGIN_ID), new Path("icons" + File.separator + "lock.png"), null)).createImage();
	public static final Image LOCKED_GRAY_IMAGE = ImageDescriptor.createFromURL(FileLocator.find(
			Platform.getBundle(org.grits.toolbox.entry.ms.annotation.Activator.PLUGIN_ID), new Path("icons" + File.separator + "lock-gray.png"), null)).createImage();
	public static final Image UNLOCKED_IMAGE = ImageDescriptor.createFromURL(FileLocator.find(
			Platform.getBundle(org.grits.toolbox.entry.ms.annotation.Activator.PLUGIN_ID), new Path("icons" + File.separator + "lock-open.png"), null)).createImage();
	
	public static final String LOCKSELECTION_LABEL = "lockSelectionLabel";
	public static final String UNLOCKSELECTION_LABEL = "unlockSelectionLabel";
	public static final String CELLEDITOR_LABEL = "comment-editor";
	public static final String EDITORCONFIG_LABEL = "comment-config";
	
	private static final ImagePainter lockedPainter = new ImagePainter(LOCKED_IMAGE);
	private static final ImagePainter lockedGrayPainter = new ImagePainter(LOCKED_GRAY_IMAGE);
	private static final ImagePainter unlockedPainter = new ImagePainter(UNLOCKED_IMAGE);

	protected MSAnnotationTable parentTable = null; // if I'm a subset table (selection window), the parent table
	private String sParentTableRowId; // if I'm a subset table (selection window), the id of the peak my parent window
	private Integer iParentTableParentScanNum;
	private Integer iParentTableRowIndex; // if I'm a subset table (selection window), the index of the row in my parent window

	protected MSAnnotationTable currentSubsetTable = null; // if I'm a parent window, my current subset table (if any)
	private String sCurrentTableRowId; // if I'm a parent window, the id of the peak that created the subset table
	private Integer iCurrentParentScanNum;
	private Integer iCurrentRowIndex; // if I'm a parent window, the index of my row that created the subset table
	private boolean bHideUnannotated = false;
	private boolean bHideInvisible = false;

	protected SharedCheckboxWidget scw = null;
	protected boolean bLoadedSubset = false;
	protected boolean iParentTableNeedsScroll = false;

	public MSAnnotationTable(Composite parent, TableDataProcessor tableDataExtractor) {
		super(parent, tableDataExtractor);		
	}

	public MSAnnotationTable(MSAnnotationTableBase parent, TableDataProcessor xmlExtractor) throws Exception {
		super(parent.getParent(), xmlExtractor);
		this.parentView = parent;
	}

	public MSAnnotationTable(Composite parent, MSAnnotationTable parentTable, int iParentRowIndex, int iParentScanNum, String sParentRowId) {
		super(parent, parentTable.getTableDataProcessor());
		this.sParentTableRowId = sParentRowId;
		this.iParentTableRowIndex = iParentRowIndex;
		this.iParentTableParentScanNum = iParentScanNum;
		this.parentTable = parentTable;
		parentView = null;
		scw = new SharedCheckboxWidget();
	}	
	
	/* (non-Javadoc)
	 * @see org.grits.toolbox.display.control.table.tablecore.GRITSTable#performAutoResize()
	 */
	@Override
	public void performAutoResize() {
		super.performAutoResize();
		if( getParentTableNeedsScroll() && getLastMouseDownRow() != -1 ) {
			moveRowIntoViewport(getLastMouseDownRow());
			int iSize = getViewportLayer().getRowCount();
			int iRowPostion = LayerUtil.convertRowPosition(getBottomDataLayer(), getLastMouseDownRow(), getViewportLayer());
			int iDelta = (iSize / 2) - iRowPostion;
			moveRowIntoViewport(getLastMouseDownRow() - iDelta);
			setParentTableNeedsScroll(false);
		}
	}

	public boolean getParentTableNeedsScroll() {
		return iParentTableNeedsScroll;
	}
	
	public void setParentTableNeedsScroll(boolean iParentTableNeedsScroll) {
		this.iParentTableNeedsScroll = iParentTableNeedsScroll;
	}
	
	public SharedCheckboxWidget getSharedCheckboxWidget() {
		return scw;
	}

	protected MSAnnotationMultiPageViewer getParentMultiPageViewer() {
		Entry parentEntry = parentView.getEntry().getParent();
		MSAnnotationMultiPageViewer viewer = MSAnnotationMultiPageViewer.getActiveViewerForEntry(parentView.getParentEditor().getContext(), parentEntry);
		return viewer;
	}		

	public Integer getParentTableRowIndex() {
		return iParentTableRowIndex;
	}

	public String getParentTableRowId() {
		return sParentTableRowId;
	}

	public void setParentTableRowId(String sParentTableRowId) {
		this.sParentTableRowId = sParentTableRowId;
	}

	public Integer getParentTableParentScanNum() {
		return iParentTableParentScanNum;
	}

	public void setParentTableScanNum(Integer iParentScanNum) {
		this.iParentTableParentScanNum = iParentScanNum;
	}

	public String getCurrentRowId() {
		return sCurrentTableRowId;
	}

	public void setCurrentRowId(String sCurrentTableRowId) {
		this.sCurrentTableRowId = sCurrentTableRowId;
	}

	public Integer getCurrentParentScanNum() {
		return iCurrentParentScanNum;
	}

	public void setCurrentParentScanNum(Integer iCurrentParentScanNum) {
		this.iCurrentParentScanNum = iCurrentParentScanNum;
	}

	public Integer getCurrentRowIndex() {
		return iCurrentRowIndex;
	}

	public void setCurrentRowIndex(Integer iCurrentRowIndex) {
		this.iCurrentRowIndex = iCurrentRowIndex;
	}

	public void reInit(Composite parent, MSAnnotationTable parentTable, int iParentRowIndex, int iParentScanNum, String sParentRowId ) {
		this.sParentTableRowId = sParentRowId;
		this.iParentTableRowIndex = iParentRowIndex;
		this.iParentTableParentScanNum = iParentScanNum;
		this.parentTable = parentTable;
		setMzXMLPathName(parentTable.getMzXMLPathName());
		//	DBW	loadDataFromParent();
//		setPreference(this.parentTable.getPreference());
		createSubsetTable();
		parentView = null;
	}	
	
	@Override
	protected void initCornerLayer() {
		DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(
				columnHeaderDataProvider, rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, sortHeaderLayer) {
			@Override
			public ICellPainter getCellPainter(int columnPosition, int rowPosition, ILayerCell cell,
					IConfigRegistry configRegistry) {
				if (bLoadedSubset) {
					// check if the row is locked or not
					Integer iPeakId = ( (Integer) getBottomDataLayer().getDataValueByPosition( 
							((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0), rowPosition) );
					Integer iScan = null;
					if ( ! getMyTableDataObject().getScanNoCols().isEmpty() &&
							getGRITSTableDataObject().getTableData().get(rowPosition).getDataRow().get( getMyTableDataObject().getScanNoCols().get(0) ) != null ) {
						iScan = (Integer) getGRITSTableDataObject().getTableData().get(rowPosition).getDataRow().get( getMyTableDataObject().getScanNoCols().get(0) );
					}
					int parentScanNo = getParentScanNumberFromTable(parentTable.getParentView(), 0);
					if (iPeakId != null) {
						String sRowId = Feature.getRowId(iPeakId, iScan, ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getUsesComplexRowId());
						boolean isLocked = parentTable.getGRITSTableDataObject().isLockedPeak(parentScanNo, sRowId);
						// if locked, show locked image
						if (isLocked) {
							return lockedPainter;
						} 
					}
					return unlockedPainter;
				}
				return super.getCellPainter(columnPosition, rowPosition, cell, configRegistry);
			}
		}; 
	}
	
	protected void registerLockedIcons(ConfigRegistry configRegistry) {
		  CellPainterDecorator locked= new CellPainterDecorator(new TextPainter(),
	                CellEdgeEnum.BOTTOM_RIGHT,
	                lockedGrayPainter);	
		  configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, locked,
					DisplayMode.NORMAL, LOCKSELECTION_LABEL);
	}

	/**
	 * @return
	 */
	public MSAnnotationTable getCurrentSubsetTable() {
		return currentSubsetTable;
	}

	/**
	 * @param currentSubsetTable
	 */
	public void setCurrentSubsetTable(MSAnnotationTable currentSubsetTable) {
		this.currentSubsetTable = currentSubsetTable;
	}

	@Override
	public void createMainTable() throws Exception {
		try {
			setSimDataObject(((MSAnnotationTableDataProcessor) getTableDataProcessor()).getSimianTableDataObject());
			initCommonTableComponents();
			initColumnChooserLayer();
			registerDataUpdateHandler();
			registerLockedIcons(this.configRegistry);
			registerEditableColumns(this.configRegistry);
			
			updateEventListForVisibility();
			updateRowVisibilityAfterRead();
			bLoadedSubset = false;
			finishNatTable();
			performAutoResizeAfterPaint();
			initialSort();
			
			
		} catch( Exception e ) {
			logger.error("Error initializing table.");
			throw new Exception(e.getMessage());
		}
	}
	
	protected void registerDataUpdateHandler() {
		dataLayer.unregisterCommandHandler(UpdateDataCommand.class);
		dataLayer.registerCommandHandler(new UpdateDataCommandHandler(dataLayer) {
			@Override
			protected boolean doCommand(UpdateDataCommand command) {
				int columnPosition = command.getColumnPosition();
	            int rowPosition = command.getRowPosition();
	            Integer commentCol = null;
	            if (getMyTableDataObject().getCommentCols() != null && !getMyTableDataObject().getCommentCols().isEmpty())
	            	commentCol = getMyTableDataObject().getCommentCols().get(0);
	            Integer ratioCol = null;
	            if (getMyTableDataObject().getRatioCols() != null && !getMyTableDataObject().getRatioCols().isEmpty())
	            	ratioCol = getMyTableDataObject().getRatioCols().get(0);
	            GRITSListDataRow backendRowData = bodyDataProvider.getGRITSListDataRow(rowPosition);
				int rowIndex = getSourceIndexFromRowId(backendRowData.getId());
				int eventRowIdx = getNatIndexFromSourceIndex(rowIndex);
				GRITSListDataRow rowData = (GRITSListDataRow) getGRITSTableDataObject().getTableData().get(rowIndex);
				GRITSListDataRow eventRowData = (GRITSListDataRow) eventList.get(eventRowIdx);
	            if (commentCol != null && columnPosition == commentCol) {
	            	// update comment in the underlying data object
	            	// need to convert the index
					rowData.getDataRow().set(commentCol, command.getNewValue());
					eventRowData.getDataRow().set(commentCol, command.getNewValue());
	            }
	            if (ratioCol != null && columnPosition == ratioCol) {
	            	// update ratio in the underlying data object
	            	// need to convert the index
					rowData.getDataRow().set(ratioCol, command.getNewValue());
					eventRowData.getDataRow().set(ratioCol, command.getNewValue());
	            }
	            //Integer parentScanNo = getScanNumberForVisibility(MSAnnotationTable.this, rowPosition);
	            Integer parentScanNo = getParentScanNumberFromTable(getParentView(), rowPosition);
	            MSAnnotationTableDataProcessor tdp = (MSAnnotationTableDataProcessor) getTableDataProcessor();
				tdp.addDirtyParentScan(parentScanNo);
				getParentView().setDirty(true);
				return true;
			}
		});
		
	}

	protected void registerEditableColumns(ConfigRegistry configRegistry) {
		TextCellEditor textCellEditor = new TextCellEditor();
        textCellEditor.setErrorDecorationEnabled(true);
        textCellEditor.setErrorDecorationText(
        		"Ratio should be a floating point number");
        textCellEditor.setDecorationPositionOverride(SWT.LEFT | SWT.TOP);
        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, textCellEditor, DisplayMode.NORMAL, CELLEDITOR_LABEL+DMAnnotation.annotation_ratio.name());
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, new TextCellEditor(), DisplayMode.NORMAL, CELLEDITOR_LABEL+TableDataProcessor.commentColHeader.getKeyValue());
		configRegistry.registerConfigAttribute(
                EditConfigAttributes.DATA_VALIDATOR,
                new DoubleDataValidator(), DisplayMode.EDIT,
                EDITORCONFIG_LABEL+DMAnnotation.annotation_ratio.name());
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, getEditableRuleForColumn(DMAnnotation.annotation_ratio.name()), 
				DisplayMode.EDIT, EDITORCONFIG_LABEL+DMAnnotation.annotation_ratio.name());
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, getEditableRuleForColumn(TableDataProcessor.commentColHeader.getKeyValue()), 
				DisplayMode.EDIT, EDITORCONFIG_LABEL+TableDataProcessor.commentColHeader.getKeyValue());
	}

	@Override
	protected void initialSort() {
		if (((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols() != null &&
				!((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().isEmpty()) {
			int iScanNumIndex = ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0);
			this.getSortModel().sort(iScanNumIndex, SortDirectionEnum.ASC, false);
		}
	}

	protected boolean addUnrecognizedHeadersToSubsetTable( MSAnnotationViewerPreference parentPref, MSAnnotationViewerPreference pref ) {
		boolean bChanged = false;
		for( GRITSColumnHeader parentHeader : parentPref.getPreferenceSettings().getHeaders() ) {
			if( ! pref.getPreferenceSettings().hasColumn(parentHeader.getKeyValue() ) ) {
				pref.getPreferenceSettings().addColumn(parentHeader);
				bChanged = true;
			}
		}
		return bChanged;
	}
	
	public void createSubsetTable()  {
		try {
			loadDataFromParent();
			MSAnnotationViewerPreference parentPref = (MSAnnotationViewerPreference) parentTable.getPreference();
			MSAnnotationViewerPreference pref = MSAnnotationViewerPreferenceLoader.getTableViewerPreference(parentPref.getMSLevel(), FillTypes.Selection);
			if( pref.getPreferenceSettings() == null ) { // not initialized
				pref.setPreferenceSettings(parentPref.getPreferenceSettings());
				pref.writePreference();
			} else {
				boolean bUpdate = addUnrecognizedHeadersToSubsetTable(parentPref, pref);
				if( bUpdate ) {
					pref.writePreference();
				}
			}
			setPreference(pref);
			initCommonTableComponents();
			initColumnChooserLayer();
			registerSelectedCheckbox(this.configRegistry, getCheckboxEditableRule());
			bLoadedSubset = true;
			finishNatTable();
			performAutoResizeAfterPaint();
			parentTable.setCurrentSubsetTable(this);
		} catch (Exception e) {
			logger.error("Failed to create subset table.", e);
		}
	}
	
	/** 
	 * determines when the given column 
	 * should be editable. For example, "ratio" column is only editable for rows with an annotation (feature).
	 * Any new editable column's rule should be added here.
	 * @return rule for editing comment column
	 */
	protected IEditableRule getEditableRuleForColumn (String column) {
		IEditableRule rule = new IEditableRule() {

			@Override
			public boolean isEditable(int arg0, int arg1) {
				if (arg0 == arg1) {
					return true;
				}
				return false;
			}

			@Override
			public boolean isEditable(ILayerCell arg0, IConfigRegistry arg1) {
				if (column.equals (DMAnnotation.annotation_ratio.name())) {
					// only editable for rows with an annotation and 
					// enable only for ms1 level not for the fragmentation
					int iCurMSLevel = ((MassSpecEntityProperty) parentView.getEntityProperty()).getMsLevel();
					if (iCurMSLevel > 2)
						return false;
					if (getMyTableDataObject().getFeatureIdCols() == null || getMyTableDataObject().getFeatureIdCols().isEmpty())
						return false;
					Object featureId = getBottomDataLayer().getDataValueByPosition( 
							((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().get(0), arg0.getRowIndex()) ;
					if (featureId != null)
						return true;
									
					return false;
				} else {  // other editable columns are always editable
					return true;
				}
			}
		};
		return rule;
	}
	
	/**
	 * enable "selection table" checkbox only when the selections are not locked
	 * @return rule for editing candidate selection checkboxes
	 */
	protected IEditableRule getCheckboxEditableRule () {
		IEditableRule rule = new IEditableRule() {

			@Override
			public boolean isEditable(int arg0, int arg1) {
				if (arg0 == arg1) {
					return true;
				}
				return false;
			}

			@Override
			public boolean isEditable(ILayerCell arg0, IConfigRegistry arg1) {
				if (getBottomDataLayer().getDataValueByPosition(
						arg0.getColumnIndex(), arg0.getRowIndex()) != null) {
					// check to see if the selections are locked
					Integer iParentScanNum = getParentScanNumberFromTable(parentTable.getParentView(), 0);
					Integer iPeakId = ( (Integer) getBottomDataLayer().getDataValueByPosition( 
							((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0), 0) );
					if (iParentScanNum != null && iPeakId != null) {
						Integer iScan = null;
						if ( ! getMyTableDataObject().getScanNoCols().isEmpty() &&
								getGRITSTableDataObject().getTableData().get(0).getDataRow().get( getMyTableDataObject().getScanNoCols().get(0) ) != null ) {
							iScan = (Integer) getGRITSTableDataObject().getTableData().get(0).getDataRow().get( getMyTableDataObject().getScanNoCols().get(0) );
						}
						String sRowId = Feature.getRowId(iPeakId, iScan, ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getUsesComplexRowId());						
						if (!parentTable.getGRITSTableDataObject().isLockedPeak(iParentScanNum, sRowId)) {
							return true;
						}
					}
					else
						return true;
				}
				return false;
			}
		};
		return rule;
	}

	@Override
	protected boolean addHeaderListeners() {
		return ! bLoadedSubset;
	}

	@Override
	protected void initColumnChooserLayer() {
		MSAnnotationViewColumnChooserCommandHandler columnChooserCommandHandler = new MSAnnotationViewColumnChooserCommandHandler( this );		
		columnGroupHeaderLayer.registerCommandHandler(columnChooserCommandHandler);		
	}

	public void setHideUnannotated(boolean bHideUnannotated) {
		this.bHideUnannotated = bHideUnannotated;
	}
	
	public void setHideInvisible(boolean bHideInvisible) {
		this.bHideInvisible = bHideInvisible;
	}
	
	@Override
	protected void hideRows(List<Integer> alHiddenRows) {
		super.hideRows(alHiddenRows);
		if( isEmpty() ) {
			logger.debug("No annotated rows! Adding an empty one");
			GRITSListDataRow blankRow = (GRITSListDataRow) TableDataProcessor.getNewRow( getGRITSTableDataObject().getLastHeader().size(), 0  );
			GRITSColumnHeader firstCol = getGRITSTableDataObject().getTablePreferences().getPreferenceSettings().getColumnAtVisColInx(0);
			int iTableFirstCol = getGRITSTableDataObject().getLastHeader().indexOf(firstCol);
			blankRow.getDataRow().set(iTableFirstCol, "No annotated rows");
			eventList.add(blankRow);
		}
	}

	public boolean hideUnannotated() {
		return bHideUnannotated;
	}
	
	public boolean getHideInvisible() {
		return this.bHideInvisible;
	}

	public void hideUnannotatedRows() {
		setHideUnannotated(true);
		finishUpdateHiddenRowsAfterEdit(false);
	}

	public void showUnannotatedRows() {
		setHideUnannotated(false);
		finishUpdateHiddenRowsAfterEdit(false);
	}

	private MSAnnotationTableDataObject getMyTableDataObject() {
		return (MSAnnotationTableDataObject) getGRITSTableDataObject();
	}

	private MSAnnotationTableDataObject getMSAnnotationTableDataObject( MSAnnotationTable _table ) {
		return (MSAnnotationTableDataObject) _table.getGRITSTableDataObject();
	}

	protected void loadDataFromParent() {
		// currently, only supporting the row selection method for non-merge reports, thus take first scan col
		//		int iPeakId = (Integer) parentTable.getSimDataObject().getTableData().
		//				get(getParentPeakId()).getDataRow().
		//				get(getMSAnnotationTableDataObject(parentTable).getPeakIdCols().get(0));
		//		setSimDataObject( getMSAnnotationTableDataObject(parentTable).getSubsetSimianTableDataObject( iPeakId ) );
		boolean bCheckParentScan = ! ((MSAnnotationTableDataProcessor) getTableDataProcessor()).getMethod().getMsType().equals( Method.MS_TYPE_INFUSION );
		setSimDataObject( getMSAnnotationTableDataObject(parentTable).getSubsetSimianTableDataObject( getParentTableParentScanNum(), getParentTableRowId(), bCheckParentScan ) );
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void initCellAccumulator() {
		MSAnnotationCellOverrideLabelAccumulator cellLabelAccumulator = null;
		MSAnnotationCellOverrideLabelAccumulatorForRowHeader rowHeaderCellAccumulator = null;
		Integer filterCol = null;
		if (getMyTableDataObject().getFilterCols() != null && !getMyTableDataObject().getFilterCols().isEmpty())
			filterCol = getMyTableDataObject().getFilterCols().get(0);
		Integer commentCol = null;
		if (getMyTableDataObject().getCommentCols() != null && !getMyTableDataObject().getCommentCols().isEmpty())
			commentCol = getMyTableDataObject().getCommentCols().get(0);
		Integer ratioCol = null;
		if (getMyTableDataObject().getRatioCols() != null && !getMyTableDataObject().getRatioCols().isEmpty())
			ratioCol = getMyTableDataObject().getRatioCols().get(0);
		if ( parentTable == null ) {
			List<Integer> intensityCols = getAllIntensityColumns(null);
			cellLabelAccumulator = new MSAnnotationCellOverrideLabelAccumulator(this.bodyDataProvider, null,
					filterCol, commentCol, ratioCol, intensityCols );
			rowHeaderCellAccumulator = new MSAnnotationCellOverrideLabelAccumulatorForRowHeader<>(bodyDataProvider, this, 0);
		} else {	
			List<Integer> intensityCols = getAllIntensityColumns(1);  // shift all values by 1 because of the selection column
			cellLabelAccumulator = new MSAnnotationCellOverrideLabelAccumulator(this.bodyDataProvider, 0, filterCol, commentCol, ratioCol, intensityCols);
		}

		dataLayer.setConfigLabelAccumulator(cellLabelAccumulator);	
		if (rowHeaderCellAccumulator != null)
			rowHeaderDataLayer.setConfigLabelAccumulator(rowHeaderCellAccumulator);
	}

	protected void updateEventListForVisibility() {	
		if ( getGRITSTableDataObject().getTableData() == null || 
				getGRITSTableDataObject().getTableData().isEmpty() || 
				getMyTableDataObject().getPeakIdCols().isEmpty() ) 
			return;
		try {
			int iStartCol = getTableDataProcessor().getLastVisibleCol();
			for(int i = 0; i < getGRITSTableDataObject().getTableData().size(); i++ ) {
				if ( getGRITSTableDataObject().getTableData().get(i).getDataRow().get( getMyTableDataObject().getPeakIdCols().get(0) ) == null )
					continue;

				// iterating over the table data object, so get scan number passing the table data object				
				Integer iParentScanNum = getScanNumberForVisibility(getMyTableDataObject(), i);
				Integer iPeakId = (Integer) getGRITSTableDataObject().getTableData().get(i).getDataRow().get( getMyTableDataObject().getPeakIdCols().get(0) );
				// overwrite the eventlist where necessary to make data "invisible" or "visible" using the SimDataObject backend
				int iNatIndex = getNatIndexFromSourceIndex(i);
				if( iNatIndex < 0 || iNatIndex >= eventList.size() ) { 
					continue;
				}
				Integer iScan = null;
				if ( ! getMyTableDataObject().getScanNoCols().isEmpty() &&
						getGRITSTableDataObject().getTableData().get(i).getDataRow().get( getMyTableDataObject().getScanNoCols().get(0) ) != null ) {
					iScan = (Integer) getGRITSTableDataObject().getTableData().get(i).getDataRow().get( getMyTableDataObject().getScanNoCols().get(0) );
				}
				String sRowId = Feature.getRowId(iPeakId, iScan, ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getUsesComplexRowId());
				
				boolean bInvisible = getGRITSTableDataObject().isInvisibleRow(iParentScanNum, sRowId);
				GRITSListDataRow eventRowData = (GRITSListDataRow) eventList.get(iNatIndex);					
				GRITSListDataRow backendRowData = (GRITSListDataRow) getGRITSTableDataObject().getTableData().get(i);	
				//			int iStartCol = MSAnnotationTableDataProcessorUtil.getLastPeakColumn() + 1;
				for( int iCol = iStartCol; iCol < eventRowData.getDataRow().size(); iCol++ ) {
					Object eventObj = eventRowData.getDataRow().get(iCol);
					Object backendObj = backendRowData.getDataRow().get(iCol);
					if ( backendObj == null ) // already null, no change in visibility ever
						continue;
					if ( ! backendObj.equals(eventObj) && ! bInvisible )
						eventRowData.getDataRow().set( iCol, backendObj );
					else if ( backendObj.equals(eventObj) && bInvisible )
						eventRowData.getDataRow().set( iCol, null );

				}
			}
		} catch( Exception ex ) {
			logger.error("Exception in GRITStable:updateEventListForVisibility.", ex);
		}
	}

	
	@Override
	public void mouseDoubleClick(MouseEvent e) {
		logger.debug("Double click on the table");
		if ( parentTable != null )  // only handle event this way for the parent table of a SimGlycan Result (not merge)
			return;

		if ( getMyTableDataObject().getScanNoCols().isEmpty() ) 
			return;

		if ( getMzXMLPathName() == null ) 
			return;

		GridLayer gl = (GridLayer) getLayer();

		int origRow = gl.getRowPositionByY(e.y); 
		if ( origRow < 2 )
			return;

		int origCol = gl.getColumnPositionByX(e.x); 
		if ( origCol < 1 )
			return;

		int iRowPostion = LayerUtil.convertRowPosition(gl, origRow, getBottomDataLayer());
		int iColPostion = LayerUtil.convertColumnPosition(gl, origCol, getBottomDataLayer());

		int iScanNum = -1;
		int iAnnotId = -1;
		String sFeatureId = null;
		double dMz = 0.0;
		Integer iParentScan = -1;
		if ( hasColumnGroupHeader() ) { // if a merge result, you have to double-click the scan num of the experiment
			Object obj = getBottomDataLayer().getDataValueByPosition( iColPostion, iRowPostion);	
			if( obj == null ) {
				return;
			}
			try {
				iScanNum = Integer.parseInt(obj.toString());
			} catch(NumberFormatException ex) {
				logger.error(ex.getMessage(), ex);
				return;
			}
			if ( iScanNum == -1 ) 
				return;
			int iInx = getMyTableDataObject().getScanNoCols().indexOf(iScanNum);
		} else { // if not a merge result, there is only 1 scan num, so just get(0)
			Object obj = getBottomDataLayer().getDataValueByPosition( getMyTableDataObject().getScanNoCols().get(0), iRowPostion);	
			if( obj == null ) {
				return;
			}
			try {
				iScanNum = Integer.parseInt(obj.toString());
			} catch(NumberFormatException ex) {
				logger.error(ex.getMessage(), ex);
				return;
			}
			if( ! getMyTableDataObject().getMzCols().isEmpty() ) {
				Object obj2 = getBottomDataLayer().getDataValueByPosition( getMyTableDataObject().getMzCols().get(0), iRowPostion);	
				try {
					dMz = Double.parseDouble(obj2.toString());
				} catch(NumberFormatException ex) {
					logger.error(ex.getMessage(), ex);
					return;
				}
			}
			if( getMyTableDataObject().getAnnotationIdCols()!= null && ! getMyTableDataObject().getAnnotationIdCols().isEmpty() ) {
				obj = getBottomDataLayer().getDataValueByPosition( getMyTableDataObject().getAnnotationIdCols().get(0), iRowPostion);	
				if( obj != null ) {
					try {
						iAnnotId = Integer.parseInt(obj.toString());
						//				sID = (String) getBottomDataLayer().getDataValueByPosition( getMyTableDataObject().getIdCols().get(0), iRowPostion);
					} catch(NumberFormatException ex) {
						logger.error(ex.getMessage(), ex);
						return;
					}
				}
			}
			if( getMyTableDataObject().getFeatureIdCols() != null && ! getMyTableDataObject().getFeatureIdCols().isEmpty() ) {
				obj = getBottomDataLayer().getDataValueByPosition( getMyTableDataObject().getFeatureIdCols().get(0), iRowPostion);	
				if( obj != null ) {
					try {
						sFeatureId = obj.toString();
					} catch(NumberFormatException ex) {
						logger.error(ex.getMessage(), ex);
						return;
					}
				}
			}
			iParentScan = -1;
			if( getMyTableDataObject().getParentNoCol() != null && ! getMyTableDataObject().getParentNoCol().isEmpty() ) {
				obj = getBottomDataLayer().getDataValueByPosition( getMyTableDataObject().getParentNoCol().get(0), iRowPostion);	
				if( obj != null && ! obj.toString().equals("") ) {
					try {
						iParentScan = (Integer) obj;
					} catch(Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
				}
			}
		}
		int iCurMSLevel = ((MassSpecEntityProperty) parentView.getEntityProperty()).getMsLevel();	
		performDoubleClickOnScan(iScanNum, dMz, iAnnotId, sFeatureId, iCurMSLevel, iParentScan);
	}
	
	protected Entry getNewTableCompatibleEntry(Entry parentEntry) {
		Entry newEntry = MSAnnotationEntityProperty.getTableCompatibleEntry(parentEntry);	
		return newEntry;
	}
	
	public void performDoubleClickOnScan( int iScanNum, double dMz, int iAnnotId, String sFeatureId, int iMSLevel, Integer iParentScan ) {

		String sRunName = parentView.getEntry().getParent().getDisplayName();

		int iParentScanNum = -1;
		double dParentMz = 0.0;
		String sParentFeatureId = null;
		if( parentView.getEntry().getProperty() instanceof MSAnnotationEntityProperty ) {
			iParentScanNum = ( (MSAnnotationEntityProperty) parentView.getEntry().getProperty()).getScanNum();
			dParentMz = ( (MSAnnotationEntityProperty) parentView.getEntry().getProperty()).getMz();
			sParentFeatureId = ( (MSAnnotationEntityProperty) parentView.getEntry().getProperty()).getFeatureId(); 
		}
		if( iParentScan != null ) { // TEST TEST TEST
			iParentScanNum = iParentScan.intValue();
		}
		Entry newEntry = getNewTableCompatibleEntry(parentView.getEntry());
		MassSpecEntityProperty msEntityProp = (MassSpecEntityProperty) newEntry.getProperty();
		msEntityProp.setParentScanNum(iParentScanNum);
		msEntityProp.setScanNum(iScanNum);
		msEntityProp.setMz(dMz);
		msEntityProp.setParentMz(dParentMz);
		( (MSAnnotationEntityProperty) parentView.getEntry().getProperty()).setParentFeatureId(sParentFeatureId);
		msEntityProp.setDataFile(( (MSAnnotationEntityProperty) parentView.getEntry().getProperty()).getDataFile());
//		int iCurMSLevel = ((MassSpecEntityProperty) parentView.getEntityProperty()).getMsLevel();		
		msEntityProp.setMsLevel(iMSLevel + 1);

		StringBuilder sb = new StringBuilder(newEntry.getDisplayName());
		if( dMz == 0.0 ) {
			sb.append(": ");
		} else { // fragmentation pathway
			sb.append("->");
		}
		sb.append("[Scan ");
		sb.append(iScanNum);
		sb.append(", MS");
		sb.append(iMSLevel);
		if( dMz > 0.0 ) {
			sb.append(", ");
			sb.append(dMz);
		}
		if(iMSLevel > 2) { // if we're descending down a particular parent structure, then include the annotation id
			sb.append(", ");
			sb.append(iAnnotId);
		}
		sb.append("]");
		newEntry.setDisplayName(sb.toString());
		if( newEntry.getProperty() instanceof MSAnnotationEntityProperty ) {
			MSAnnotationEntityProperty msAnnotEntityProp = (MSAnnotationEntityProperty) newEntry.getProperty();
			msAnnotEntityProp.setAnnotationId( iAnnotId );
			msAnnotEntityProp.setFeatureId(sFeatureId);			
		}
		showMSOverview(newEntry);
	}

	@Override	
	protected void showMSOverview(Entry newEntry) {
		ViewMSOverviewCommandExecutor.showMSOverview(parentView.getParentEditor().getContext(), newEntry);		
	}

	@Override
	public void mouseDown(MouseEvent e) {
		if ( e.count > 1 || parentTable != null|| hasColumnGroupHeader() )  // only handle event this way for the parent table of a SimGlycan Result (not merge)
			return;

		if ( getMyTableDataObject().getMSLevel() == 1 )
			return;

		// TODO: handle event for Merge
		GridLayer gl = (GridLayer) getLayer();

		int origRow = gl.getRowPositionByY(e.y); 
		if ( origRow < 2 )
			return;

		int origCol = gl.getColumnPositionByX(e.x); 
		if ( origCol < 1 )
			return;

		int iRowPostion = LayerUtil.convertRowPosition(gl, origRow, getBottomDataLayer());
		performMouseDown(iRowPostion);
	}
	
	public void performMouseDown( int iRowPostion )	{
		GRITSListDataRow row = ((GRITSListDataProvider) getBottomDataLayer().getDataProvider()).getGRITSListDataRow(iRowPostion);

		Object obj = getBottomDataLayer().getDataValueByPosition( getMyTableDataObject().getPeakIdCols().get(0), iRowPostion);	
		if( obj == null ) {
			return;
		}
		int iPeakId = -1;
		try {
			iPeakId = ((Integer) obj).intValue();
		} catch(Exception ex) {
			logger.error(ex.getMessage(), ex);
			return;
		}
		Integer iScanNum = null;
		if( getMyTableDataObject().getScanNoCols() != null && ! getMyTableDataObject().getScanNoCols().isEmpty() && 
				getBottomDataLayer().getDataValueByPosition( getMyTableDataObject().getScanNoCols().get(0), iRowPostion ) != null ) {
			iScanNum = (Integer) getBottomDataLayer().getDataValueByPosition( getMyTableDataObject().getScanNoCols().get(0), iRowPostion );
//			iScanNum = iScan.intValue();
		}
		int iRowIndex = getSourceIndexFromRowId(row.getId());
//		String sRowId = Feature.getRowId(iPeakId, iScanNum);
		String sRowId = null;
		if( iScanNum != null && iScanNum > 0 ) {
			sRowId = Feature.getRowId(iPeakId, iScanNum, ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getUsesComplexRowId());
		} else {
			sRowId = Feature.getRowId(iPeakId, null, ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getUsesComplexRowId());
		}
		// mousedown on the nattable, so get scan number passing self
		Integer iParentScan = getScanNumberForVisibility(this, iRowIndex);
		setCurrentRowIndex(iRowIndex);
		setCurrentRowId(sRowId);
		setCurrentParentScanNum(iParentScan);
		setLastMouseDownRow(iRowPostion); // we will use this to scroll back to where we were
		
		doCommand(new SelectRowsCommand(this.selectionLayer, 0, iRowPostion, false, false));
		openRowSelection(iRowIndex, iParentScan, sRowId);
	}
	
	protected void openRowSelection(int iSourceRowIndex, int iSourceParentScanNum, String sSourceParentRowId ) {
		MSAnnotationPeaksView.showRowSelection(parentView.getParentEditor().getContext(), parentView.getEntry(), 
				this, iSourceRowIndex, iSourceParentScanNum, sSourceParentRowId);		
	}

	@Override
	public void mouseUp(MouseEvent e) {
	}	

	public int getScanNumberForVisibility( MSAnnotationTable table, int i ) {	
		MSAnnotationTableDataObject tdo = (MSAnnotationTableDataObject) table.getGRITSTableDataObject();
		return getScanNumberForVisibility(tdo, i);
	}

	public int getScanNumberForVisibility( MSAnnotationTableDataObject tdo, int i ) {
		if( parentView == null || ((MassSpecEntityProperty) parentView.getEntityProperty()) == null 
				|| ((MassSpecEntityProperty) parentView.getEntityProperty()).getMsLevel() == null ) {
			return -1;
		}
		int iCurMSLevel = ((MassSpecEntityProperty) parentView.getEntityProperty()).getMsLevel();
		if( iCurMSLevel < 3 && ((MSAnnotationTableDataProcessor) getTableDataProcessor()).getMethod().getMsType().equals( Method.MS_TYPE_INFUSION ) ) {
			return ((MSAnnotationTableDataProcessor) getTableDataProcessor()).getGRITSdata().getFirstMS1Scan();
		}
		if( i < 0 ) {
			return -1;
		}		
		Integer iParentScanNum = null;
		if ( tdo.getParentNoCol() != null && ! tdo.getParentNoCol().isEmpty()) {
			if( tdo.getTableData().get(i).getDataRow().get( tdo.getParentNoCol().get(0)) != null ) {
				iParentScanNum = (Integer) tdo.getTableData().get(i).getDataRow().get( tdo.getParentNoCol().get(0));
			}

		}
		if( iParentScanNum == null ) {
			return -1;
		}
		return iParentScanNum.intValue();
	}

	// updates row visibility based on subset table (annotation selector with checkboxes)
	public boolean startUpdateHiddenRowsAfterEdit( MSAnnotationTable subsetTable ) {
		if ( hasColumnGroupHeader() )
			return false;
		Hashtable<Integer, Hashtable<String, String>> htScanToFirstAnnotation = new Hashtable<Integer, Hashtable<String, String>>();
		Hashtable<Integer, ArrayList<String>> htAtLeastOne = new Hashtable<Integer, ArrayList<String>>();
		boolean isDirty = false;
		int iNumRows = subsetTable.getBottomDataLayer().getRowCount();
		if ( iNumRows == 0 )
			return false;
		MSAnnotationTableDataProcessor tdp = (MSAnnotationTableDataProcessor) getTableDataProcessor();
		for( int i = 0; i < iNumRows; i++ ) {
			if ( subsetTable.getBottomDataLayer().getDataValueByPosition( 
					((MSAnnotationTableDataObject) subsetTable.getGRITSTableDataObject()).getParentNoCol().get(0), i) == null )
				continue;
//			if ( subsetTable.getBottomDataLayer().getDataValueByPosition( 
//					((MSAnnotationTableDataObject) subsetTable.getGRITSTableDataObject()).getScanNoCols().get(0), i) == null )
//				continue;
			if ( subsetTable.getBottomDataLayer().getDataValueByPosition( 
					((MSAnnotationTableDataObject) subsetTable.getGRITSTableDataObject()).getPeakIdCols().get(0), i) == null )
				continue;
			if ( subsetTable.getBottomDataLayer().getDataValueByPosition( 
					((MSAnnotationTableDataObject) subsetTable.getGRITSTableDataObject()).getFeatureIdCols().get(0), i) == null )
				continue;
			// iterating over the nattable bottom layer, so get scan number passing self
			Integer iParentScanNum = getScanNumberForVisibility(subsetTable, i);
//			Integer iScanNum = ( (Integer) subsetTable.getBottomDataLayer().getDataValueByPosition( 
//					((MSAnnotationTableDataObject) subsetTable.getGRITSTableDataObject()).getScanNoCols().get(0), i) );
			
			Integer iScanNum = null;
			if ( subsetTable.getBottomDataLayer().getDataValueByPosition( 
					((MSAnnotationTableDataObject) subsetTable.getGRITSTableDataObject()).getScanNoCols().get(0), i) != null ) {
				iScanNum = ( (Integer) subsetTable.getBottomDataLayer().getDataValueByPosition( 
						((MSAnnotationTableDataObject) subsetTable.getGRITSTableDataObject()).getScanNoCols().get(0), i) );
			}

			Integer iPeakId = ( (Integer) subsetTable.getBottomDataLayer().getDataValueByPosition( 
					((MSAnnotationTableDataObject) subsetTable.getGRITSTableDataObject()).getPeakIdCols().get(0), i) );
			String sFeature = subsetTable.getBottomDataLayer().getDataValueByPosition( 
					((MSAnnotationTableDataObject) subsetTable.getGRITSTableDataObject()).getFeatureIdCols().get(0), i).toString();
 			String sRowId = Feature.getRowId(iPeakId, iScanNum, ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getUsesComplexRowId());

			boolean bPrevInvisible = getGRITSTableDataObject().isInvisibleRow(iParentScanNum, sRowId);
			boolean bPrevHidden = getGRITSTableDataObject().isHiddenRow(iParentScanNum, sRowId, sFeature);
			boolean bCurHidden = ! ( (Boolean) subsetTable.getBottomDataLayer().getDataValueByPosition( 0, i ) ).booleanValue();
			if ( ! bCurHidden ) {
				getGRITSTableDataObject().removeHiddenRow(iParentScanNum, sRowId, sFeature);
				if ( bPrevInvisible || bPrevHidden ) {
					int rowIndex = locateRowInParentTable(sFeature, iPeakId);
					updateFilterVal(rowIndex, true, true);
					Integer filterVal = getFilterVal(rowIndex, true);
					if (filterVal != null && filterVal < 10) { // no match
						// see if there are hidden matches
						adjustFilterVal (rowIndex, iParentScanNum, sRowId, iScanNum, true);
					}
					isDirty = true;
				}
			} else if ( bCurHidden ) {
				getGRITSTableDataObject().setHiddenRow(iParentScanNum, sRowId, sFeature);
				if ( ! bPrevHidden || ! bPrevInvisible ) {
					int rowIndex = locateRowInParentTable(sFeature, iPeakId);
					updateFilterVal(rowIndex, false, true);
					isDirty = true;
				}
			}
			if( isDirty ) {
				tdp.addDirtyParentScan(iParentScanNum);				
				String sHeaderKey = MSAnnotationEntityScroller.getCombinedKeyForLookup( iPeakId, sFeature );	
				boolean bChecked = ( (Boolean) subsetTable.getBottomDataLayer().getDataValueByPosition( 0, i ) ).booleanValue();
				SharedCheckboxWidget scw = subsetTable.getSharedCheckboxWidget();
				ExtCheckBoxPainter ecbp = scw.getHtGlycanToCheckBox().get(sHeaderKey);
				ecbp.setCurStatus(bChecked);
				getGRITSTableDataObject().addManuallyChangedPeak(iParentScanNum, sRowId);
				subsetTable.notifyListeners(iParentScanNum, iPeakId, sFeature );
			}

			if( sFeature != null ) {
				if ( ! bCurHidden ) {
					ArrayList<String> alAtLeastOne = null;
					if( ! htAtLeastOne.containsKey(iParentScanNum) ) {
						alAtLeastOne = new ArrayList<String>();
						htAtLeastOne.put(iParentScanNum, alAtLeastOne);
					} else {
						alAtLeastOne = htAtLeastOne.get(iParentScanNum);
					}
					alAtLeastOne.add(sRowId);
				}
				Hashtable<String, String> htPeakToFeature = null;
				if( ! htScanToFirstAnnotation.containsKey(iParentScanNum) ) {
					htPeakToFeature = new Hashtable<>();
					htScanToFirstAnnotation.put(iParentScanNum, htPeakToFeature);
				} else {
					htPeakToFeature = htScanToFirstAnnotation.get(iParentScanNum);
				}
				if ( ! htPeakToFeature.containsKey(sRowId) ) {
					htPeakToFeature.put(sRowId, sFeature);
				}					
			}
		}	

		for( Integer iParentScanNum : htScanToFirstAnnotation.keySet() ) {
			Hashtable<String, String> htPeakToFeature = htScanToFirstAnnotation.get(iParentScanNum);
			ArrayList<String> alAtLeastOne = htAtLeastOne.get(iParentScanNum);
			for( String iCurRowId : htPeakToFeature.keySet() ) {
				if ( alAtLeastOne == null || ! alAtLeastOne.contains(iCurRowId) ) { // no selected rows...must do the hacky remove hidden / set invisible
					getGRITSTableDataObject().removeHiddenRow(iParentScanNum, iCurRowId, htPeakToFeature.get(iCurRowId) );
					if ( ! getGRITSTableDataObject().isInvisibleRow(iParentScanNum, iCurRowId) )
						getGRITSTableDataObject().addInvisibleRow(iParentScanNum, iCurRowId);
				} else if ( getGRITSTableDataObject().isInvisibleRow(iParentScanNum, iCurRowId) ) {
					getGRITSTableDataObject().removeInvisibleRow(iParentScanNum, iCurRowId);
				}
			}	
		}
		return isDirty;
	}

	public void finishUpdateHiddenRowsAfterEdit( boolean isDirty ) {
		//					showAllRows();  // disabling on 09/24. Changed implementation of hideRows...and added index maps
		// hacky but have to remove the label overrides for glycan images. Either that or extend ConfigRegistry to ignore invisble.
		//		if( ! isDirty )
		//			return;
		updateRowVisibilityAfterRead();
		updateEventListForVisibility();
		reSort();
		if( parentView != null ) {
			IEclipseContext context = parentView.getParentEditor().getContext();
			IEventBroker eventBroker = context.get(IEventBroker.class);
			eventBroker.send (MSAnnotationMultiPageViewer.EVENT_PARENT_ENTRY_VALUE_MODIFIED, parentView.getEntry());
			//MSAnnotationTableBase.propigateSharedCheckboxChanges(parentView.getEntry());
		}

		if ( isDirty && ! parentView.isDirty() ) {
			parentView.setDirty(true);
		}		
	}
	
	protected void highlightRows() {
		if (getGRITSTableDataObject().getTableData() == null || 
				getGRITSTableDataObject().getTableData().isEmpty()) 
			return;
		try {
			if (((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFilterCols() == null ||
					((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFilterCols().isEmpty()) 
				return;
			Integer filterCol = ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFilterCols().get(0);
			if (filterCol == null || filterCol == -1) // no filter column
				return; 
			for(int i = 0; i < getGRITSTableDataObject().getTableData().size(); i++ ) {
				int iNatIndex = getNatIndexFromSourceIndex(i);
				if( iNatIndex < 0 || iNatIndex >= eventList.size() ) { 
					return;
				}
				GRITSListDataRow eventRowData = (GRITSListDataRow) eventList.get(iNatIndex);					
				GRITSListDataRow backendRowData = (GRITSListDataRow) getGRITSTableDataObject().getTableData().get(i);
				
				if (backendRowData != null && eventRowData != null) {
					Integer filterVal = (Integer)backendRowData.getDataRow().get(filterCol);
					eventRowData.getDataRow().set(filterCol, filterVal);
				}
			}
		} catch( Exception ex ) {
			logger.error("Exception in GRITStable:hightlightRows.", ex);
		}
	}

	@Override
	public void reSort() {
		super.reSort();
		setParentTableNeedsScroll(true);
		performAutoResize();
	}

	public boolean startUpdateHiddenRowsAfterEdit( String _sCustomExtraDataKey, int _iNumTopHits, boolean _bOverrideManual, Object filter, boolean keepExisting, boolean highlightOnly ) {		
		if ( hasColumnGroupHeader() )
			return false;
	
		// DBW 01/25/17:  I had previously disabled this. I don't know why. Performance? 
		//            But if you don't first show all rows, you can't get back previously filtered items.
		showAllRows();  // DBW 01/25/17: when/why did I comment this out before?? previous comment:   "I changed the hideRows method so that it will showAll even none hidden..."
		clearHighlighting(); // to clear previous filter highlights
		int iNumRows =  getBottomDataLayer().getRowCount();
		if ( iNumRows == 0 )
			return false;

		int iParentScanSortIndex = ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getParentNoCol().get(0);
		int iPeakSortIndex = ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0);
		int iScanNumIndex = ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0);

		// store sort state
		SortStatePersistor<GRITSListDataRow> sortStatePersistor = new SortStatePersistor<>(this.sortModel);
		Properties sortProperties = new Properties();
		sortStatePersistor.saveState("beforeFilter", sortProperties);
		
		// first sort by peak id
		this.sortModel.sort(iParentScanSortIndex, SortDirectionEnum.ASC, false);
		this.sortModel.sort(iPeakSortIndex, SortDirectionEnum.ASC, true);
		this.sortModel.sort(iScanNumIndex, SortDirectionEnum.ASC, true);
		int iCNDSortIndex = getColumnIndexForKey(_sCustomExtraDataKey);
		
		boolean isDirty = false;
		Hashtable<Integer, Hashtable<String, String>> htScanToFirstAnnotation = new Hashtable<Integer, Hashtable<String, String>>();
		Hashtable<Integer, ArrayList<String>> htAtLeastOne = new Hashtable<Integer, ArrayList<String>>();
		Hashtable<Integer, Hashtable<String, ArrayList<String>>> currentHiddenRows = copyHiddenRows(getGRITSTableDataObject().getHtHiddenRows());
		
		if( iCNDSortIndex >= 0 ) {
			this.sortModel.sort(iCNDSortIndex, SortDirectionEnum.DESC, true);
		} // else there is no score filter, select all
		
		try {
			isDirty = applyFilter(filter, _bOverrideManual, highlightOnly);
		} catch (Exception e) {
			logger.error("Could not apply the filters", e);
		}
		
		String sLastRowId = null;
		String sLastScan = null;
		int iRowCounter = 0;
		String sLastFeatureId = "";
		int iLastParentScan = -1;
		MSAnnotationTableDataProcessor tdp = (MSAnnotationTableDataProcessor) getTableDataProcessor();
		for( int i = 0; i < iNumRows; i++ ) {
			if ( getBottomDataLayer().getDataValueByPosition( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getParentNoCol().get(0) , i) == null )
				continue;
			if ( getBottomDataLayer().getDataValueByPosition( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0) , i) == null )
				continue;
			if ( getBottomDataLayer().getDataValueByPosition( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().get(0) , i) == null )
				continue;
			// iterating over the nattable bottom layer, so get scan number passing self
			Integer iParentScanNo = getScanNumberForVisibility(this, i);
			Integer iPeakId = ( (Integer) getBottomDataLayer().getDataValueByPosition( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0) , i) );
			Integer iScan = null;
			if( getBottomDataLayer().getDataValueByPosition( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0) , i) != null ) {
				iScan = (Integer) getBottomDataLayer().getDataValueByPosition( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0) , i);
			}
		
			String sRowId = Feature.getRowId(iPeakId, iScan, ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getUsesComplexRowId());
			String sFeatureId = getBottomDataLayer().getDataValueByPosition( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().get(0), i).toString();
			
			boolean bLocked = getGRITSTableDataObject().isLockedPeak(iParentScanNo, sRowId);
			if (bLocked)  // do not change selections if it is locked
				continue;
			boolean bManuallyChanged = getGRITSTableDataObject().isManuallyChangedPeak(iParentScanNo, sRowId);
			if( bManuallyChanged && ! _bOverrideManual ) {
				// need to update filter value even if the peak is manually changed before skipping this peak
				boolean bPrevHidden = getGRITSTableDataObject().isHiddenRow(iParentScanNo, sRowId, sFeatureId);
				if (!bPrevHidden) {
					updateFilterVal(i, true, false);
					Integer filterVal = getFilterVal(i, false);
					if (filterVal != null && filterVal.equals(0)) { // no match
						// see if there are hidden matches
						adjustFilterVal (i, iParentScanNo, sRowId, iScan, false);
					}
				}
				continue;
			}
			
			// reset manually changed
			getGRITSTableDataObject().removeManuallyChangedPeak(iParentScanNo, sRowId);
			
			sLastFeatureId = sFeatureId;
			iLastParentScan = iParentScanNo;
			if( ! sRowId.equals(sLastRowId) || ! iScan.toString().equals(sLastScan) ) { // iPeakId != iLastPeakId ) {
				sLastRowId = sRowId;
				sLastScan = iScan.toString();
				iRowCounter = 0;
			}
			
			boolean bPrevInvisible = getGRITSTableDataObject().isInvisibleRow(iParentScanNo, sRowId);
			boolean bPrevHidden = getGRITSTableDataObject().isHiddenRow(iParentScanNo, sRowId, sFeatureId);
			boolean bCurHidden;
			if (iCNDSortIndex < 0) {  // no score filter, should follow other filter results or keep the selection (if no other filter)
				bCurHidden = bPrevHidden;
			} else { // there is a score filter
				if (filter != null) {
					// there are other filter options
					// need to give priority to the result of filters even if there is a score filter
					bCurHidden = bPrevHidden || ( iRowCounter++ >= _iNumTopHits && _iNumTopHits >= 0 );
				} else // check whether it is one of top iNumTopHits as defined by score filter
					bCurHidden = ( iRowCounter++ >= _iNumTopHits && _iNumTopHits >= 0 );
			}
			if ( ! bCurHidden ) {
				getGRITSTableDataObject().removeHiddenRow(iParentScanNo, sRowId, sFeatureId);
				updateFilterVal(i, true, false);
				Integer filterVal = getFilterVal(i, false);
				if (filterVal != null && filterVal.equals(0)) { // no match
					// see if there are hidden matches
					adjustFilterVal (i, iParentScanNo, sRowId, iScan, false);
				}
				if ( bPrevInvisible || bPrevHidden ) { 
					isDirty = true;
					tdp.addDirtyParentScan(iParentScanNo);
					String sKey = MSAnnotationEntityScroller.getCombinedKeyForLookup(iPeakId, sFeatureId);
					notifyListeners(iParentScanNo, iPeakId, sFeatureId);
				}
			} else if ( bCurHidden ) {
				getGRITSTableDataObject().setHiddenRow(iParentScanNo, sRowId, sFeatureId);
				if ( ! (bPrevHidden || bPrevInvisible) ) {
					isDirty = true;
					tdp.addDirtyParentScan(iParentScanNo);
					String sKey = MSAnnotationEntityScroller.getCombinedKeyForLookup(iPeakId, sFeatureId);
					notifyListeners(iParentScanNo, iPeakId, sFeatureId);
				}
			}
			if( sFeatureId != null ) {
				if ( ! bCurHidden ) {
					ArrayList<String> alAtLeastOne = null;
					if( ! htAtLeastOne.containsKey(iParentScanNo) ) {
						alAtLeastOne = new ArrayList<String>();
						htAtLeastOne.put(iParentScanNo, alAtLeastOne);
					} else {
						alAtLeastOne = htAtLeastOne.get(iParentScanNo);
					}
					alAtLeastOne.add(sRowId);
				}
				Hashtable<String, String> htPeakToFeature = null;
				if( ! htScanToFirstAnnotation.containsKey(iParentScanNo) ) {
					htPeakToFeature = new Hashtable<>();
					htScanToFirstAnnotation.put(iParentScanNo, htPeakToFeature);
				} else {
					htPeakToFeature = htScanToFirstAnnotation.get(iParentScanNo);
				}
				if ( ! htPeakToFeature.containsKey(sRowId) ) {
					htPeakToFeature.put(sRowId, sFeatureId);
				}					
			}
		}		
		for( Integer iParentScanNum : htScanToFirstAnnotation.keySet() ) {
			Hashtable<String, String> htPeakToFeature = htScanToFirstAnnotation.get(iParentScanNum);
			ArrayList<String> alAtLeastOne = htAtLeastOne.get(iParentScanNum);
			// if we check this here and skip, then if everything is filtered out we don't get invisible rows
			// commenting out from here and adding the check inside the for loop
		/*	if( alAtLeastOne == null ) {
				logger.debug("Why: " + iParentScanNum);
				continue;
			}*/
			for( String sRowId : htPeakToFeature.keySet() ) {
				if ( alAtLeastOne == null || ! alAtLeastOne.contains(sRowId) ) { // no selected rows...must do the hacky remove hidden / set invisible
					// need to check if the user selected "keep existing selections"
					if (keepExisting) {
						// check whether this row was selected before the filter, if so keep it selected
						if (wasRowSelected (currentHiddenRows, iParentScanNum, sRowId, htPeakToFeature.get(sRowId))) {
							getGRITSTableDataObject().removeHiddenRow(iParentScanNum, sRowId, htPeakToFeature.get(sRowId) );
						} else { // if not, need to become an invisible row
							getGRITSTableDataObject().removeHiddenRow(iParentScanNum, sRowId, htPeakToFeature.get(sRowId) );
							if ( ! getGRITSTableDataObject().isInvisibleRow(iParentScanNum, sRowId) )
								getGRITSTableDataObject().addInvisibleRow(iParentScanNum, sRowId);
						}
					} else {
						getGRITSTableDataObject().removeHiddenRow(iParentScanNum, sRowId, htPeakToFeature.get(sRowId) );
						if ( ! getGRITSTableDataObject().isInvisibleRow(iParentScanNum, sRowId) )
							getGRITSTableDataObject().addInvisibleRow(iParentScanNum, sRowId);
					}
				} else if ( getGRITSTableDataObject().isInvisibleRow(iParentScanNum, sRowId) ) {
					getGRITSTableDataObject().removeInvisibleRow(iParentScanNum, sRowId);
				}
			}	
		}
		
		sortStatePersistor.loadState("beforeFilter", sortProperties);

		return isDirty;
	}

	

	protected void clearHighlighting() {
		if (getGRITSTableDataObject().getTableData() == null || 
				getGRITSTableDataObject().getTableData().isEmpty()) 
			return;
		try {
			if (((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFilterCols() == null ||
					((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFilterCols().isEmpty()) 
				return;
			Integer filterCol = ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFilterCols().get(0);
			if (filterCol == null || filterCol == -1) // no filter column
				return; 
			for(int i = 0; i < getGRITSTableDataObject().getTableData().size(); i++ ) {
				int iNatIndex = getNatIndexFromSourceIndex(i);
				if( iNatIndex < 0 || iNatIndex >= eventList.size() ) { 
					return;
				}
				GRITSListDataRow eventRowData = (GRITSListDataRow) eventList.get(iNatIndex);					
				GRITSListDataRow backendRowData = (GRITSListDataRow) getGRITSTableDataObject().getTableData().get(i);
				
				if (backendRowData != null && eventRowData != null) {
					backendRowData.getDataRow().set(filterCol, -1);
					eventRowData.getDataRow().set(filterCol, -1);
				}
			}
		} catch( Exception ex ) {
			logger.error("Exception in GRITStable:clearHightlighing.", ex);
		}
		
	}

	private Hashtable<Integer, Hashtable<String, ArrayList<String>>> copyHiddenRows(
			Hashtable<Integer, Hashtable<String, ArrayList<String>>> htHiddenRows) {
		Hashtable<Integer, Hashtable<String, ArrayList<String>>> copyOfHiddenRows = new Hashtable<>();
		for (Integer parentScan: htHiddenRows.keySet()) {
			Hashtable<String, ArrayList<String>> rowToHiddenPeaks = htHiddenRows.get(parentScan);
			Hashtable<String, ArrayList<String>> copyOfRowToHiddenPeaks = new Hashtable<>();
			for (String rowId: rowToHiddenPeaks.keySet()) {
				ArrayList<String> secondaryIds = rowToHiddenPeaks.get(rowId);
				ArrayList<String> copyOfSecondaryIds = new ArrayList<>();
				copyOfSecondaryIds.addAll(secondaryIds);
				copyOfRowToHiddenPeaks.put(rowId, copyOfSecondaryIds);
			}
			copyOfHiddenRows.put(parentScan, copyOfRowToHiddenPeaks);
		}
		return copyOfHiddenRows;
	}

	private boolean wasRowSelected(Hashtable<Integer, Hashtable<String, ArrayList<String>>> currentHiddenRows,
			Integer _iScanNum, String _iRowId, String _sSecondaryId) {
		Hashtable<String, ArrayList<String>> htRowToHiddenFeatures = null;
    	if( ! currentHiddenRows.containsKey(_iScanNum) ) {
    		return true;
    	} else {
    		htRowToHiddenFeatures = currentHiddenRows.get(_iScanNum);
    	}
    	ArrayList<String> alHiddenAnnotations = null;
    	if ( htRowToHiddenFeatures.containsKey(_iRowId) )
    		alHiddenAnnotations = htRowToHiddenFeatures.get(_iRowId);
    	else {
    		return true;
    	}	
		return !alHiddenAnnotations.contains(_sSecondaryId);		
	}
	
	/**
	 * Try to find the parent table index for the row whose selection has been changed in the subset table
	 * 
	 * @param sFeature
	 * @param iPeakId
	 * @return rowIndex 
	 */
	private int locateRowInParentTable(String sFeature,  Integer iPeakId) {
		ArrayList<GRITSListDataRow> rows = getGRITSTableDataObject().getTableData();
		for(int i = 0; i < rows.size(); i++ ) {		
			GRITSListDataRow row = rows.get(i);
			if ( row.getDataRow().get(((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().get(0)) == null )
				continue;
			if ( row.getDataRow().get(((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0)) == null )
				continue;
			Integer peakId = (Integer) row.getDataRow().get(((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0));
			String feature = row.getDataRow().get(((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().get(0)).toString();
			
			if (sFeature.equals(feature) && peakId.equals(iPeakId))
				return i;
		}
		
		return -1;
	}
	
	/**
	 * This method marks the given row as "matching the filter and selected" or "matching the filter but not selected"
	 * Filter column value is either incremented or decremented by 1 based on the boolean argument
	 * 
	 * @param rowIndex is the index of the row in the underlying data layer/or the underlying object model
	 * @param increment whether to increase or decrease the filter value
	 * @param absoluteIndex whether the index is absolute or needs to be converted to the underlying object model row index
	 */
	private void updateFilterVal(int rowIndex, boolean increment, boolean absoluteIndex) {
		if (((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFilterCols() == null ||
				((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFilterCols().isEmpty())
			return;
		if (rowIndex == -1)
			return;
		Integer filterCol = ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFilterCols().get(0);
		if (filterCol == null || filterCol == -1) // no filter column
			return; 
		if (filterCol != -1) {
			int j = rowIndex;
			if (!absoluteIndex) {
				// need to convert the index
				GRITSListDataRow backendRowData = this.bodyDataProvider.getGRITSListDataRow(rowIndex);
				j = this.getSourceIndexFromRowId(backendRowData.getId());
			}
			GRITSListDataRow rowData = (GRITSListDataRow) getGRITSTableDataObject().getTableData().get(j);
			Integer filterVal = (Integer) rowData.getDataRow().get(filterCol);
			if (filterVal != null) {
				if (increment && filterVal >= 10) // only increment if it is already a match
					rowData.getDataRow().set(filterCol, 11);
				else if (!increment && filterVal == 1)
					rowData.getDataRow().set(filterCol, 0);
				else if (!increment && filterVal == 11)
					rowData.getDataRow().set(filterCol, 10);
			}
		}
	}
	
	private void adjustFilterVal(int rowIndex, Integer iParentScanNo, String rowId, Integer iScan, boolean absolute) {
		int iNumRows =  getGRITSTableDataObject().getTableData().size();
		boolean match = false;
		for( int i = 0; i < iNumRows; i++ ) {
			if (getGRITSTableDataObject().getTableData().get(i).getDataRow().get(((MSAnnotationTableDataObject) getGRITSTableDataObject()).getParentNoCol().get(0)) == null)
				continue;
			if ( getGRITSTableDataObject().getTableData().get(i).getDataRow().get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0)) == null)
				continue;
			if ( getGRITSTableDataObject().getTableData().get(i).getDataRow().get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().get(0)) == null)
				continue;
			if ( getGRITSTableDataObject().getTableData().get(i).getDataRow().get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFilterCols().get(0)) == null)
				continue;
			// iterating over the nattable bottom layer, so get scan number passing self
			Integer iParentScanNo2 = getScanNumberForVisibility(this, i);
			Integer iPeakId2 = ((Integer) getGRITSTableDataObject().getTableData().get(i).getDataRow().get(((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0)));
			String sFeatureId = getGRITSTableDataObject().getTableData().get(i).getDataRow().get(((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().get(0)).toString();
			boolean isHidden = getGRITSTableDataObject().isHiddenRow(iParentScanNo2, iPeakId2.toString(), sFeatureId);
			Integer iScan2 = null;
			if( getGRITSTableDataObject().getTableData().get(i).getDataRow().get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0)) != null ) {
				iScan2 = (Integer) getGRITSTableDataObject().getTableData().get(i).getDataRow().get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0));
			}
			Integer filterValue = getFilterVal(i, true);
			String sRowId2 = Feature.getRowId(iPeakId2, iScan2, ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getUsesComplexRowId());
//					if (iParentScanNo.equals(iParentScanNo2) && iPeakId2.toString().equals(peakId) && iScan.equals(iScan2)) {
			if (iParentScanNo.equals(iParentScanNo2) && rowId.equals(sRowId2)) {
				if (isHidden) {
					// candidate for the same peak
					// check its filter value, if not 0, adjust the rowIndex's filterValue
					if (filterValue != null && filterValue >= 10) {  // hidden match
						match = true;
					}
				}
			} 
		}
		if (match)
			setFilterVal(rowIndex, 1, absolute);
	}
	
	Integer getFilterVal (int rowIndex, boolean absoluteIndex) {
		if (((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFilterCols() == null ||
				((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFilterCols().isEmpty())
			return -1;
		if (rowIndex == -1)
			return -1;
		Integer filterCol = ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFilterCols().get(0);
		if (filterCol == null || filterCol == -1) // no filter column
			return -1; 
		if (filterCol != -1) {
			int j = rowIndex;
			if (!absoluteIndex) {
				// need to convert the index
				GRITSListDataRow backendRowData = this.bodyDataProvider.getGRITSListDataRow(rowIndex);
				j = this.getSourceIndexFromRowId(backendRowData.getId());
			}
			GRITSListDataRow rowData = (GRITSListDataRow) getGRITSTableDataObject().getTableData().get(j);
			Integer filterVal = (Integer) rowData.getDataRow().get(filterCol);
			return filterVal;
		}
		return -1;
	}
	
	void setFilterVal (int rowIndex, int newValue, boolean absoluteIndex) {
		if (((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFilterCols() == null ||
				((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFilterCols().isEmpty())
			return;
		if (rowIndex == -1)
			return;
		Integer filterCol = ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFilterCols().get(0);
		if (filterCol == null || filterCol == -1) // no filter column
			return; 
		if (filterCol != -1) {
			// need to convert the index
			int j = rowIndex;
			if (!absoluteIndex) {
				// need to convert the index
				GRITSListDataRow backendRowData = this.bodyDataProvider.getGRITSListDataRow(rowIndex);
				j = this.getSourceIndexFromRowId(backendRowData.getId());
			}
			GRITSListDataRow rowData = (GRITSListDataRow) getGRITSTableDataObject().getTableData().get(j);
			rowData.getDataRow().set(filterCol, newValue);
		}		
	}

	/**
	 * apply the filter on the table and make necessary changes to the selection status for the rows
	 * according to filter match results. if overrideManual is false, it will not change the manual selections even 
	 * if the row fails to match the filter
	 * if highlightOnly is true, current selections will not be changed but only marked to be highlighted
	 *
	 * returns whether the table is modified or not
	 * 
	 * @param filter
	 * @param overrideManual
	 * @param highlightOnly
	 * @return true if filter caused selection changes on the table, false if nothing has changed
	 * @throws Exception
	 */
	protected boolean applyFilter(Object filter, boolean overrideManual, boolean highlightOnly) throws Exception {
		// nothing to do for generic implementation
		return false;
	}
	
	/**
	 * Determines if a row is supposed to be hidden based on row settings (see the pop-up menu for the row).
	 * This differs from being hidden based on user selection (the selection table below the main table).
	 * 
	 * @param iRowNum, the row number of the GRITSTableDataObject.
	 * @return true if user wants to hide this row, false otherwise.
	 */
	protected boolean isHiddenTableRow(int iRowNum) {
		boolean bHidden = (hideUnannotated() && getMyTableDataObject().isUnannotatedRow(iRowNum));
		return bHidden;
	}

	/**
	 * After reading the data and filling the GRITSTableDataObject, hide the rows that are either 
	 * de-selected or to be hidden based on user-setting in the row pop-up menu.
	 */
	public void updateRowVisibilityAfterRead() {
		ArrayList<Integer> alHiddenRows = new ArrayList<Integer>();
		int iNumRows = getGRITSTableDataObject().getTableData().size();
		//		int iSortIndex = -1;
		boolean bHasScan = false;
		for( int i = 0; i < iNumRows; i++ ) {
			boolean bHidden = isHiddenTableRow(i);
			if( ! bHidden ){
				if ( ! bHasScan && ! ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().isEmpty() && 
						getGRITSTableDataObject().getTableData().get(i).getDataRow()
						.get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0) ) != null  ) {
					//					iSortIndex = ((MSAnnotationTableDataObject) getSimDataObject()).getScanNoCols().get(0);
					bHasScan = true;
				}			
				if ( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().isEmpty() || 
						((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().isEmpty() )
					continue;
				if ( getGRITSTableDataObject().getTableData().get(i).getDataRow()
						.get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0) ) == null )
					continue;
				if ( getGRITSTableDataObject().getTableData().get(i).getDataRow().get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().get(0) ) == null )
					continue;
				// iterating over the table data object, so get scan number passing the table data object				
				Integer iParentScanNum = getScanNumberForVisibility( getMyTableDataObject(), i);
				
				Integer iPeakId = (Integer) getGRITSTableDataObject().getTableData().get(i).getDataRow()
						.get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0));
				String sFeatureId = (String) getGRITSTableDataObject().getTableData().get(i).getDataRow().get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().get(0));
				Integer iScan = null;
				if ( ! ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().isEmpty() &&
						getGRITSTableDataObject().getTableData().get(i).getDataRow().get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0) ) != null ) {
					iScan = (Integer) getGRITSTableDataObject().getTableData().get(i).getDataRow()
							.get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0));
				}
				String sRowId = Feature.getRowId(iPeakId, iScan, ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getUsesComplexRowId());
				bHidden = getGRITSTableDataObject().isHiddenRow(iParentScanNum, sRowId, sFeatureId);
			}
			if ( bHidden ) {
				alHiddenRows.add(i);
			}				
		}
		hideRows(alHiddenRows);
//		reSort();
		//		if( iSortIndex != -1 ) {
		//			this.sortModel.sort(iSortIndex, SortDirectionEnum.ASC, false);
		//		} else {
		performAutoResize();
		//		}
	}

	@Override
	protected GRITSHeaderMenuConfiguration getNewHeaderMenuConfiguration() {
		return new MSAnnotationHeaderMenuConfiguration(this);
	}
	
	@Override
	public void updatePreferenceSettingsFromCurrentView() {
		MSAnnotationViewerPreference pref = (MSAnnotationViewerPreference) getPreference();
		pref.setHideUnannotatedPeaks(hideUnannotated());
		super.updatePreferenceSettingsFromCurrentView();
	}

	@Override
	public boolean updateViewFromPreferenceSettings() {
		MSAnnotationViewerPreference pref = (MSAnnotationViewerPreference) getPreference();
		setHideUnannotated( pref.isHideUnannotatedPeaks() );
		return super.updateViewFromPreferenceSettings();
	}

	protected void createCheckBoxPainters() {
		// TODO: generic implementation?
	}

	/**
	 * @param _sKey : comprised of "peak_id:feature_id"
	 * This method toggles the checkbox in the "selection" table of the StructuredAnnotation viewer. 
	 * The method iterates over the rows in the selection table, creating the "peak_id:feature_id" key for each row.
	 * If the key for the row matches the key passed in _sKey, then it toggles the selection.
	 * Uses the static method MSAnnotationEntityScroller.getCombinedKeyForLookup(...) to create the row keys
	 * 
	 */
	public void toggleSubsetTableRowsForClickedItem(String _sKey) {
		int iNumRows = getBottomDataLayer().getRowCount();
		if (iNumRows == 0)
			return;
		for (int i = 0; i < iNumRows; i++) {
			if (getBottomDataLayer().getDataValueByPosition(
					((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0), i) == null)
				continue;
			if (getBottomDataLayer().getDataValueByPosition(
					((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().get(0), i) == null)
				continue;
			Integer iPeakId = ((Integer) getBottomDataLayer().getDataValueByPosition(
					((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0), i));
			String sId = getBottomDataLayer().getDataValueByPosition(
					((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().get(0), i).toString();
			String sCompareTo = MSAnnotationEntityScroller.getCombinedKeyForLookup(iPeakId, sId);
			if (_sKey.equals(sCompareTo)) {
				// toggle this row
				boolean bCurSelected = ((Boolean) getBottomDataLayer().getDataValueByPosition(0, i)).booleanValue();
				getBottomDataLayer().getDataProvider().setDataValue(0, i, Boolean.valueOf(!bCurSelected));
				break;
			}
		}
		
	}
	
	protected void notifyListeners( Integer iScanNum, Integer iPeakId, String sFeature  ) {
		if( parentTable != null && parentTable.getParentMultiPageViewer() != null ) {
			((MSAnnotationTableDataProcessor) getTableDataProcessor()).setPropertyScanNum(iScanNum);
			ScanFeatures scanFeatures = ((MSAnnotationTableDataProcessor) getTableDataProcessor()).getScanFeatures(iScanNum);
			Feature matchedFeature = null;
			for( Feature feature : scanFeatures.getFeatures() ) {
				if( feature.getId().equals(sFeature) ) {
					matchedFeature = feature;
				}
			}
			if( matchedFeature == null ) {
				logger.error("Unable to find feature: " + sFeature);
				return;
			}
			Annotation annotation = AnnotationRowExtraction.getAnnotation(((MSAnnotationTableDataProcessor) getTableDataProcessor()).getGRITSdata(), matchedFeature.getAnnotationId());
			MSAnnotationTableDataChangedMessage message = 
					new MSAnnotationTableDataChangedMessage(this, iPeakId, matchedFeature, annotation);

			
			IEclipseContext context = parentTable.getParentMultiPageViewer().getContext();
			IEventBroker eventBroker = context.get(IEventBroker.class);
			eventBroker.send(MSAnnotationMultiPageViewer.EVENT_PARENT_ENTRY_VALUE_MODIFIED, message);
		}
	}
	
	@Override
	protected void addConfigurations() {
		// do not change the order of configurations here!
		this.addConfiguration(getGRITSNatTableStyleConfiguration());
		if (addHeaderListeners()) { // only add row header listeners to main table
			this.addConfiguration(getNewHeaderMenuConfiguration());
		} else  // subset table
			this.addConfiguration(getNewSubsetTableHeaderMenuConfiguration());
		this.addConfiguration(getBodyMenuConfiguration());
		this.addConfiguration(getSingleClickConfiguration());
		this.addConfiguration(getUIBindingConfiguration());
	}
	
	protected IConfiguration getSingleClickConfiguration() {
		return new GRITSSingleClickConfiguration( getGRITSTableDataObject().getTableHeader().size() > 1);
	}

	protected IConfiguration getNewSubsetTableHeaderMenuConfiguration() {
		return new MSAnnotationSelectionTableHeaderMenuConfiguration(this);
	}

	
	/**
	 * This is used only for the subtable to lock selections
	 */
	public void lockSelection() {
		Integer iParentScanNum = getParentScanNumberFromTable(parentTable.getParentView(), 0);
		Integer iPeakId = ( (Integer) getBottomDataLayer().getDataValueByPosition(
				((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0), 0) );
		Integer iScan = null;
		if (getMyTableDataObject().getScanNoCols() != null && ! getMyTableDataObject().getScanNoCols().isEmpty()) {
			iScan =  (Integer) getBottomDataLayer().getDataValueByPosition( 
				((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0), 0) ;
		}
		if (iParentScanNum != null && iPeakId != null) {
			String sRowId = Feature.getRowId(iPeakId, iScan, ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getUsesComplexRowId());
			if (!parentTable.getGRITSTableDataObject().isLockedPeak(iParentScanNum, sRowId)) {
				parentTable.getGRITSTableDataObject().addLockedPeak(iParentScanNum, sRowId);
				String sFeatureId = (String) getBottomDataLayer().getDataValueByPosition(
						((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().get(0), 0);
				parentTable.notifyListeners(iParentScanNum, iPeakId, sFeatureId);
				saveLockUnlock(iParentScanNum);
			}
		}
	}
	
	protected Integer getParentScanNumberFromTable (MassSpecTableBase parentView, int row) {
		Integer iParentScanNum = -1;
		MSAnnotationMultiPageViewer viewer = MSAnnotationMultiPageViewer.getActiveViewerForEntry(parentView.getParentEditor().getContext(), parentView.getEntry());
		MSAnnotationTableDataObject tdo = ((MSAnnotationTableDataObject) getGRITSTableDataObject());
		if (viewer.getDetailsView() != null) {
			//MSn
			if ( tdo.getParentNoCol() != null && ! tdo.getParentNoCol().isEmpty()) {
				if( tdo.getTableData().get(row).getDataRow().get( tdo.getParentNoCol().get(0)) != null ) {
					iParentScanNum = (Integer) tdo.getTableData().get(row).getDataRow().get( tdo.getParentNoCol().get(0));
				}
			}
		}
		else 
			iParentScanNum = getScanNumberForVisibility(this, 0);
		return iParentScanNum;
	}
	
	/**
	 * method to inform the correct viewer about the lock/unlock state changes for a peak
	 * @param parentScanNo parent scan number of the peak
	 */
	private void saveLockUnlock(Integer parentScanNo) {
		MSAnnotationTableDataProcessor tdp = (MSAnnotationTableDataProcessor) parentTable.getTableDataProcessor();
		tdp.addDirtyParentScan(parentScanNo);
		parentTable.getParentView().setDirty(true);
	}

	/**
	 * This is used only for the subtable to remove a lock
	 * 
	 */
	public void unlockSelection() {
		Integer iParentScanNum = getParentScanNumberFromTable(parentTable.getParentView(), 0);
		Integer iPeakId = ( (Integer) getBottomDataLayer().getDataValueByPosition(
				((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0), 0) );
		if (iParentScanNum != null && iPeakId != null) {
			Integer iScan = null;
			if (getMyTableDataObject().getScanNoCols() != null && ! getMyTableDataObject().getScanNoCols().isEmpty()) {
				iScan =  (Integer) getBottomDataLayer().getDataValueByPosition( 
					((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0), 0) ;
			}
			String sRowId = Feature.getRowId(iPeakId, iScan, ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getUsesComplexRowId());
			if (parentTable.getGRITSTableDataObject().isLockedPeak(iParentScanNum, sRowId)) {
				parentTable.getGRITSTableDataObject().removeLockedPeak(iParentScanNum, sRowId);	
				String sFeatureId = (String) getBottomDataLayer().getDataValueByPosition(
						((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().get(0), 0);
				parentTable.notifyListeners(iParentScanNum, iPeakId, sFeatureId);
				saveLockUnlock(iParentScanNum);
			}
		}
	}

	public void hideInvisibleRows() {
		setHideInvisible(true);
		updateRowVisibilityForInvisibleRows();
		updateEventListForVisibility();
		reSort();
	}

	public void showInvisibleRows() {
		setHideInvisible(false);
		updateRowVisibilityForInvisibleRows();
		updateEventListForVisibility();
		reSort();
	}

	/**
	 * this method hides/shows all rows which are in invisible (rows with no cartoon) list (either because they have no annotations, 
	 * or because all selections are filtered out)
	 * 
	 * hide when bHideInvisible is true, show when bHideInvisible is false
	 */
	private void updateRowVisibilityForInvisibleRows() {
		ArrayList<Integer> alHiddenRows = new ArrayList<Integer>();
		int iNumRows = getGRITSTableDataObject().getTableData().size();
		for( int i = 0; i < iNumRows; i++ ) {
			if ( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().isEmpty() || 
					((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().isEmpty() )
				continue;
			if ( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().isEmpty() || 
					((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().isEmpty() )
				continue;
			if ( getGRITSTableDataObject().getTableData().get(i).getDataRow()
					.get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0) ) == null )
				continue;
			// iterating over the table data object, so get scan number passing the table data object
			Integer iParentScanNum = getScanNumberForVisibility( getMyTableDataObject(), i);
			Integer iPeakId = (Integer) getGRITSTableDataObject().getTableData().get(i).getDataRow()
					.get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getPeakIdCols().get(0));
			Integer iScan  = null;
			if ( getGRITSTableDataObject().getTableData().get(i).getDataRow()
					.get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0) ) != null ) {
				iScan = (Integer) getGRITSTableDataObject().getTableData().get(i).getDataRow()
						.get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getScanNoCols().get(0));
			}
			String sRowId = Feature.getRowId(iPeakId, iScan, ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getUsesComplexRowId());
			boolean bHidden = (getHideInvisible() && getMyTableDataObject().isInvisibleRow(iParentScanNum, sRowId));
			if(!bHidden){	
				if ( getGRITSTableDataObject().getTableData().get(i).getDataRow().get( ((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().get(0) ) == null )
					continue;
				String sFeatureId = (String) getGRITSTableDataObject().getTableData().get(i).getDataRow().get(((MSAnnotationTableDataObject) getGRITSTableDataObject()).getFeatureIdCols().get(0));
				bHidden = getGRITSTableDataObject().isHiddenRow(iParentScanNum, sRowId, sFeatureId);
			}
			if (bHidden) {
				alHiddenRows.add(i);
			}				
		}
		hideRows(alHiddenRows);
		performAutoResize();
	}
	
	public MSAnnotationTable getParentTable() {
		return parentTable;
	}
}
