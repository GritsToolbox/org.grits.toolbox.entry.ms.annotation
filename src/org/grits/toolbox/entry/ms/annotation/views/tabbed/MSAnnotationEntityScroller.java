package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.grits.toolbox.entry.ms.annotation.process.loader.MSAnnotationTableDataProcessor;
import org.grits.toolbox.entry.ms.annotation.tablehelpers.SharedCheckboxWidget;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.Scan;

public class MSAnnotationEntityScroller extends Composite {
	private static final Logger logger = Logger.getLogger(MSAnnotationEntityScroller.class);
	protected Feature feature = null;
	protected Peak peak = null; // adding in order to get checkbox feature working
	protected Annotation annotation = null;
	protected Scan scan = null;
	protected Button prevButton = null;
	protected Button nextButton = null;
	protected Composite featureControl = null;
	
	// reference to the containing details page in order to perform events
	protected MSAnnotationDetails msAnnotationDetails = null;	
	private int iCurViewInx = -1;
	
	protected Composite compositeTop = null;
	
	protected CLabel idLabel= null;
	protected Label featCounterLabel= null;
	protected int iMaxHeight;
	protected int iMaxWidth;
		
	public MSAnnotationEntityScroller(Composite parent, int style) {
		super(parent, style);
	}
	
	public MSAnnotationEntityScroller(Composite parent, int style, MSAnnotationDetails msAnnotationDetails) {
		this(parent, style);
		this.msAnnotationDetails = msAnnotationDetails;
	}
	
	@Override
	public String toString() {
		return "MSAnnotationEntityScroller (" + feature + ")";
	}
	
	public int calcMaxHeight() {
		int iHeight = (int) (iMaxHeight * 2);
		if ( iHeight < 175 )
			return 175;
		return iHeight;		
	}
	
	public int getMaxHeight() {
		return iMaxHeight;
	}
	
	public void setMaxHeight(int iMaxHeight) {
		this.iMaxHeight = iMaxHeight;
	}

	public int calcMaxWidth() {
		int iWidth = (int) (iMaxWidth * 1.3);
		if ( iWidth < 250 )
			return 250;
		return iWidth;
	}
	
	public int getMaxWidth() {
		return iMaxWidth;
	}
	
	public void setMaxWidth(int iMaxWidth) {
		this.iMaxWidth = iMaxWidth;
	}		
		
	public Scan getScan() {
		return scan;
	}
	
	public void setScan(Scan scan) {
		this.scan = scan;
	}
	
	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public void setFeature(Feature feature) {
		this.feature = feature;
	}

	public Feature getFeature() {
		return feature;
	}

	public Peak getPeak() {
		return peak;
	}
	
	public void setPeak(Peak peak) {
		this.peak = peak;
	}
	
	public String getFeatureDesc() {
		return annotation.getId() + "";
	}

	public String getFeatureCountDesc() {
		return "(" + (getCurViewIndex()+1) + " of " + msAnnotationDetails.getNumFeatures() + ")";
	}
	
	public void reDraw() {
		Feature curFeature = getMsAnnotationDetails().getFeature(getCurViewIndex());
		Annotation annot =  ((MSAnnotationTableDataProcessor) getCurrentPeaksView().getTableDataProcessor()).getAnnotation(curFeature.getAnnotationId());
		Scan scan = ((MSAnnotationTableDataProcessor) getCurrentPeaksView().getTableDataProcessor()).getScan(getMsAnnotationDetails().getMsEntityProperty().getScanNum());
		setFeature( curFeature );
		setAnnotation( annot );
		setScan(scan);
		reDrawLabel();
		reDrawCounterLabel();
	}
	
	protected void reDrawLabel() {
		if( idLabel != null ) 
			idLabel.setText(getFeatureDesc());		
	}
	
	protected void reDrawCounterLabel() {
		if( featCounterLabel != null)
			featCounterLabel.setText(getFeatureCountDesc());		
	}
	
	public CLabel getNewCLabel(Composite parent, int style) {
		CLabel newLabel = new CLabel(parent, style);
		return newLabel;
	}

	protected void drawLabel() {
		GridData idLabelData = new GridData();
		idLabelData.grabExcessHorizontalSpace = true;
		idLabelData.horizontalAlignment = GridData.FILL;
		idLabelData.horizontalSpan = 2;
		idLabel = getNewCLabel(this.compositeTop, SWT.NONE);
		idLabel.setLayoutData(idLabelData);
		GridData fcLabelData = new GridData();
//		fcLabelData.grabExcessHorizontalSpace = true;
		fcLabelData.horizontalAlignment = GridData.CENTER;
//		fcLabelData.horizontalSpan = 1;
		featCounterLabel = new Label(this.compositeTop, SWT.NONE);
		featCounterLabel.setLayoutData(fcLabelData);		
	}
	
	protected void drawFeature() {
		if( this.featureControl != null )
			this.featureControl.dispose();
		featureControl = new Composite(this, SWT.NONE);
		featureControl.setLayout(new FillLayout());
		Label featLabel = new Label(this.featureControl, SWT.NONE);
		featLabel.setText("Feature Id: ");
		Text featText = new Text(this.featureControl, SWT.BORDER);
		if( getFeature() == null ) {
			setFeature( msAnnotationDetails.getFeature( msAnnotationDetails.getCurViewIndex() ));
		}
		featText.setText(getFeature().getId());
		Label seqLabel = new Label(this.featureControl, SWT.NONE);
		seqLabel.setText("Sequence: ");
		Text seqText = new Text(this.featureControl, SWT.BORDER);
		seqText.setText(this.feature.getSequence());
	}

	public Composite getFeatureControl() {
		return featureControl;
	}
	
	
	public void createPartControl(Composite parent) {
		compositeTop = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 5;
		compositeTop.setLayout(new GridLayout(3, false));
		GridData prevButtonData = new GridData();
//		prevButtonData.grabExcessHorizontalSpace = true;
//		prevButtonData.horizontalAlignment = GridData.FILL;
		prevButtonData.grabExcessVerticalSpace = true;
		prevButtonData.verticalAlignment = GridData.FILL;
		this.prevButton = new Button(compositeTop, SWT.DEFAULT);
		this.prevButton.setText("Prev");		
		this.prevButton.setLayoutData(prevButtonData);
		drawFeature();
		GridData nextButtonData = new GridData();
//		nextButtonData.grabExcessHorizontalSpace = true;
//		nextButtonData.horizontalAlignment = GridData.FILL;
		nextButtonData.grabExcessVerticalSpace = true;
		nextButtonData.verticalAlignment = GridData.FILL;
		this.nextButton = new Button(compositeTop, SWT.DEFAULT);
		this.nextButton.setText("Next");
		this.nextButton.setLayoutData(nextButtonData);
		drawLabel();
		getPrevButton().addMouseListener(new MouseListener() {

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

		getNextButton().addMouseListener(new MouseListener() {
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

	public void goNext() {
		this.iCurViewInx++;

		if( this.iCurViewInx >= getMsAnnotationDetails().getPeakComposites().size() ) {
			this.iCurViewInx = 0;
		}		
		reDraw();
	}
	
	public void goPrev() {
		this.iCurViewInx--;
		if( this.iCurViewInx < 0 ) {
			this.iCurViewInx = getMsAnnotationDetails().getPeakComposites().size() - 1;
		}		
		reDraw();
	}
	public Button getNextButton() {
		return nextButton;
	}
	
	public Button getPrevButton() {
		return prevButton;
	}

	public MSAnnotationDetails getMsAnnotationDetails() {
		return msAnnotationDetails;
	}
	
	// not really used in generic annotation. This is placed here for easier shared use in extending implementations
	public static String getCombinedKeyForLookup( Integer _iPeakId, String _sFeatureId ) {
		return _iPeakId + ":" + _sFeatureId;
	}
	
	protected SharedCheckboxWidget getParentSharedCheckboxWidget() {
		return getMsAnnotationDetails().getParentSharedCheckboxWidget();
	}

	public void setCurViewIndex(int iCurViewInx) {
		this.iCurViewInx = iCurViewInx;
	}
	
	public int getCurViewIndex() {
		if( iCurViewInx < 0 ) {
			iCurViewInx = getMsAnnotationDetails().getCurViewIndex();
		}
		return iCurViewInx;
	}

	public MSAnnotationPeaksView getCurrentPeaksView() {
		return getMsAnnotationDetails().getPeaksViews().get( getCurViewIndex() );
	}	

}
