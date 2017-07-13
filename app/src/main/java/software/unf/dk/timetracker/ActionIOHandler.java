package software.unf.dk.timetracker;

import android.util.Log;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Handles processing of Action XML file(s)
 *
 * File syntax:
 * <actions>
 *     <action name="" classification="" time=""/>
 * </actions>
 */

class ActionIOHandler extends IOHandler {
    private DocumentBuilder builder;
    private Document document;

    // Instantiates, anc create a fil.
    ActionIOHandler(File file) {
        super(file);
        final String DOCUMENT_HEADER = "<?xml version=\"1.0\"?>\n<actions>\n</actions>";
        try {
            // Create file if it doesn't exist
            if (file.createNewFile() || file.length() == 0) {
                PrintWriter writer = new PrintWriter(file, "UTF-8");
                writer.println(DOCUMENT_HEADER);
                writer.close();
            }
            // Initialise parser objects
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            // Parse file
            document = builder.parse(file);
            // document = builder.parse(file);
        } catch (ParserConfigurationException e) {
            // TODO: Actually handle exceptions
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Error", "File input/output error");
            e.printStackTrace();
        } catch (SAXException e) {
            Log.e("Error", "XML parsing error");
            e.printStackTrace();
        }
    }

    /**
     * Parses the file the IOHandler handles and returns an ArrayList with all the actions inside it
     * @return An ArrayList of the Actions in the XML file
     */
    ArrayList<Action> parseActions() {
        ArrayList<Action> actionList = new ArrayList<>();
        Log.e("Info", "File length is " + file.length());
        try {
            document = builder.parse(file);
            // get root XML element
            Element root = document.getDocumentElement();
            // Check proper root tag
            if ((!root.getTagName().equals("actions")) || root.getNodeType() != Node.ELEMENT_NODE) {
                throw new IOException();
            }
            // Get action nodes
            NodeList actionNodes = root.getElementsByTagName("action");
            for (int i = 0; i < actionNodes.getLength(); i++) {
                Node actionNode = actionNodes.item(i);
                // Check proper tag name
                if (actionNode.getNodeType() != Node.ELEMENT_NODE || !(actionNode.getNodeName().equals("action") )) {
                    throw new IOException();
                }

                // Cast to Element
                Element actionElement = (Element) actionNode;
                // Get name
                String name = actionElement.getAttribute("name");
                // Get classification
                Classification classification = Classification.getClassificationByName(actionElement.getAttribute("classification"));
                // Get time
                String dateString = actionElement.getAttribute("time");
                Log.i("Info", "dateString is: " + dateString);
                Date date = Action.stringToDate(dateString);
                if (date == null) {
                    Log.w("TimeTracker", "Illegal date string, assuming now");
                    date = new Date();
                }
                String description = actionElement.getTextContent();
                // Create action with values
                Action action = new Action(name, classification, date, description);
                actionList.add(action);
            }
        } catch (IOException e) {
            Log.e("Error", "File input/output error");
            e.printStackTrace();
        } catch (SAXException e) {
            Log.e("Error", "XML Parsing error");
        }
        return actionList;
    }

    /**
     * Saves the specified ArrayList of Actions in the IOHandler's file
     * @param actionList ArrayList of Actions to be saved
     */
    void writeActions(ArrayList<Action> actionList) {
        Document document = builder.newDocument();

        // Create root element
        Element root = document.createElement("actions");
        document.appendChild(root);

        // Create Action elements
        for (int i = 0; i < actionList.size(); i++) {
            Action action = actionList.get(i);
            Element actionElement = document.createElement("action");

            // Set attributes
            Attr nameAttribute = document.createAttribute("name");
            nameAttribute.setValue(action.getName());
            actionElement.setAttributeNode(nameAttribute);

            Attr classAttribute = document.createAttribute("classification");
            classAttribute.setValue(action.getClassification().getName());
            actionElement.setAttributeNode(classAttribute);

            Attr timeAttribute = document.createAttribute("time");
            timeAttribute.setValue(Action.dateToString(action.getDate()));
            actionElement.setAttributeNode(timeAttribute);
            String description = action.getDescription();
            actionElement.setTextContent(description);

            // Add element to root
            root.appendChild(actionElement);
        }

        // Write XML file
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        } catch (Exception e) {
            Log.e("Error", "Failed to write XML file");
            e.printStackTrace();
        }
    }
}
