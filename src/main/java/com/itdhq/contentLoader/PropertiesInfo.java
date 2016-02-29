package com.itdhq.contentLoader;

import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

import java.util.List;
import java.util.Map;

/**
 * Created by malchun on 2/25/16.
 */
public interface PropertiesInfo
{
    List<PropertyDefinition> getProperties();
    Map<QName, ConstraintDefinition> getPropertiesWithConstraints();
    Map<QName, QName> getPropertiesWithDataType();
}
