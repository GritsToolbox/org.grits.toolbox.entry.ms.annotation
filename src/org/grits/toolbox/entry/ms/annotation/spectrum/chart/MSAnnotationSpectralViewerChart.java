/*
 * ProteinFDRChart.java
 *
 * Created on December 21, 2007, 1:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.grits.toolbox.entry.ms.annotation.spectrum.chart;

import java.awt.Color;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.Plot;

import org.grits.toolbox.display.control.spectrum.chart.GRITSSpectralViewerChart;

/**
 *
 * @author brentw
 */
public class MSAnnotationSpectralViewerChart extends GRITSSpectralViewerChart {
    protected String sID;
    protected Double dObsMass = null;
  
    public MSAnnotationSpectralViewerChart( String _sDescription, int _iScanNum, 
    		int _iMSLevel, boolean _bIsProfile, boolean _bVertLabels, String sID, Double _dObsMass ){
        super( _sDescription, _iScanNum, _iMSLevel, _bIsProfile, _bVertLabels );
        this.sID = sID;
        this.dObsMass = _dObsMass;
    }
   
    @Override
    protected LegendItemCollection createLegendItems() {
        LegendItemCollection legenditemcollection = new LegendItemCollection();
    	if ( sID == null ) 
    		return legenditemcollection;
        LegendItem legenditem = new LegendItem("Annotation: " + sID, "-", null, null, Plot.DEFAULT_LEGEND_ITEM_BOX, Color.white);
        legenditemcollection.add(legenditem);
        return legenditemcollection;
    }  
        
 }
