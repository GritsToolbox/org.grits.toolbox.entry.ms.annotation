package org.grits.toolbox.entry.ms.annotation.views.tabbed;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.editor.ScrollableEntryEditorPart;
import org.grits.toolbox.ms.om.data.Method;

public class MSAnnotationFilterView extends ScrollableEntryEditorPart {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSAnnotationFilterView.class);
		
	private MPart part;
	private Composite parent;
	private Composite container;

	private Method msAnnotationMethod;
	
	@Inject
	public MSAnnotationFilterView(Entry entry) {
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
			addFilterSettings();
		} catch( Exception e) {
			logger.error("Error adding property elements.", e);
		}
		sc.setContent(c);
		sc.setMinSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	protected void addFilterSettings () {
		// nothing to do for generic Filter settings
	}

	public void setMsAnnotationMethod(Method msAnnotationMethod) {
		this.msAnnotationMethod = msAnnotationMethod;
	}

	public Method getMsAnnotationMethod() {
		return msAnnotationMethod;
	}
	
	@Override
	protected Composite getParent() {
		return this.parent;
	}
	
	@Override
	protected void initializeComponents() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateProjectProperty() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void savePreference() {
		// TODO Auto-generated method stub

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

}
