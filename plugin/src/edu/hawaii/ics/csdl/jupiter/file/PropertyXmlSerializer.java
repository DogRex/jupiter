package edu.hawaii.ics.csdl.jupiter.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javanet.staxutils.StaxUtilsXMLOutputFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

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
    return readProperty(project);
  }

  // Parse the xml file to property object using jaxb
  private static Property readProperty(final IProject project) throws ReviewException {

    IFile jupiterConfigIFile = project.getFile(PROPERTY_XML_FILE);
    File jupiterConfigFile = jupiterConfigIFile.getLocation().toFile();

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

    StaxUtilsXMLOutputFactory xmlof = new StaxUtilsXMLOutputFactory(XMLOutputFactory.newInstance());
    xmlof.setProperty(StaxUtilsXMLOutputFactory.INDENTING, true);
    XMLStreamWriter writer = null;
    try {
      writer = xmlof.createXMLStreamWriter(new FileOutputStream(outputPropertyFile), "UTF-8");
      writer.writeStartDocument("UTF-8", "1.0");

      writer.writeStartElement(PropertyConstraints.ELEMENT_PROPERTY);

      List<Review> reviews = property.getReview();
      for (Review review : reviews) {
        StaxPropertyXmlUtil.writeReview(writer, review);
      }

      writer.writeEndElement(); // Property
    }
    catch (FileNotFoundException e) {
      throw new ReviewException("FileNotFoundException: " + e.getMessage(), e);
    }
    catch (XMLStreamException e) {
      throw new ReviewException("XMLStreamException: " + e.getMessage(), e);
    }
    finally {
      if (writer != null) {
        try {
          writer.close();
        }
        catch (XMLStreamException e) {
          log.error(e);
        }

        try {
          // try to refresh the resource since we wrote to it
          outputPropertyIFile.refreshLocal(IResource.DEPTH_ONE, null);
        }
        catch (CoreException e) {
          log.error(e);
        }
      }
    }
  }

  /**
   * Copies default config file in the <code>Project</code>. Leave the current config file in the project if the file
   * already exists.
   * 
   * @param outputPropertyFile the output property file.
   * @return the config file <code>File</code> instance.
   * @throws IOException if problems occur.
   * @throws CoreException if problems occur.
   */
  private static File copyDefaultConfigFileTo(final File outputPropertyFile) throws IOException, CoreException {
    // System.out.println("about to copy a file to " + outputPropertyFile);
    if (!outputPropertyFile.exists()) {
      outputPropertyFile.createNewFile();
    }

    URL pluginUrl = ReviewPlugin.getInstance().getInstallURL();
    // System.out.println(pluginUrl.getFile());
    URL xmlUrl = FileLocator.toFileURL(new URL(pluginUrl, DEFAULT_PROPERTY_XML_FILE));
    // System.out.println("From : " + xmlUrl);

    File sourceXmlFile = new File(xmlUrl.getFile());
    // copy XML file in the plug-in directory to the state location.
    // System.out.println("From : " + sourceXmlFile);
    FileUtil.copy(sourceXmlFile, outputPropertyFile);
    return outputPropertyFile;
  }

  /**
   * Loads the default review from property.xml.
   * 
   * @return Returns the <code>Review</code> object or null.
   */
  public static Review cloneDefaultReview() {
    URL pluginUrl = ReviewPlugin.getInstance().getInstallURL();

    XMLInputFactory xmlif = XMLInputFactory.newInstance();
    xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
    xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);

    XMLStreamReader reader = null;
    try {
      URL xmlUrl = FileLocator.toFileURL(new URL(pluginUrl, DEFAULT_PROPERTY_XML_FILE));

      reader = xmlif.createXMLStreamReader(xmlUrl.getFile(), new FileInputStream(xmlUrl.getFile()));

      Property property = StaxPropertyXmlUtil.parsePropertyFile(reader);
      // there should only be the default review in the list
      return property.getReview().get(0);
    }
    catch (IOException e) {
      log.error(e);
    }
    catch (XMLStreamException e) {
      log.error(e);
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch (XMLStreamException e) {
          log.error(e);
        }
      }
    }
    return null;
  }
}
