/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package edu.hawaii.ics.csdl.jupiter.ui.import_wizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import edu.hawaii.ics.csdl.jupiter.ReviewPlugin;


/**
 * @author Prasanna
 */
public class MyPageOne extends WizardPage {

  private Text text1;
  private Composite container;

  public MyPageOne(final String pageName, final IProject project, final String imageFilePath) {
    super(pageName);
    if (imageFilePath != null) {
      setImageDescriptor(ReviewPlugin.createImageDescriptor(imageFilePath));
    }
    setTitle("Select a file to import review");
    String message = "same";
    setMessage(message);
  }

  public void createControl(final Composite ancestor) {
    Composite parent = createsGeneralComposite(ancestor);
    this.container = parent;
    Label label1 = new Label(this.container, SWT.NONE);
    label1.setText("Select a file");

    this.text1 = new Text(this.container, SWT.BORDER | SWT.SINGLE);
    this.text1.setText("");
    text1.this.text1.addKeyListener(new KeyListener() {

      public void keyPressed(final KeyEvent e) {}

      public void keyReleased(final KeyEvent e) {
        if (!MyPageOne.this.text1.getText().isEmpty()) {
          setPageComplete(true);

        }
      }

    });
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    this.text1.setLayoutData(gd);
    // Required to avoid an error in the system
    setControl(this.container);
    setPageComplete(false);

  }


  /**
   * Creates view preference frame and return the child composite.
   * 
   * @param parent the parent composite.
   * @return the child composite.
   */
  private Composite createsGeneralComposite(final Composite parent) {
    Composite child = new Composite(parent, SWT.LEFT);
    FormLayout layout = new FormLayout();
    layout.marginWidth = 7;
    layout.marginHeight = 7;
    child.setLayout(layout);
    return child;
  }

  public String getText1() {
    return this.text1.getText();
  }
}
