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
package org.limewire.eclipse.gettext.core;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.limewire.eclipse.gettext.GettextPlugin;

public class I18nRefactoring extends Refactoring {

	private File resourcesFile;

	private List<Change> changes;

	private Properties textByKey;

	private IJavaElement source;

	private ICompilationUnit[] units;

	private Set<String> trMethodNames = new HashSet<String>();
	
	private String markTrClassName;
	
	private String markTrMethodName;
	
	public I18nRefactoring(IJavaElement source, ICompilationUnit[] units) {
		this.source = source;
		this.units = units;
	}

	public File getResourcesFile() {
		return resourcesFile;
	}
	
	public void setResourcesFile(File resourcesFile) {
		this.resourcesFile = resourcesFile;
	}
	
	public String[] getTrMethodNames() {
		return trMethodNames.toArray(new String[0]);
	}
	
	public void setTrMethodNames(String[] trMethodNames) {
		this.trMethodNames = new HashSet<String>(Arrays.asList(trMethodNames));
	}
	
	public String getMarkTrMethodName() {
		return markTrMethodName;
	}
	
	public void setMarkTrMethodName(String markTrMethodName) {
		this.markTrMethodName = markTrMethodName;
	}
	
	public String getMarkTrClassName() {
		return markTrClassName;
	}
	
	public void setMarkTrClassName(String markTrClassName) {
		this.markTrClassName = markTrClassName;
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(final IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		try {
			pm.beginTask("Converting strings", units.length + 20);

			pm.subTask("Reading properties");
			textByKey = new Properties();
			FileInputStream in = new FileInputStream(resourcesFile);
			try {
				textByKey.load(in);
			} finally {
				in.close();
			}
			pm.worked(20);

			changes = new ArrayList<Change>();

			for (ICompilationUnit unit : units) {
				pm.subTask("Processing " + unit.getElementName());
				Change change = convertStrings(unit, new SubProgressMonitor(pm, 1));
				if (change != null) {
					changes.add(change);
				}		
			}

			return RefactoringStatus.create(Status.OK_STATUS);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CoreException(GettextPlugin.toStatus(e));
		} finally {
			pm.done();
		}
	}

	private Change convertStrings(ICompilationUnit unit, IProgressMonitor pm)
			throws MalformedTreeException, CoreException {
		try {
			pm.beginTask("Rewriting: " + unit.getElementName(), 4);

			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(unit);
			ASTNode rootNode = parser.createAST(pm);
			Visitor visitor = new Visitor(unit, rootNode);
			rootNode.accept(visitor);
			pm.worked(1);

			ASTRewrite astRewrite = visitor.getAstRewrite();
			if (astRewrite != null) {
				MultiTextEdit edit = new MultiTextEdit();
				edit.addChild(astRewrite.rewriteAST());
				pm.worked(1);
				
				ImportRewrite importRewrite = visitor.getImportRewrite();
				if (importRewrite != null) {
					edit.addChild(importRewrite.rewriteImports(new NullProgressMonitor()));
				}
				pm.worked(1);
				
				TextFileChange change = new TextFileChange(unit
						.getElementName(), (IFile) unit.getResource());
				change.setTextType("java");
				change.setEdit(edit);
				pm.worked(1);

				return change;
			}

			return null;
		} finally {
			pm.done();
		}
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return RefactoringStatus.create(Status.OK_STATUS);
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		return new CompositeChange(getName(), changes.toArray(new Change[0]));
	}

	@Override
	public String getName() {
		return "Convert Gettext";
	}

	public IJavaProject getJavaProject() {
		return source.getJavaProject();
	}
	
	private class Visitor extends ASTVisitor {

		private AST ast;
		private ASTRewrite astRewrite;
		private ImportRewrite importRewrite;
		private ICompilationUnit cu;

		public Visitor(ICompilationUnit cu, ASTNode rootNode) {
			this.cu = cu;
			this.ast = rootNode.getAST();
		}

		public ASTRewrite getAstRewrite() {
			return astRewrite;
		}
		
		public ImportRewrite getImportRewrite() {
			return importRewrite;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean visit(StringLiteral node) {
			String key = node.getLiteralValue();
			System.out.println("Processing: " + key);
			String text = textByKey.getProperty(key);
			if (text != null) {
				System.out.println(" -> " + text);
				ASTNode newNode;

				if (astRewrite == null) {
					astRewrite = ASTRewrite.create(ast);
				}

				StringLiteral newTextNode = ast.newStringLiteral();
				newTextNode.setLiteralValue(text);

				if (needsMarkTr(node)) {
					MethodInvocation marktrInvocation = ast.newMethodInvocation();					
					marktrInvocation.setName(ast.newSimpleName(getMarkTrMethodName()));
					marktrInvocation.setExpression(getQualifiedName(getMarkTrClassName()));
					marktrInvocation.arguments().add(newTextNode);
					newNode = marktrInvocation;
				} else {
					newNode = newTextNode;
				}
				
				astRewrite.replace(node, newNode, null);
			}
			return true;
		}

		private Name getQualifiedName(String className) {
			String packageName = "";
			String typeName;
			int i = className.lastIndexOf(".");
			if (i != -1) {
				packageName = className.substring(0, i);
				typeName = className.substring(i + 1);
			} else {
				typeName = className;
			}
			
			if (importRewrite == null) {
				try {
					importRewrite = ImportRewrite.create(cu, true);
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
			}
			
			importRewrite.addImport(packageName + "." + typeName);
			
			return ast.newSimpleName(typeName);
		}

		private boolean needsMarkTr(StringLiteral node) {
			ASTNode parent = node.getParent();
			if (parent instanceof MethodInvocation) {
				MethodInvocation methodInvocation = (MethodInvocation) parent;
				String name = methodInvocation.getName().getFullyQualifiedName();
				return !trMethodNames.contains(name);
			}
			return true;
		}

	}

}
