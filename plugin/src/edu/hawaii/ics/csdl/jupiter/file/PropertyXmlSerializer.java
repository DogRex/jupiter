package edu.hawaii.ics.csdl.jupiter.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javanet.staxutils.StaxUtilsXMLOutputFactory;

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
    IFile jupiterConfigIFile = project.getFile(PropertyResource.PROPERTY_XML_FILE);
    File jupiterConfigFile = jupiterConfigIFile.getLocation().toFile();
    Property property = null;

    if (jupiterConfigFile.exists()) {
      property = parseProperty(jupiterConfigFile);
    }
    else {
      // parse the defaults
      if (FileResource.getActiveProject().getName().equals(project.getName())) {
        File configFile;
        try {
          configFile = copyDefaultConfigFileTo(jupiterConfigFile);
          property = parseProperty(configFile);
          jupiterConfigIFile.refreshLocal(IResource.DEPTH_ONE, null);
        }
        catch (IOException e) {
          throw new ReviewException("IOException : " + e.getMessage(), e);
        }
        catch (CoreException e) {
          throw new ReviewException("CoreException : " + e.getMessage(), e);
        }
      }
    }

    return property;
  }

  /**
   * Parses the given XML file and return Property object
   * 
   * @param file XML file containing property
   * @return parsed Property object
   * @throws ReviewException if an error occurs during parsing
   */
  public static Property parseProperty(final File file) throws ReviewException {
    Property property = null;

    XMLInputFactory xmlif = XMLInputFactory.newInstance();
    xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
    xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
    XMLStreamReader reader = null;

    try {
      reader = xmlif.createXMLStreamReader(file.getAbsolutePath(), new FileInputStream(file));
      property = StaxPropertyXmlUtil.parsePropertyFile(reader);
    }
    catch (FileNotFoundException e) {
      throw new ReviewException("FileNotFoundException : " + e.getMessage(), e);
    }
    catch (XMLStreamException e) {
      throw new ReviewException("XMLStreamException : " + e.getMessage(), e);
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
    return property;
  }

  /**
   * Serializes a <code>Property</code> to the jupiter config.
   * 
   * @param property The properties to save.
   * @param outputPropertyFile file to serialize the given Property object
   * @throws ReviewException Thrown if there is an error during serialization.
   */
  public static void serializeProperty(final Property property, final File outputPropertyFile) throws ReviewException {
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
    URL xmlUrl = FileLocator.toFileURL(new URL(pluginUrl, PropertyResource.DEFAULT_PROPERTY_XML_FILE));
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
      URL xmlUrl = FileLocator.toFileURL(new URL(pluginUrl, PropertyResource.DEFAULT_PROPERTY_XML_FILE));

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
