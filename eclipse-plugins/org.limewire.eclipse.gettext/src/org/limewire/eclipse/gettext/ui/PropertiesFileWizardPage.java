package org.limewire.eclipse.gettext.ui;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.limewire.eclipse.gettext.I18n;
import org.limewire.eclipse.gettext.core.I18nRefactoring;

public class PropertiesFileWizardPage extends UserInputWizardPage {

	private Text propertiesFileText;
	private IProject project;
	private I18nRefactoring refactoring;
	private Text trMethodNamesText;
	private Text marktrClassNameText;
	private Text marktrMethodNameText;

	public PropertiesFileWizardPage(I18nRefactoring refactoring) {
		super("PropertyPage");

		this.refactoring = refactoring;
		this.project = refactoring.getJavaProject().getProject();

		setTitle(I18n.tr("Specifiy Properties"));		
	}

	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		Label label = new Label(composite, SWT.WRAP);
		label
				.setText(I18n
						.tr("The property file specifies a mapping of keys in the source code to translated strings. Any occurences of a key in a string literal will be replaced by the corresponding translation."));
		GridDataFactory.generate(label, 3, 1);

		label = new Label(composite, SWT.NONE);
		label.setText(I18n.tr("Property file:"));

		propertiesFileText = new Text(composite, SWT.DEFAULT);
		propertiesFileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				refactoring.setResourcesFile(new File(propertiesFileText
						.getText()));
				updateStatus();
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(
				propertiesFileText);

		Button button = new Button(composite, SWT.DEFAULT);
		button.setText("Browse");
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				FilteredResourcesSelectionDialog dialog = new FilteredResourcesSelectionDialog(
						getShell(), false, project, IResource.FILE);
				dialog.setInitialPattern("*.properties");
				if (dialog.open() == Window.OK) {
					IFile selectedFile = (IFile) dialog.getFirstResult();
					if (selectedFile != null) {
						propertiesFileText.setText(selectedFile.getLocation()
								.toFile().getAbsolutePath());
					}
				}
			}
		});

		// spacer
		label = new Label(composite, SWT.WRAP);
		GridDataFactory.generate(label, 3, 1);

		label = new Label(composite, SWT.WRAP);
		label
				.setText(I18n
						.tr("Names of methods that are used to lookup translations (separated by comma)."));
		GridDataFactory.generate(label, 3, 1);

		label = new Label(composite, SWT.WRAP);
		label.setText(I18n.tr("Method Names:"));

		trMethodNamesText = new Text(composite, SWT.DEFAULT);
		trMethodNamesText.setEditable(true);
		trMethodNamesText
				.setText("getStringResource, getFormattedStringResource, tr, trn, trc, marktr");
		trMethodNamesText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				refactoring.setTrMethodNames(trMethodNamesText.getText().split(
						", *"));
				updateStatus();
			}
		});
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(
				trMethodNamesText);

		// spacer
		label = new Label(composite, SWT.WRAP);
		GridDataFactory.generate(label, 3, 1);

		label = new Label(composite, SWT.WRAP);
		label
				.setText(I18n
						.tr("Method call that is added for strings that are not translated."));
		GridDataFactory.generate(label, 3, 1);

		label = new Label(composite, SWT.WRAP);
		label.setText(I18n.tr("Class:"));

		marktrClassNameText = new Text(composite, SWT.DEFAULT);
		marktrClassNameText.setEditable(true);
		marktrClassNameText.setText("com.limegroup.gnutella.I18n");
		marktrClassNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				refactoring.setMarkTrClassName(marktrClassNameText.getText());
				updateStatus();
			}
		});
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(
				marktrClassNameText);

		label = new Label(composite, SWT.WRAP);
		label.setText(I18n.tr("Method:"));

		marktrMethodNameText = new Text(composite, SWT.DEFAULT);
		marktrMethodNameText.setEditable(true);
		marktrMethodNameText.setText("marktr");
		marktrMethodNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				refactoring.setMarkTrMethodName(marktrMethodNameText.getText());
				updateStatus();
			}
		});
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(
				marktrMethodNameText);

		apply();
		updateStatus();
		
		Dialog.applyDialogFont(composite);
		setControl(composite);
	}

	private void updateStatus() {
		if (refactoring.getResourcesFile() == null) {
			setMessage(I18n.tr("Select a property file"));
			setPageComplete(false);
		} else if (refactoring.getMarkTrMethodName() == null
				|| refactoring.getMarkTrMethodName().trim().length() == 0) {
			setMessage(I18n.tr("Enter a translation method name"));
			setPageComplete(false);
		} else {
			setMessage(I18n.tr("Please enter the properties for converting existing translations for the selected elements."));
			setPageComplete(true);
		}
	}

	private void apply() {
		refactoring.setTrMethodNames(trMethodNamesText.getText().split(", *"));
		refactoring.setMarkTrClassName(marktrClassNameText.getText());
		refactoring.setMarkTrMethodName(marktrMethodNameText.getText());
	}
	
}
