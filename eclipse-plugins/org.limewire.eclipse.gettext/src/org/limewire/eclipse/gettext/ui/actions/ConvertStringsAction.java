package org.limewire.eclipse.gettext.ui.actions;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.limewire.eclipse.gettext.GettextPlugin;
import org.limewire.eclipse.gettext.core.I18nRefactoring;
import org.limewire.eclipse.gettext.ui.I18nWizard;

public class ConvertStringsAction implements IWorkbenchWindowActionDelegate {

	private ICompilationUnit units[];
	private IJavaElement source;
	private IWorkbenchWindow window;

	public ConvertStringsAction() {
	}

	private ICompilationUnit[] getSource(IStructuredSelection selection) {
		if (selection.isEmpty() || selection.size() != 1)
			return null;

		Object first = selection.getFirstElement();
		if (first instanceof IJavaElement) {
			this.source = (IJavaElement) first;
			return getCompilationUnits(selection);
		}

		return null;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			action.setEnabled(false);
			return;
		}

		this.units = getSource((IStructuredSelection) selection);
		action.setEnabled(this.units != null);
	}

	public void run(IAction action) {
		I18nRefactoring refactoring = new I18nRefactoring(source, units);
		I18nWizard wizard = new I18nWizard(refactoring);

		RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(
				wizard);
		try {
			op.run(window.getShell(), "Convert String");
		} catch (InterruptedException ignored) {
			// canceled
		}
	}

	public void dispose() {
		this.window = null;
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public ICompilationUnit[] getCompilationUnits(IStructuredSelection selection) {
		Set<ICompilationUnit> result = new HashSet<ICompilationUnit>();
		Object[] selected = selection.toArray();
		for (int i = 0; i < selected.length; i++) {
			try {
				if (selected[i] instanceof IJavaElement) {
					IJavaElement elem = (IJavaElement) selected[i];
					if (elem.exists()) {
						switch (elem.getElementType()) {
						case IJavaElement.TYPE:
							if (elem.getParent().getElementType() == IJavaElement.COMPILATION_UNIT) {
								result.add((ICompilationUnit) elem.getParent());
							}
							break;
						case IJavaElement.COMPILATION_UNIT:
							result.add((ICompilationUnit) elem);
							break;
						case IJavaElement.IMPORT_CONTAINER:
							result.add((ICompilationUnit) elem.getParent());
							break;
						case IJavaElement.PACKAGE_FRAGMENT:
							collectCompilationUnits((IPackageFragment) elem,
									result);
							break;
						case IJavaElement.PACKAGE_FRAGMENT_ROOT:
							collectCompilationUnits(
									(IPackageFragmentRoot) elem, result);
							break;
						case IJavaElement.JAVA_PROJECT:
							IPackageFragmentRoot[] roots = ((IJavaProject) elem)
									.getPackageFragmentRoots();
							for (int k = 0; k < roots.length; k++) {
								collectCompilationUnits(roots[k], result);
							}
							break;
						}
					}
				}
			} catch (JavaModelException e) {
				GettextPlugin.log(e.getStatus());
			}
		}
		return (ICompilationUnit[]) result.toArray(new ICompilationUnit[result
				.size()]);
	}

	private void collectCompilationUnits(IPackageFragment pack,
			Collection<ICompilationUnit> result) throws JavaModelException {
		result.addAll(Arrays.asList(pack.getCompilationUnits()));
	}

	private void collectCompilationUnits(IPackageFragmentRoot root,
			Collection<ICompilationUnit> result) throws JavaModelException {
		if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
			IJavaElement[] children = root.getChildren();
			for (int i = 0; i < children.length; i++) {
				collectCompilationUnits((IPackageFragment) children[i], result);
			}
		}
	}

}