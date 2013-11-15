/*
 * Copyright (c) Robert Bosch GmbH. All rights reserved.
 */
package edu.hawaii.ics.csdl.jupiter.ui.preference;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.hawaii.ics.csdl.jupiter.ReviewI18n;
import edu.hawaii.ics.csdl.jupiter.ui.property.ReviewPropertyContentProvider;
import edu.hawaii.ics.csdl.jupiter.ui.property.ReviewPropertyLabelProvider;


/**
 * @author pbt2kor
 */
public class ReviewTemplatePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

  private Composite composite;
  private Button newButton;
  private Table table;
  private Button removeButton;
  private Button editButton;
  private TableViewer tableViewer;

  private static final String COLUMN_KEY = "ColumnKey";
  /** The column review template ID key. */
  public static final String COLUMN_REVIEW_TEMPLATE_KEY = "ReviewTemplatePreferencePage.label.column.templateId";
  /** The column description key. */
  public static final String COLUMN_DESCRIPTION_KEY = "ReviewTemplatePreferencePage.label.column.description";

  /**
   * {@inheritDoc}
   */
  @Override
  protected Control createContents(final Composite ancestor) {
    this.composite = ancestor;
    noDefaultAndApplyButton();
    Composite parent = createsGeneralComposite(ancestor);
    createReviewIdTableContent(parent);
    createButtonsContent(parent);
    return parent;
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

  /**
   * Creates review id table.
   * 
   * @param parent the composite.
   */
  private void createReviewIdTableContent(final Composite parent) {
    this.table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
    FormData tableData = new FormData();
    tableData.left = new FormAttachment(0, 0);
    tableData.right = new FormAttachment(80, 0);
    tableData.top = new FormAttachment(0, 0);
    tableData.bottom = new FormAttachment(100, 0);
    this.table.setLayoutData(tableData);
    this.table.setHeaderVisible(true);
    this.table.setLinesVisible(true);
    this.table.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event e) {
        handleReviewIdSelection();
      }
    });
    TableColumn columnReviewId = new TableColumn(this.table, SWT.NONE);
    columnReviewId.setText(ReviewI18n.getString(COLUMN_REVIEW_TEMPLATE_KEY));
    columnReviewId.setData(COLUMN_KEY, COLUMN_REVIEW_TEMPLATE_KEY);

    TableColumn columnDescription = new TableColumn(this.table, SWT.NONE);
    String description = ReviewI18n.getString(COLUMN_DESCRIPTION_KEY);
    columnDescription.setText(description);
    columnDescription.setData(COLUMN_KEY, COLUMN_DESCRIPTION_KEY);


    List<TableColumn> columnList = new ArrayList<TableColumn>();
    columnList.add(columnReviewId);
    columnList.add(columnDescription);
    hookSelectionListener(columnList);

    TableLayout tableLayout = new TableLayout();
    tableLayout.addColumnData(new ColumnWeightData(22));
    tableLayout.addColumnData(new ColumnWeightData(48));
    this.table.setLayout(tableLayout);

    this.tableViewer = new TableViewer(this.table);
    this.tableViewer.setLabelProvider(new ReviewPropertyLabelProvider());
    this.tableViewer.setContentProvider(new ReviewPropertyContentProvider());
    // this.tableViewer.setSorter(ReviewPropertyViewerSorter.getViewerSorter(COLUMN_DATE_KEY));
    // this.tableViewer.setInput(PropertyResource.getInstance(this.project, true).getReviewIdList());

    this.tableViewer.addDoubleClickListener(new IDoubleClickListener() {

      public void doubleClick(final DoubleClickEvent event) {
        // editReviewId();
      }
    });
  }

  /**
   * Creates buttons content.
   * 
   * @param parent the parent.
   */
  private void createButtonsContent(final Composite parent) {
    this.newButton = new Button(parent, SWT.PUSH);
    this.newButton.setText(ReviewI18n.getString("ReviewPropertyPage.label.button.new"));
    this.newButton.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event e) {
        addReviewId();
      }
    });
    FormData newButtonData = new FormData();
    newButtonData.top = new FormAttachment(this.table, 0, SWT.TOP);
    newButtonData.left = new FormAttachment(this.table, 10);
    newButtonData.right = new FormAttachment(100, 0);
    this.newButton.setLayoutData(newButtonData);

    this.editButton = new Button(parent, SWT.PUSH);
    this.editButton.setText(ReviewI18n.getString("ReviewPropertyPage.label.button.edit"));
    this.editButton.setEnabled(false);
    this.editButton.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event e) {
        editReviewId();
      }
    });
    FormData editButtonData = new FormData();
    editButtonData.top = new FormAttachment(this.newButton, 5);
    editButtonData.left = new FormAttachment(this.newButton, 0, SWT.LEFT);
    editButtonData.right = new FormAttachment(100, 0);
    this.editButton.setLayoutData(editButtonData);

    this.removeButton = new Button(parent, SWT.PUSH);
    this.removeButton.setText(ReviewI18n.getString("ReviewPropertyPage.label.button.remove"));
    this.removeButton.setEnabled(false);
    this.removeButton.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event e) {
        removeReviewId();
      }
    });
    FormData removeButtonData = new FormData();
    removeButtonData.top = new FormAttachment(this.editButton, 5);
    removeButtonData.left = new FormAttachment(this.newButton, 0, SWT.LEFT);
    removeButtonData.right = new FormAttachment(100, 0);
    this.removeButton.setLayoutData(removeButtonData);
  }


  /**
   * Hooks selection listener for each <code>TableColumn</code> element of the list.
   * 
   * @param columnList the list of the <code>TableColumn</code> elements.
   */
  private void hookSelectionListener(final List<TableColumn> columnList) {
    for (TableColumn column : columnList) {
      column.addListener(SWT.Selection, new Listener() {

        public void handleEvent(final Event event) {
          String columnKey = (String) event.widget.getData(COLUMN_KEY);
          // sortBy(columnKey);
        }
      });
    }
  }


  /**
   * Handles review id selection
   */
  protected void handleReviewIdSelection() {

  }

  private void addReviewId() {

  }

  private void editReviewId() {

  }

  private void removeReviewId() {

  }


  /**
   * {@inheritDoc}
   */
  @Override
  public void init(final IWorkbench workbench) {
    // TODO Auto-generated method stub

  }

}
