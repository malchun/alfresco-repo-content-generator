package com.itdhq.contentLoader;

import org.apache.commons.el.parser.ParseException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by malchun on 12/20/15.
 */
public class ObjectsConfigParser {
    private Logger logger = Logger.getLogger(ObjectsConfigParser.class);
    private Map<String, RepoObject> repoObjects = new HashMap<>();

    public ObjectsConfigParser(String path) throws ParseException {
        logger.debug("ObjectsConfigParser constructor on file " + path);
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            File dataObjectsConfigFile = new File(getClass().getClassLoader().getResource(path).getFile());
            Document dataObjectsConfig = dBuilder.parse(dataObjectsConfigFile);
            dataObjectsConfig.getDocumentElement().normalize();
            NodeList objects = dataObjectsConfig.getDocumentElement().getChildNodes();

            for (int i = 0; i < objects.getLength(); ++i)
            {
                if (org.w3c.dom.Node.ELEMENT_NODE == objects.item(i).getNodeType()) {
                    Element eltmp = (Element) objects.item(i);

                    repoObjects.put(eltmp.getAttribute("id"),
                            new RepoObject(
                                eltmp.getAttribute("id"),
                                eltmp.getElementsByTagName("type").item(0).getTextContent()
                            )
                        );
                }
            }

        } catch(Exception e) {
            logger.debug("Bad objects config");
            throw new ParseException("Bad objects config!");
        }
        addRootObject();
    }


    // TODO del and make normal root!
    // Only for testing purposes!!!!1111
    private void addRootObject()
    {
        repoObjects.put("root", new RepoObject(
                    "root",
                    "{http://www.alfresco.org/model/content/1.0}folder"
                )
            );
    }

    public Map<String, RepoObject> getObjects() { return repoObjects; }
}
