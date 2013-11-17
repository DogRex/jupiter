package edu.hawaii.ics.csdl.jupiter.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;

import edu.hawaii.ics.csdl.jupiter.ReviewException;
import edu.hawaii.ics.csdl.jupiter.file.property.CreationDate;
import edu.hawaii.ics.csdl.jupiter.file.property.FieldItem;
import edu.hawaii.ics.csdl.jupiter.file.property.FieldItems;
import edu.hawaii.ics.csdl.jupiter.file.property.Files;
import edu.hawaii.ics.csdl.jupiter.file.property.Filter;
import edu.hawaii.ics.csdl.jupiter.file.property.Filters;
import edu.hawaii.ics.csdl.jupiter.file.property.ObjectFactory;
import edu.hawaii.ics.csdl.jupiter.file.property.Phase;
import edu.hawaii.ics.csdl.jupiter.file.property.Property;
import edu.hawaii.ics.csdl.jupiter.file.property.Review;
import edu.hawaii.ics.csdl.jupiter.file.property.Reviewers;
import edu.hawaii.ics.csdl.jupiter.model.review.ReviewId;
import edu.hawaii.ics.csdl.jupiter.model.review.ReviewerId;
import edu.hawaii.ics.csdl.jupiter.util.JupiterLogger;

/**
 * Provides the review id wrapper singleton class.
 * 
 * @author Takuya Yamashita
 * @version $Id: PropertyResource.java 180 2011-01-18 03:56:36Z jsakuda $
 */
public class PropertyResource {

  /** Jupiter logger */
  private final JupiterLogger log = JupiterLogger.getLogger();

  private static PropertyResource theInstance = new PropertyResource();
  private final Map<String, ReviewId> reviewIdMap;
  private IProject project;
  private boolean isDefaultLoaded;
  private Property property;
  private final Map<String, Review> reviewIdReviewMap;

  /** The default review id. */
  public static final String DEFAULT_ID = PropertyConstraints.DEFAULT_REVIEW_ID;

  /**
   * Prohibits clients from instantiating this.
   */
  private PropertyResource() {
    this.reviewIdMap = new TreeMap<String, ReviewId>();
    this.reviewIdReviewMap = new TreeMap<String, Review>();
  }

  /**
   * Gets the review id wrapper class for the project.
   * 
   * @param project the project.
   * @param isDefaultLoaded sets <code>true</code> if the default review id is loaded too.
   * @return the review id wrapper class which contains set of <code>ReviewID</code> instances.
   */
  public static PropertyResource getInstance(final IProject project, final boolean isDefaultLoaded) {
    if ((project != theInstance.project) || (isDefaultLoaded != theInstance.isDefaultLoaded)) {
      // reload
      theInstance.setValues(project, isDefaultLoaded);
      theInstance.fillReviewIdReviewMap(project, isDefaultLoaded);
      theInstance.fillReviewIdMap();
    }

    return theInstance;
  }

  /**
   * Sets project and isDefaultLoaded values.
   * 
   * @param project the project.
   * @param isDefaultLoaded set true if default review id is also loaded.
   */
  private void setValues(final IProject project, final boolean isDefaultLoaded) {
    this.project = project;
    this.isDefaultLoaded = isDefaultLoaded;
  }

  /**
   * Loads the <code>Review</code>s.
   * 
   * @param project The project containing the reviews.
   * @param isDefaultLoaded True if the default review ID should be loaded, otherwise false.
   */
  private void fillReviewIdReviewMap(final IProject project, final boolean isDefaultLoaded) {
    try {
      this.property = PropertyXmlSerializer.newProperty(project);
    }
    catch (ReviewException e) {
      this.log.error(e);
    }
    if (this.property == null) {
      return;
    }

    this.reviewIdReviewMap.clear();
    List<Review> reviews = this.property.getReview();
    for (Review review : reviews) {
      String reviewId = review.getId();
      if (reviewId.equals(PropertyConstraints.DEFAULT_REVIEW_ID) && !isDefaultLoaded) {
        // don't load the default review id
        continue;
      }
      this.reviewIdReviewMap.put(reviewId, review);
    }
  }

  /**
   * Gets the <code>ReviewResource</code> instance associating with the review id. Returns null if the review id does
   * not exist.
   * 
   * @param reviewId the review id.
   * @param isClone true if the <code>ReviewResource</code> contains the clone of the review element. false if it
   *          contains the review element of the document.
   * @return the <code>ReviewResource</code> instance associating with the review id. Returns null if the review id does
   *         not exist.
   */
  public ReviewResource getReviewResource(final String reviewId, final boolean isClone) {
    Review review = this.reviewIdReviewMap.get(reviewId);
    if (review != null) {
      review = (isClone) ? copyReview(review) : review;
      return new ReviewResource(review);
    }
    return null;
  }

  /**
   * Copies a review object.
   * 
   * @param review The review object to copy.
   * @return Returns the copied review.
   */
  private Review copyReview(final Review review) {
    if (review == null) {
      return null;
    }

    Review copiedReview = new Review();
    copiedReview.setAuthor(review.getAuthor());
    copiedReview.setDescription(review.getDescription());
    copiedReview.setDirectory(review.getDirectory());
    copiedReview.setId(review.getId());

    CreationDate creationDate = review.getCreationDate();
    if (creationDate != null) {
      CreationDate copiedCreationDate = new CreationDate();
      copiedCreationDate.setFormat(creationDate.getFormat());
      copiedCreationDate.setValue(creationDate.getValue());
      copiedReview.setCreationDate(copiedCreationDate);
    }

    Reviewers reviewers = review.getReviewers();
    if (reviewers != null) {
      Reviewers copiedReviewers = new Reviewers();

      List<Reviewers.Entry> entryList = reviewers.getEntry();
      for (Reviewers.Entry entry : entryList) {
        Reviewers.Entry copiedReviewersEntry = new Reviewers.Entry();
        copiedReviewersEntry.setId(entry.getId());
        copiedReviewersEntry.setName(entry.getName());
        copiedReviewers.getEntry().add(copiedReviewersEntry);
      }
      copiedReview.setReviewers(copiedReviewers);
    }

    Files files = review.getFiles();
    if (files != null) {
      Files copiedFiles = new Files();

      List<Files.Entry> entryList = files.getEntry();
      for (Files.Entry entry : entryList) {
        Files.Entry copiedFilesEntry = new Files.Entry();
        copiedFilesEntry.setName(entry.getName());
        copiedFiles.getEntry().add(copiedFilesEntry);
      }
      copiedReview.setFiles(copiedFiles);
    }

    FieldItems fieldItems = review.getFieldItems();
    if (fieldItems != null) {
      FieldItems copiedFieldItems = new FieldItems();
      List<FieldItem> fieldItemList = fieldItems.getFieldItem();
      for (FieldItem fieldItem : fieldItemList) {
        FieldItem copiedFieldItem = new FieldItem();
        copiedFieldItem.setDefault(fieldItem.getDefault());
        copiedFieldItem.setId(fieldItem.getId());

        List<FieldItem.Entry> entryList = fieldItem.getEntry();
        for (FieldItem.Entry entry : entryList) {
          FieldItem.Entry copiedFieldItemEntry = new FieldItem.Entry();
          copiedFieldItemEntry.setName(entry.getName());
          copiedFieldItem.getEntry().add(copiedFieldItemEntry);
        }
        copiedFieldItems.getFieldItem().add(copiedFieldItem);
      }
      copiedReview.setFieldItems(copiedFieldItems);
    }

    Filters filters = review.getFilters();
    if (filters != null) {
      Filters copiedFilters = new Filters();

      List<Phase> phaseList = filters.getPhase();
      for (Phase phase : phaseList) {
        Phase copiedPhase = new Phase();
        copiedPhase.setName(phase.getName());
        copiedPhase.setEnabled(phase.isEnabled());

        // filter objects
        List<Filter> filterList = phase.getFilter();
        for (Filter filter : filterList) {
          Filter copiedFilter = new Filter();
          copiedFilter.setName(filter.getName());
          copiedFilter.setValue(filter.getValue());
          copiedFilter.setEnabled(filter.isEnabled());
          copiedPhase.getFilter().add(copiedFilter);
        }
        copiedFilters.getPhase().add(copiedPhase);
      }
      copiedReview.setFilters(copiedFilters);
    }

    return copiedReview;
  }

  /**
   * Loads <code>ReviewId</code> instances for the project into review id map.
   */
  private void fillReviewIdMap() {
    this.reviewIdMap.clear();
    for (Review review : this.reviewIdReviewMap.values()) {
      ReviewResource reviewResource = new ReviewResource(review);
      ReviewId reviewId = reviewResource.getReviewId();
      this.reviewIdMap.put(reviewId.getReviewId(), reviewId);
    }
  }

  /**
   * Gets the array of the <code>String</code> review id names. Note that the order of the elements is reverse
   * chronological order in which review id was created.
   * 
   * @return the array of the <code>String</code> review id names
   */
  public String[] getReviewIdNames() {
    List<ReviewId> reviewIdList = getReviewIdList();
    Map<Date, String> reviewIdNameMap = new TreeMap<Date, String>(new Comparator<Date>() {

      public int compare(final Date object1, final Date object2) {
        return object2.compareTo(object1);
      }
    });
    for (ReviewId reviewId2 : reviewIdList) {
      ReviewId reviewId = reviewId2;
      reviewIdNameMap.put(reviewId.getDate(), reviewId.getReviewId());
    }
    return new ArrayList<String>(reviewIdNameMap.values()).toArray(new String[] {});
  }

  /**
   * Gets the array of the <code>String</code> reviewer id names.
   * 
   * @param reviewIdName the review id.
   * @return the array of the <code>String</code> review id names
   */
  public String[] getReviewerIdNames(final String reviewIdName) {
    Map<String, ReviewerId> reviewers = getReviewers(reviewIdName);
    return new ArrayList<String>(reviewers.keySet()).toArray(new String[] {});
  }

  /**
   * Gets the map of the <code>ReviewerId</code> instances. Returns the map of default reviewers if no reviewer exists.
   * 
   * @param reviewIdName the review id name.
   * @return the <code>Map</code> of the <code>ReviewerId</code> instance.
   */
  public Map<String, ReviewerId> getReviewers(final String reviewIdName) {
    ReviewId reviewId = this.reviewIdMap.get(reviewIdName);
    Map<String, ReviewerId> reviewersMap = new TreeMap<String, ReviewerId>();
    if (reviewId != null) {
      reviewersMap = reviewId.getReviewers();
    }
    return reviewersMap;
  }

  /**
   * Gets the list of the <code>ReviewId</code> instances.
   * 
   * @return the list of the <code>ReviewId</code> instances.
   */
  public List<ReviewId> getReviewIdList() {
    return new ArrayList<ReviewId>(this.reviewIdMap.values());
  }

  /**
   * Gets the <code>ReviewId</code> instance from the reviewIdName. Returns null if the <code>reviewIdName</code> does
   * not exist.
   * 
   * @param reviewIdName the review id name.
   * @return the <code>ReviewId</code> instance from the reviewIdName. Returns null if the review id name does not
   *         exist.
   */
  public ReviewId getReviewId(final String reviewIdName) {
    return this.reviewIdMap.get(reviewIdName);
  }

  /**
   * Adds <code>ReviewResource</code> instance to the property config file.
   * 
   * @param reviewResource the <code>ReviewResource</code> instance.
   * @throws ReviewException if the review id could not be written.
   * @return <code>true</code> if review id does not exist and could be written. <code>false</code> if review id already
   *         exist.
   */
  public boolean addReviewResource(final ReviewResource reviewResource) throws ReviewException {
    if (reviewResource != null) {
      Review review = reviewResource.getReview();
      ReviewId reviewId = reviewResource.getReviewId();

      this.property.getReview().add(review);
      PropertyXmlSerializer.serializeProperty(this.property, this.project);
      this.reviewIdReviewMap.put(reviewId.getReviewId(), review);
      this.reviewIdMap.put(reviewId.getReviewId(), reviewId);
      return true;
    }

    return false;
  }

  /**
   * Removes <code>ReviewId</code> instance from the property config file.
   * 
   * @param reviewId the <code>ReviewId</code> instance.
   * @throws ReviewException if the review id could not be written.
   * @return <code>true</code> if review id exists and could be written. <code>false</code> if review id does not exist.
   */
  public boolean removeReviewResource(final ReviewId reviewId) throws ReviewException {
    ReviewResource reviewResource = getReviewResource(reviewId.getReviewId(), false);
    if (reviewResource != null) {
      Review review = reviewResource.getReview();
      this.property.getReview().remove(review);
      PropertyXmlSerializer.serializeProperty(this.property, this.project);
      this.reviewIdReviewMap.remove(reviewId.getReviewId());
      this.reviewIdMap.remove(reviewId.getReviewId());
      return true;
    }

    return false;
  }

  /**
   * Exports the selected Review Ids to external file While exporting, it strips off the instance specific data like
   * date, reviewers etc.
   * 
   * @param it Iterator for ReviewId to be exported
   * @param file File where to export the selected reviews
   * @return true if the export was suscessful
   * @throws ReviewException Error while exporting reviews
   */
  public boolean exportReviews(final Iterator<ReviewId> it, final File file) throws ReviewException {
    Property p = (new ObjectFactory()).createProperty();
    List<Review> reviews = p.getReview();
    while (it.hasNext()) {
      ReviewId reviewId = it.next();
      String reviewIdString = reviewId.getReviewId();
      // Get a copy of ReviewResource
      ReviewResource reviewResource = getReviewResource(reviewIdString, true);
      Review review = reviewResource.getReview();

      // Reset the fields, not relevant for exporting
      review.setAuthor(null);
      CreationDate date = review.getCreationDate();
      date.setFormat("");
      date.setValue("");
      review.getFiles().getEntry().clear();
      review.getReviewers().getEntry().clear();
      reviews.add(review);
    }

    PropertyXmlSerializer.saveProperty(p, file);

    return true;

  }

  /**
   * Parses the given review template file and returns the reviews
   * 
   * @param file review template file
   * @return list of Reviews present in the given file
   * @throws ReviewException exception thrown if there are any errors while parsing.
   */
  public List<Review> getReviews(final File file) throws ReviewException {
    Property p = PropertyXmlSerializer.readProperty(file);
    List<Review> reviews = p.getReview();
    return reviews;
  }

}
