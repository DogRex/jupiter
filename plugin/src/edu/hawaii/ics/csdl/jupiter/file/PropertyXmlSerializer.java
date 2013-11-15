package edu.hawaii.ics.csdl.jupiter.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;

import edu.hawaii.ics.csdl.jupiter.ReviewException;
import edu.hawaii.ics.csdl.jupiter.ReviewPlugin;
import edu.hawaii.ics.csdl.jupiter.file.property.Property;
import edu.hawaii.ics.csdl.jupiter.file.property.Review;
import edu.hawaii.ics.csdl.jupiter.util.JupiterLogger;

/**
 * Provides an utility for property config XML.
 * 
 * @author Takuya Yamashita
 * @version $Id: PropertyXmlSerializer.java 179 2010-07-01 09:54:42Z jsakuda $
 */
public class PropertyXmlSerializer {

  /** Jupiter logger */
  private static JupiterLogger log = JupiterLogger.getLogger();

  private static final String DEFAULT_PROPERTY_XML_FILE = "property.xml";
  /** The property XML file name. */
  public static final String PROPERTY_XML_FILE = ".jupiter";
  /* package for the POJO classes of Property */
  public static final String PROPERTY_PACKAGE_NAMESPACE = "edu.hawaii.ics.csdl.jupiter.file.property";


  /**
   * Prohibits instantiation.
   */
  private PropertyXmlSerializer() {}

  /**
   * Creates the new <code>Property</code> config instance in the <code>IProject</code>.
   * 
   * @param project the project
   * @return the new <code>Property</code> instance.
   * @throws ReviewException if an error occurs during the new document creation.
   */
  public static Property newProperty(final IProject project) throws ReviewException {
    IFile jupiterConfigIFile = project.getFile(PROPERTY_XML_FILE);
    File jupiterConfigFile = jupiterConfigIFile.getLocation().toFile();

    return readProperty(jupiterConfigFile);
  }

  // Parse the xml file to property object using jaxb
  private static Property readProperty(final File jupiterConfigFile) throws ReviewException {
    JAXBContext jaxbContext;
    try {
      jaxbContext = JAXBContext.newInstance(PROPERTY_PACKAGE_NAMESPACE);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      try {
        Object obj = unmarshaller.unmarshal(new FileInputStream(jupiterConfigFile));
        Property zooInfo = (Property) obj;
        return zooInfo;
      }
      catch (FileNotFoundException e) {
        throw new ReviewException("FileNotFoundException: " + e.getMessage(), e);
      }

    }
    catch (JAXBException e) {
      throw new ReviewException("JAXBException: " + e.getMessage(), e);
    }

  }

  /**
   * Serializes a <code>Property</code> to the jupiter config.
   * 
   * @param property The properties to save.
   * @param project The project that the property is for.
   * @throws ReviewException Thrown if there is an error during serialization.
   */
  public static void serializeProperty(final Property property, final IProject project) throws ReviewException {
    IFile outputPropertyIFile = project.getFile(PROPERTY_XML_FILE);
    try {
      // try to refresh the resource since some plugins (CVS)
      // don't refresh after updating the project files
      outputPropertyIFile.refreshLocal(IResource.DEPTH_ONE, null);
    }
    catch (CoreException e) {
      log.error(e);
    }
    File outputPropertyFile = outputPropertyIFile.getLocation().toFile();
    saveProperty(property, outputPropertyFile);
  }

  // Serialize property to xml using jaxb
  private static void saveProperty(final Property property, final File outputPropertyFile) throws ReviewException {
    // create an element for marshalling
    // Property zooInfoElement = (new edu.hawaii.ics.csdl.jupiter.file.property.ObjectFactory()).createProperty();

    // create a Marshaller and marshal to System.out
    try {
      JAXB.marshal(property, new FileOutputStream(outputPropertyFile));
    }
    catch (FileNotFoundException e) {
      throw new ReviewException("FileNotFoundException: " + e.getMessage(), e);
    }

  }

  /**
   * Loads the default review from property.xml.
   * 
   * @return Returns the <code>Review</code> object or null.
   */
  public static Review cloneDefaultReview() {
    URL pluginUrl = ReviewPlugin.getInstance().getInstallURL();

    try {
      URL xmlUrl = FileLocator.toFileURL(new URL(pluginUrl, DEFAULT_PROPERTY_XML_FILE));
      try {
        Property property = readProperty(new File(xmlUrl.getFile()));
        // there should only be the default review in the list
        return property.getReview().get(0);
      }
      catch (ReviewException e) {
        log.error(e);
      }
    }
    catch (IOException e) {
      log.error(e);
    }
    return null;
  }
}
