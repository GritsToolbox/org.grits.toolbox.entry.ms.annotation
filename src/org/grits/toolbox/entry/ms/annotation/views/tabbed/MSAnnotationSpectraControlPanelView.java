package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.grits.toolbox.display.control.spectrum.chart.GRITSSpectralViewerData;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecSpectraControlPanelView;
import org.grits.toolbox.entry.ms.views.tabbed.MassSpecSpectraView;

public class MSAnnotationSpectraControlPanelView extends MassSpecSpectraControlPanelView {

	protected Button cbUnAnnotatedPeaks = null;
	protected Button cbUnAnnotatedPeakLabels = null;

	public MSAnnotationSpectraControlPanelView(MassSpecSpectraView parentView) {
		super(parentView);
	}
	
	@Override
	public void enableComponents( GRITSSpectralViewerData svd ) {
		super.enableComponents(svd);
		boolean bHasAnnotated = svd.getAnnotatedPeaks() != null && ! svd.getAnnotatedPeaks().isEmpty();
		cbUnAnnotatedPeaks.setEnabled(bHasAnnotated);
	}

	public boolean showUnAnnotatedPeaks() {
		return cbUnAnnotatedPeaks.getSelection();
	}
	public Button getUnAnnotatedPeaks() {
		return cbUnAnnotatedPeaks;
	}
	
	public boolean showUnAnnotatedPeakLabels() {
		return cbUnAnnotatedPeakLabels.getSelection();
	}
	public Button getUnAnnotatedPeakLabels() {
		return cbUnAnnotatedPeakLabels;
	}
	
	protected void setUnAnnotatedElements() {
		cbUnAnnotatedPeaks = new Button(parent, SWT.CHECK);
		cbUnAnnotatedPeaks.setText("Unannotated Peaks");
		GridData gdUnAnnotatedPeaks = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1);
		cbUnAnnotatedPeaks.setLayoutData(gdUnAnnotatedPeaks);
		cbUnAnnotatedPeaks.addSelectionListener(new SelectionListener() {			
			@Override
			public void widgetSelected(SelectionEvent e) {
//				if( cbUnAnnotatedPeaks.getSelection() ) {
//					cbPickedPeaks.setSelection(false);
//				}					
				if( ! cbUnAnnotatedPeaks.getSelection() ) {
					cbUnAnnotatedPeakLabels.setSelection(false);
				} 
				cbUnAnnotatedPeakLabels.setEnabled(cbUnAnnotatedPeaks.getSelection());
				updateChart();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});	
		cbUnAnnotatedPeakLabels = new Button(parent, SWT.CHECK);
		cbUnAnnotatedPeakLabels.setText("Show labels");
		GridData gdPickedPeakLabels = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1);
		cbUnAnnotatedPeakLabels.setLayoutData(gdPickedPeakLabels);
		cbUnAnnotatedPeakLabels.setEnabled(false);
		cbUnAnnotatedPeakLabels.addSelectionListener(new SelectionListener() {			
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateChart();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		
	}
		
	@Override
	protected void addElements() {
		setMSElements();
		setPickedPeaksElements();
		setAnnotatedElements();
		setUnAnnotatedElements();
	}
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
	}

	
	public void updateView() {
		// TODO: generic update?
	}

}
