package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.editor.ScrollableEntryEditorPart;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationEntityProperty;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.views.tabbed.content.AnnotationSettingsTableComposite;
import org.grits.toolbox.ms.om.data.AnalyteSettings;
import org.grits.toolbox.ms.om.data.DataHeader;
import org.grits.toolbox.ms.om.data.Method;

/**
 * MS Annotation Property Viewer
 * @author dbrentw
 *
 */
public class MSAnnotationPropertyView extends ScrollableEntryEditorPart {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSAnnotationPropertyView.class);

	private Composite parent = null;
	private Composite container = null;
	protected Method msAnnotationMethod = null;
	protected DataHeader msDataHeader = null;

	private Label descriptionLabel;
	private Text descriptionText;

	private MPart part;
	
	@Inject
	public MSAnnotationPropertyView(Entry entry) {
		this.entry = entry;
	}
	
	@PostConstruct 
	public void postConstruct(MPart part) {
		this.setPart(part);
	}

	@Override
	public void createPartControl(Composite parent) {

		//getPart().setLabel(this.entry.getDisplayName() + " Properties" );
		
		ModifyListener modListener = new ModifyListener()
		{
			public void modifyText(ModifyEvent event) 
			{
				setDirty(true);
			}
		};

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

		MSAnnotationEntityProperty msep = (MSAnnotationEntityProperty) this.entry.getProperty();
		MSAnnotationProperty pp = (MSAnnotationProperty) msep.getParentProperty();
		
		try {
			addDisplayNameLine();
			addDescriptionLine(pp, modListener);	
			AnnotationSettingsTableComposite settingsComposite = getSettingsComposite();
			settingsComposite.setMethod(msAnnotationMethod);
			settingsComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 6, 1));
			settingsComposite.createGeneralSettingsTable();
			settingsComposite.createIonsTable();
			settingsComposite.createIonExchangesTable();
			settingsComposite.createNeutralLossTable();
			
			List<AnalyteSettings> aSettings = getAnalyteSettings();
			addAnalyteSettings(aSettings, settingsComposite);
		} catch( Exception e) {
			logger.error("Error adding property elements.", e);
		}
		sc.setContent(c);
		sc.setMinSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	protected AnnotationSettingsTableComposite getSettingsComposite() {
		return new AnnotationSettingsTableComposite(getContainer(), SWT.NONE);
	}

	private void addDisplayNameLine() {
		Label lblDisplayName = new Label(getContainer(), SWT.NONE);
		lblDisplayName.setText("Display Name");
		GridData gdDisplayData = new GridData();
		gdDisplayData.horizontalSpan = 5;
		lblDisplayName.setLayoutData(gdDisplayData);

		Text txtDisplayName = new Text(getContainer(), SWT.BORDER);
		txtDisplayName.setText( getEntry().getDisplayName() );
		txtDisplayName.setEditable(false);
		GridData gdTxtDisplayData = new GridData();
		gdTxtDisplayData.grabExcessHorizontalSpace = true;
		gdTxtDisplayData.horizontalAlignment = GridData.FILL;
		gdTxtDisplayData.horizontalSpan = 1;
		txtDisplayName.setLayoutData(gdTxtDisplayData);
	}

	private void addDescriptionLine(MSAnnotationProperty pp, ModifyListener modListener) {
		descriptionLabel = new Label(getContainer(), SWT.NONE);
		descriptionLabel.setText("Description");
		GridData descriptionLabelData = new GridData();
		descriptionLabelData.horizontalSpan = 5;
		descriptionLabel.setLayoutData(descriptionLabelData);

		descriptionText = new Text(getContainer(),SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		descriptionText.setText(pp.getMSAnnotationMetaData().getDescription());
		descriptionText.addModifyListener(getModListener());
		GridData gdTxtGridData = new GridData();
		gdTxtGridData.grabExcessHorizontalSpace = true;
		gdTxtGridData.horizontalAlignment = GridData.FILL;
		gdTxtGridData.verticalAlignment = GridData.FILL;
		gdTxtGridData.horizontalSpan = 1;
		gdTxtGridData.verticalSpan = 3;
		descriptionText.setLayoutData(gdTxtGridData);
	}

	protected List<AnalyteSettings> getAnalyteSettings() {
		return this.msAnnotationMethod.getAnalyteSettings();
	}

	protected void addAnalyteSettings( List<AnalyteSettings> aSettings, AnnotationSettingsTableComposite settingsComposite ) {
		if( aSettings != null ) {			
			settingsComposite.createAnalyteSettingsTable();
		}		
	}

	public void setMsAnnotationMethod(Method msAnnotationMethod) {
		this.msAnnotationMethod = msAnnotationMethod;
	}

	public Method getMsAnnotationMethod() {
		return msAnnotationMethod;
	}	

	protected boolean isValidInput() {
		if(!checkBasicLengthCheck(descriptionLabel, descriptionText, 0, Integer.parseInt(PropertyHandler.getVariable("descriptionLength"))))
		{
			return false;
		}
		return true;
	}

	protected void updateProjectProperty() {
		try {
			Entry projectEntry = DataModelSearch.findParentByType(this.entry, ProjectProperty.TYPE);
			MSAnnotationProperty property = null;
			if( this.entry.getProperty() instanceof  MSAnnotationEntityProperty ) {
				property = (MSAnnotationProperty) ((MSAnnotationEntityProperty) this.entry.getProperty()).getParentProperty();
			} else {
				property = (MSAnnotationProperty)this.entry.getProperty();
			}
			// set description and serialize!
			property.getMSAnnotationMetaData().setDescription(descriptionText.getText());
			String settingsFile = property.getFullyQualifiedMetaDataFileName(projectEntry);
			MSAnnotationProperty.marshallSettingsFile(settingsFile, property.getMSAnnotationMetaData());
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Focus
	public void setFocus() {
		descriptionLabel.setFocus();
	}

	@Override
	protected Composite getParent() {
		return this.parent;
	}

	protected Composite getContainer() {
		return this.container;
	}

	@Override
	protected void savePreference() {
		// nothing to save
	}

	@Override
	protected void initializeComponents() {
		// nothing to initialize
	}

	public MPart getPart() {
		return part;
	}

	public void setPart(MPart part) {
		this.part = part;
	}

}
