/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package edu.hawaii.ics.csdl.jupiter.ui.import_wizard;


import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class MyPageTwo extends WizardPage {

  private Text text1;
  private Composite container;

  public MyPageTwo() {
    super("Second Page");
    setTitle("Second Page");
    setDescription("Now this is the second page");
    setControl(this.text1);
  }

  public void createControl(final Composite parent) {
    this.container = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    this.container.setLayout(layout);
    layout.numColumns = 2;
    Label label1 = new Label(this.container, SWT.NONE);
    label1.setText("Say hello to Fred");

    this.text1 = new Text(this.container, SWT.BORDER | SWT.SINGLE);
    this.text1.setText("");
    this.text1.addKeyListener(new KeyListener() {

      public void keyPressed(final KeyEvent e) {
        // TODO Auto-generated method stub

      }

      public void keyReleased(final KeyEvent e) {
        if (!MyPageTwo.this.text1.getText().isEmpty()) {
          setPageComplete(true);
        }
      }

    });
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    this.text1.setLayoutData(gd);
    Label labelCheck = new Label(this.container, SWT.NONE);
    labelCheck.setText("This is a check");
    Button check = new Button(this.container, SWT.CHECK);
    check.setSelection(true);
    // Required to avoid an error in the system
    setControl(this.container);
    setPageComplete(false);
  }

  public String getText1() {
    return this.text1.getText();
  }
}
