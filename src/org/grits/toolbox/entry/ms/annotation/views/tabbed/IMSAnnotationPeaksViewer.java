package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import org.eclipse.swt.widgets.Composite;

import org.grits.toolbox.entry.ms.views.tabbed.IMSPeaksViewer;
import org.grits.toolbox.ms.om.data.Feature;

public interface IMSAnnotationPeaksViewer extends IMSPeaksViewer {
	public Composite getSelectionArea();
	public MSAnnotationSelectionView getCurrentSelectionView();
	public void initNewSelectionView();
	public Feature getFeature(int iRowNum);
}
