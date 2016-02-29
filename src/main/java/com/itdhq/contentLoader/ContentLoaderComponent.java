/*
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.itdhq.contentLoader;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.digester.annotations.rules.AttributeCallParam;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.james.mime4j.dom.datetime.DateTime;
import org.apache.log4j.Logger;
import org.springframework.util.PropertiesPersister;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A basic component that will be started for this module.
 * Uses the NodeLocatorService to easily find nodes and the
 * NodeService to display them
 *
 * @author Gabriele Columbro
 * @author Maurizio Pillitu
 */
public class ContentLoaderComponent
        extends AbstractModuleComponent {
    private Logger logger = Logger.getLogger(ContentLoaderComponent.class);

    private Map<String, RepoObject> objects;

    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private ContentService contentService;
    private DictionaryService dictionaryService;
    private String objectsConfig;
    private String dataStruct;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setObjectsConfig(String objectsConfig) {
        this.objectsConfig = objectsConfig;
    }

    public void setDataStruct(String dataStruct) {
        this.dataStruct = dataStruct;
    }


    /**
     * Start method
     */
    @Override
    protected void executeInternal() throws Throwable {
        logger.debug("ContentLoaderComponent");
        objects = (new ObjectsConfigParser(objectsConfig)).getObjects();
        logger.debug(objects.toString());

        /*
        for (QName i : dictionaryService.getAllModels()) {
            for (ConstraintDefinition constraint : dictionaryService.getConstraints(i)) {
                logger.debug(constraint.getConstraint().getShortName() + " " + constraint.getConstraint().getParameters());
            }
        }
        */

        for (QName i : dictionaryService.getAllDataTypes()) {
            logger.debug("DataType: " + i.toString());
            /*
            for (QName j : dictionaryService.getAllProperties(i)) {
                logger.debug(j); //+ dictionaryService.getConstraint(j).toString());
            }
            */
        }

        this.parseRootNode(dataStruct);
/*
        logger.debug(nodeService.getProperty(test_folder, ContentModel.PROP_NAME));
        for (int i = 0; i < 20; ++i) {
            NodeRef tmp = createDocument(test_folder, "test document " + Integer.toString(i));
            logger.debug(nodeService.getProperty(tmp, ContentModel.PROP_NAME));
        }
*/
    }


    /**
     * Init parsing method. Should be totally refactored.
     *
     * @param file - path to config
     */
    public void parseRootNode(String file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        File dataStructFile = new File(getClass().getClassLoader().getResource(file).getFile());
        Document dataStructConfig = dBuilder.parse(dataStructFile);
        dataStructConfig.getDocumentElement().normalize();

        Node root = dataStructConfig.getDocumentElement();
        NodeRef root_folder = null;

        if (Node.ELEMENT_NODE == root.getNodeType() && root.getNodeName().equals("data")) {
            Element tmpEl = (Element) root;
            if ("data" == tmpEl.getNodeName()) {
                // TODO add correct regexp checker and refactor!!!!
                logger.debug(tmpEl.getAttribute("store") + "/" + tmpEl.getAttribute("path"));
                StoreRef spaces = this.getStoreRefByName(tmpEl.getAttribute("store"));
                List<String> path = Arrays.asList(tmpEl.getAttribute("path").split("/"));
                root_folder = nodeService.getRootNode(spaces);
                logger.debug(nodeService.getProperty(root_folder, ContentModel.PROP_NAME));
                for (String i : path) {
                    logger.debug("cm:Name " + i);
                    root_folder = this.createDocument(root_folder, i, objects.get("root"));
                    logger.debug(nodeService.getProperty(root_folder, ContentModel.PROP_NAME));
                }
            } else {
                logger.debug("Not root!");
            }
            NodeList nodes = tmpEl.getChildNodes();
            for (int i = 0; i < nodes.getLength(); ++i) {
                parseNode(root_folder, nodes.item(i));
            }
        }
    }


    /**
     * Usual node parser
     *
     * @param parent
     * @param node
     */
    private void parseNode(NodeRef parent, Node node) {
        logger.debug("parseNode");
        if (Node.ELEMENT_NODE == node.getNodeType() && node.getNodeName().equals("node")) {
            Element tmpEl = (Element) node;
            logger.debug(tmpEl.getNodeName() + "; name: " + tmpEl.getAttribute("name") + "; object: " + tmpEl.getAttribute("object") + "; count: " + tmpEl.getAttribute("count"));
            int count = Integer.parseInt(tmpEl.getAttribute("count"));
            if (count > 0) {
                logger.debug(Integer.toString(count) + " - number.");
                NodeRef tmp1 = this.createDocument(parent, tmpEl.getAttribute("name"), objects.get(tmpEl.getAttribute("object")));
                logger.debug(nodeService.getProperty(tmp1, ContentModel.PROP_NAME));
                NodeList nodes = tmpEl.getChildNodes();
                for (int i = 0; i < nodes.getLength(); ++i) {
                    parseNode(tmp1, nodes.item(i));
                }
                for (int j = 1; j < count; ++j) {
                    NodeRef tmp = this.createDocument(parent, tmpEl.getAttribute("name") + " " + Integer.toString(j), objects.get(tmpEl.getAttribute("object")));
                    logger.debug(nodeService.getProperty(tmp, ContentModel.PROP_NAME));
                    nodes = tmpEl.getChildNodes();
                    for (int i = 0; i < nodes.getLength(); ++i) {
                        parseNode(tmp, nodes.item(i));
                    }
                }
            }
        }
    }


    /**
     * Returns store by name or raises exception
     *
     * @param name - name of store with protocol
     * @return StoreRef of store
     * @throws if store not exist
     */
    public StoreRef getStoreRefByName(String name) {
        logger.debug("ContentLoaderComponent.getStoreRefByName");
        List<StoreRef> stores = nodeService.getStores();
        StoreRef spaces = null;
        for (StoreRef i : stores) {
            if (i.toString().equals(name)) {
                spaces = i;
                break;
            }
        }
        if (spaces != null) {
            return spaces;
        } else {
            throw new AlfrescoRuntimeException("Wrong store!");
        }
    }

    /**
     * Returns NodeRef by name or null if not exist
     *
     * @param parent - parent node
     * @param cmname - name of target node
     * @return NodeRef or null
     */
    public NodeRef getChildNodeRefByName(NodeRef parent, String cmname) {
        logger.debug("getChildNodeRefByName");
        NodeRef res = null;
        for (ChildAssociationRef child : nodeService.getChildAssocs(parent)) {
            NodeRef tmp = child.getChildRef();
            //logger.debug(nodeService.getProperty(tmp, ContentModel.PROP_NAME));
            if (nodeService.getProperty(tmp, ContentModel.PROP_NAME).equals(cmname)) {
                res = tmp;
                break;
            }
        }
        return res;
    }

    /**
     * Creates subfolder if parent folder known. In case the subfolder is already exits - only returns NodeRef
     *
     * @param parent - parent node
     * @param cmname - name of new folder
     * @return NodeRef of subfolder
     */
    public NodeRef createSubFolderIfNotExist(NodeRef parent, String cmname) {
        logger.debug("createSubFolderIfNotExist");
        NodeRef res = this.getChildNodeRefByName(parent, cmname);
        if (res == null) {
            res = fileFolderService.create(parent, cmname, ContentModel.TYPE_FOLDER).getNodeRef();
        } else {
            logger.debug("exists");
        }
        return res;
    }

    /**
     * Creates document. Most likely this function would be changed.
     *
     * @param parent - parent node
     * @param cmname - new node name
     * @return NodeRef of new document
     */
    public NodeRef createDocument(NodeRef parent, String cmname, RepoObject object) {
        logger.debug("createDocument");

        NodeRef res = this.getChildNodeRefByName(parent, cmname);
        if (res != null) {
            logger.debug("exist");
            return res;
        }


        res = fileFolderService.create(parent, cmname, object.getType()).getNodeRef();

        fillNodeProperties(res);

        // TODO here will be many types
        if (object.hasContent()) {
            ContentWriter writer = this.contentService.getWriter(res, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.setEncoding("UTF-8");
            writer.putContent(genPlainText(ThreadLocalRandom.current().nextInt(object.getMinSize(), object.getMaxSize() + 1)));
        }
        // TODO null check
        //logger.debug("created: " + nodeService.getProperty(res, ContentModel.PROP_NAME));
        return res;
    }

    /**
     * Plain Text generator
     * TODO find normal human-like text generator
     *
     * @param size in chars
     * @return plain text (letters and numbers)
     */
    private String genPlainText(int size) {
        return RandomStringUtils.randomAlphanumeric(size);
    }

    /**
     * Here you can see a bit of magic!
     */
    private void fillNodeProperties(NodeRef node) {
        logger.debug("ContentLoaderComponent.fillNodeProperties");
        Map<QName, Serializable> props = nodeService.getProperties(node);
        Map<QName, PropertyDefinition> allProps = getAllTypeProperties(node);
        Map<QName, PropertyDefinition> fillingProps = new HashMap<>();
        for (Map.Entry<QName, PropertyDefinition> prop : allProps.entrySet())
        {
            if (!props.containsKey(prop.getKey())) {
                fillingProps.put(prop.getKey(), prop.getValue());
                if (!prop.getValue().getConstraints().isEmpty()) {
                    logger.debug("Constraint types " + prop.getValue().getConstraints().get(0).getConstraint().getType() + "  "  + prop.getValue().getConstraints().get(0).getConstraint().getParameters());

                }
            }
        }
        for (Map.Entry<QName, PropertyDefinition> fillingProp: fillingProps.entrySet()) {
            switch (fillingProp.getValue().getDataType().getName().toString())
            {
                case "{http://www.alfresco.org/model/dictionary/1.0}text":
                    props.put(fillingProp.getKey(), genTextProperty(fillingProp.getValue()));
                    logger.debug("New text property " + fillingProp.getKey() + "  " + genTextProperty(fillingProp.getValue()));
                    break;
                case "{http://www.alfresco.org/model/dictionary/1.0}datetime":
                    props.put(fillingProp.getKey(), genDateTimeProperty(fillingProp.getValue(), 1000));
                    logger.debug("New datetime property " + fillingProp.getKey() + "  " + genDateTimeProperty(fillingProp.getValue(), 1000));
                    break;
                case "{http://www.alfresco.org/model/dictionary/1.0}date":
                    props.put(fillingProp.getKey(), genDateTimeProperty(fillingProp.getValue(), 1000));
                    logger.debug("New date property " + fillingProp.getKey() + "  " + genDateTimeProperty(fillingProp.getValue(), 1000));
                    break;
                case "{http://www.alfresco.org/model/dictionary/1.0}long":
                    props.put(fillingProp.getKey(), genLongProperty(fillingProp.getValue()));
                    logger.debug("New long property " + fillingProp.getKey() + "  " + genLongProperty(fillingProp.getValue()));
                    break;
                case "{http://www.alfresco.org/model/dictionary/1.0}int":
                    props.put(fillingProp.getKey(), genLongProperty(fillingProp.getValue()));
                    logger.debug("New int property " + fillingProp.getKey() + "  " + genIntProperty(fillingProp.getValue(), 1000));
                    break;
            }
        }
        nodeService.setProperties(node, props);
    }

    public Map<QName, PropertyDefinition> getAllTypeProperties(NodeRef ref)
    {
        QName typeName = nodeService.getType(ref);
        TypeDefinition type = dictionaryService.getType(typeName);
        return getAllTypeProperties(type);
    }

    public Map<QName, PropertyDefinition> getAllTypeProperties(TypeDefinition type)
    {
        Map<QName, PropertyDefinition> results = new HashMap<>();

        Map<QName, PropertyDefinition> properties = type.getProperties();
        for (Map.Entry<QName, PropertyDefinition> entry : properties.entrySet())
        {
            PropertyDefinition prop = entry.getValue();
            QName name = entry.getKey();
            results.put(name, prop);
        }

        List<AspectDefinition> aspects = type.getDefaultAspects(true);
        for (AspectDefinition def : aspects)
        {
            properties = def.getProperties();
            for (Map.Entry<QName, PropertyDefinition> entry : properties.entrySet())
            {
                PropertyDefinition prop = entry.getValue();
                QName name = entry.getKey();
                results.put(name, prop);
            }
        }
        return results;
    }

    private long genLongProperty(PropertyDefinition def)
    {
        return ThreadLocalRandom.current().nextLong();
    }

    private int genIntProperty(PropertyDefinition def, int max)
    {
        return ThreadLocalRandom.current().nextInt(max);
    }

    private Date genDateTimeProperty(PropertyDefinition def, int max)
    {
        //long t2 = t1 + 2 * 60 * 1000 + r.nextInt(60 * 1000) + 1;
        return new Date(System.currentTimeMillis() + ThreadLocalRandom.current().nextInt());
        //DateTime d2 = new DateTime(t2);

    }

    private String genTextProperty(PropertyDefinition def)
    {
        if (def.getConstraints().isEmpty()) {
            return genPlainText(ThreadLocalRandom.current().nextInt(5, 100));
        } else {
            if (def.getConstraints().get(0).getConstraint().getType() == ListOfValuesConstraint.CONSTRAINT_TYPE) {
                List<String> list = ((ListOfValuesConstraint)def.getConstraints().get(0).getConstraint()).getAllowedValues();
                return list.get(ThreadLocalRandom.current().nextInt(list.size()));
            }
        }
        return genPlainText(ThreadLocalRandom.current().nextInt(5, 100));
    }
}


