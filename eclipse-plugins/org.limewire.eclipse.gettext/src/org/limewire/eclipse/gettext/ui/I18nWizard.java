/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.limewire.eclipse.gettext.ui;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.limewire.eclipse.gettext.I18n;
import org.limewire.eclipse.gettext.core.I18nRefactoring;

public class I18nWizard extends RefactoringWizard {

	private PropertiesFileWizardPage propertiesFileWizardPage;

	public I18nWizard(I18nRefactoring refactoring) {
		super(refactoring, CHECK_INITIAL_CONDITIONS_ON_OPEN | WIZARD_BASED_USER_INTERFACE);
		setWindowTitle(I18n.tr("Convert Strings to Gettext"));
	}

	/**
	 * @see RefactoringWizard#addUserInputPages()
	 */
	protected void addUserInputPages() {
		I18nRefactoring refactoring= (I18nRefactoring) getRefactoring();
		propertiesFileWizardPage = new PropertiesFileWizardPage(refactoring);
		addPage(propertiesFileWizardPage);
	}

	@Override
	public boolean canFinish() {
		return propertiesFileWizardPage.canFlipToNextPage() && super.canFinish();
	}
	
}
