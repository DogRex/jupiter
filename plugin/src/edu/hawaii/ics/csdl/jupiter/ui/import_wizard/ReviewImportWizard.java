/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package edu.hawaii.ics.csdl.jupiter.ui.import_wizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.Wizard;


/**
 * @author Prasanna
 */
public class ReviewImportWizard extends Wizard {

  public final static String PAGE_SELECT_FILE = "SelectFile";
  private final IProject project;
  protected MyPageOne one;
  protected MyPageTwo two;


  /**
   * 
   */
  public ReviewImportWizard(final IProject project) {
    super();
    this.project = project;
    setNeedsProgressMonitor(true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addPages() {
    this.one = new MyPageOne(PAGE_SELECT_FILE, this.project, null);
    this.two = new MyPageTwo();
    addPage(this.one);
    addPage(this.two);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean performFinish() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canFinish() {
    return true;
  }
}
