package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.datamodel.ms.annotation.preference.MSAnnotationViewerPreference;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.MSAnnotationTableDataObject;
import org.grits.toolbox.display.control.spectrum.chart.GRITSSpectralViewerChart;
import org.grits.toolbox.display.control.spectrum.chart.GRITSSpectralViewerData;
import org.grits.toolbox.display.control.spectrum.datamodel.MSIonData;
import org.grits.toolbox.display.control.table.datamodel.GRITSListDataRow;
import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.spectrum.chart.MSAnnotationSpectralViewerChart;
import org.grits.toolbox.entry.ms.exceptions.MSException;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecMultiPageViewer;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecSpectraControlPanelView;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecSpectraView;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.Method;


public class MSAnnotationSpectraView extends MassSpecSpectraView {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSAnnotationSpectraView.class);
	protected String sID = null;

	@Inject
	public MSAnnotationSpectraView( Entry entry ) {
		super(entry);
	}

	public void setGlycanID(String sGlycanID) {
		this.sID = sGlycanID;
	}

	@Override
	protected GRITSSpectralViewerData initSpectralViewerData() throws MSException {
		GRITSSpectralViewerData svd = super.initSpectralViewerData();
		try {
			ArrayList<MSIonData> alAnnotatedPeaks = svd.getAnnotatedPeaks(); // reuse the already annotated ones
			HashMap<Double, List<Object>> htAnnotatedLabels = svd.getAnnotatedPeakLabels();

			ArrayList<MSIonData> alUnAnnotatedPeaks = new ArrayList<>();
			HashMap<Double, List<Object>> htUnAnnotatedLabels = new HashMap<>();
			svd.setUnAnnotatedPeaks(alUnAnnotatedPeaks);
			svd.setUnAnnotatedPeakLabels(htUnAnnotatedLabels);

			if( getPeakListTableProcessor() == null ) 
				return svd;
			MSAnnotationTableDataObject msTable = (MSAnnotationTableDataObject) getPeakListTableProcessor().getSimianTableDataObject();
			if( msTable.getMzCols() == null || msTable.getMzCols().isEmpty() ) 
				return svd;
			
			// first load the annotated peaks
			for( int i = 0; i < msTable.getTableData().size(); i++  ) {			
				GRITSListDataRow alRow = msTable.getTableData().get(i);
				Object iId = null;
				Double dMz = null;
				Double dIntensity = null;
				String sSequence = null;
				Integer iPeakId = null;
				Integer iScanNum = -1;
				Integer iParentScan = -1;
				String iRowId = null;
				try {
					dMz = (Double) alRow.getDataRow().get(msTable.getMzCols().get(0));
					dIntensity = (Double) alRow.getDataRow().get(msTable.getPeakIntensityCols().get(0));
					if( msTable.getFeatureIdCols() != null && ! msTable.getFeatureIdCols().isEmpty() ) {
						iId = alRow.getDataRow().get(msTable.getFeatureIdCols().get(0));
					}
					if( msTable.getSequenceCols() != null && ! msTable.getSequenceCols().isEmpty() ) {
						sSequence = (String) alRow.getDataRow().get(msTable.getSequenceCols().get(0));
					}
					iPeakId = (Integer) alRow.getDataRow().get(msTable.getPeakIdCols().get(0));
					iScanNum = null;
					if( msTable.getScanNoCols() != null && ! msTable.getScanNoCols().isEmpty() ) {
						if( alRow.getDataRow().get(msTable.getScanNoCols().get(0)) != null ) {
							iScanNum = (Integer) alRow.getDataRow().get(msTable.getScanNoCols().get(0));
						}
					}
					if( iPeakId != null ) {
						iRowId = Feature.getRowId(iPeakId, iScanNum, msTable.getUsesComplexRowId());
					}
					
					if( msTable.getParentNoCol() != null && ! msTable.getParentNoCol().isEmpty() ) {
						if( alRow.getDataRow().get(msTable.getParentNoCol().get(0)) != null ) {
							iParentScan = (Integer) alRow.getDataRow().get(msTable.getParentNoCol().get(0));
						}
					}

				} catch( Exception ex ) {
					logger.error("Invalid number format for m/z or intensity in table", ex);
				}
				if( dMz == null || dIntensity == null ) {
					continue;
				}
				int iPScan = iParentScan;
				MSAnnotationTableDataProcessor msap = (MSAnnotationTableDataProcessor) getPeakListTableProcessor(); 
				if( msap.getMethod().getMsType().equals(Method.MS_TYPE_INFUSION) ) {
					iPScan = msap.getGRITSdata().getFirstMS1Scan();
				}
				if( iId == null || iPeakId == null || iRowId == null ||
					msTable.isHiddenRow(iPScan, iRowId, iId.toString()) || 
						 msTable.isInvisibleRow(iPScan, iRowId)) {
						continue;
				}
				MSIonData msData = new MSIonData(dMz, dIntensity);
				ArrayList<MSIonData> alPeaks = alAnnotatedPeaks;
				HashMap<Double, List<Object>> htPeakLabels = htAnnotatedLabels;
				alPeaks.add(msData);						
				List<Object> al = null;
				if( htPeakLabels.containsKey(dMz) ) {
					al = htPeakLabels.get(dMz);
				} else {
					al = new ArrayList<>();
					htPeakLabels.put(dMz, al);
				}
				Object sStartLabel = null;
				Object sLabel = getPeakLabel(dMz, iId, sStartLabel, sSequence);		
				if( ! al.contains(sLabel) ) {
					al.add(sLabel);
				}
			}
			for( int i = 0; i < msTable.getTableData().size(); i++  ) {			
				GRITSListDataRow alRow = msTable.getTableData().get(i);
				Double dMz = null;
				Double dIntensity = null;
				Object iId = null;
				try {
					dMz = (Double) alRow.getDataRow().get(msTable.getMzCols().get(0));
					dIntensity = (Double) alRow.getDataRow().get(msTable.getPeakIntensityCols().get(0));
					if( msTable.getFeatureIdCols() != null && ! msTable.getFeatureIdCols().isEmpty() ) {
						iId = alRow.getDataRow().get(msTable.getFeatureIdCols().get(0));
					}
				} catch( Exception ex ) {
					logger.error("Invalid number format for m/z or intensity in table", ex);
				}
				if( dMz == null || dIntensity == null ) {
					continue;
				}
				if( htAnnotatedLabels.containsKey(dMz) || htUnAnnotatedLabels.containsKey(dMz) ) { // had been marked annotated...ignore unannotated row
					continue;
				}
				MSIonData msData = new MSIonData(dMz, dIntensity);
				ArrayList<MSIonData> alPeaks = alUnAnnotatedPeaks;
				HashMap<Double, List<Object>> htPeakLabels = htUnAnnotatedLabels;
				if( alPeaks.contains(msData) ) { // prevent duplicates!
					continue;
				}
				alPeaks.add(msData);						
				List<Object> al = null;
				if( htPeakLabels.containsKey(dMz) ) {
					al = htPeakLabels.get(dMz);
				} else {
					al = new ArrayList<>();
					htPeakLabels.put(dMz, al);
				}
				Object sStartLabel = null;
				Object sLabel = getPeakLabel(dMz, null, sStartLabel, null);		
				if( iId != null ) {
					sLabel = "User de-selected: " + sLabel;
				}
				if( ! al.contains(sLabel) ) {
					al.add(sLabel);
				}
			}						
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return svd;
	}

	protected Object getPeakLabel( Double dMz, Object oFeatureId, Object oPrevLabel, String sFeatureSeq ) {
		DecimalFormat df = new DecimalFormat("0.00");
		if( oFeatureId == null ) 
			return df.format(dMz);
		String sLabel = df.format(dMz) + ", ID: " + oFeatureId.toString();
		return sLabel;
	}

	@Override
	protected GRITSSpectralViewerChart getNewSpectralViewerChart() {
		return new MSAnnotationSpectralViewerChart( this.sDescription, this.iScanNum, 
				this.iMSLevel, ! this.bIsCentroid, true, this.sID, this.dMz );
	}

	@Override
	protected void initializeChartPlot() {
		super.initializeChartPlot();
		MSAnnotationViewerPreference pref = (MSAnnotationViewerPreference) getScanListTableProcessor().getSimianTableDataObject().getTablePreferences();
		MSAnnotationSpectraControlPanelView mscp = (MSAnnotationSpectraControlPanelView) controlPanel;
		mscp.getUnAnnotatedPeaks().setSelection(pref.isShowUnannotated());
		if( mscp.getUnAnnotatedPeaks().getSelection() ) {
			mscp.getUnAnnotatedPeaks().setEnabled(true);
			mscp.getUnAnnotatedPeakLabels().setEnabled(true);
			mscp.getUnAnnotatedPeakLabels().setSelection(pref.isShowUnannotatedLabels());			
		} 
	}
	
	@Override
	public void updateChartPlot() {
		MSAnnotationSpectraControlPanelView cp = (MSAnnotationSpectraControlPanelView) controlPanel;

		svChart.updateChart( cp.showRaw(), cp.showPickedPeaks(), cp.showPickedPeakLabels(), 
				   cp.showAnnotatedPeaks(), cp.showAnnotatedPeakLabels(),
				   cp.showUnAnnotatedPeaks(), cp.showUnAnnotatedPeakLabels());
		updatePrefs();
	}
	
	@Override
	protected void updatePrefs() {
		super.updatePrefs();
		MSAnnotationViewerPreference pref = (MSAnnotationViewerPreference) getScanListTableProcessor().getSimianTableDataObject().getTablePreferences();
		pref.setShowUnannotated( ((MSAnnotationSpectraControlPanelView) controlPanel).getUnAnnotatedPeaks().getSelection() );
		pref.setShowUnannotatedLabels( ((MSAnnotationSpectraControlPanelView) controlPanel).getUnAnnotatedPeakLabels().getSelection()  );
		pref.writePreference();
	}
	
	@Override
	protected MassSpecSpectraControlPanelView getNewSpectraControlPanel() {
		return new MSAnnotationSpectraControlPanelView(this);
	}

	@Override
	public void createChart(Composite parent) throws MSException {
		super.createChart(parent);
	}

	public void createView() throws MSException {
		createChart(this.parent);
	}
	
	@Optional
	@Inject
	void refreshCheckboxes(
			@UIEventTopic(MassSpecMultiPageViewer.EVENT_PARENT_ENTRY_VALUE_MODIFIED) Entry parentEntry) {
		if( getEntry().getParent() != null && 
			getEntry().getParent().equals(parentEntry) ) {
			MSAnnotationSpectraControlPanelView cp = (MSAnnotationSpectraControlPanelView) getControlPanel();
			cp.updateView();
		}
	}
}
