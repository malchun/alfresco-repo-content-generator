package com.itdhq.contentLoader;

import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.namespace.QName;

import java.util.List;
import java.util.Map;

/**
 * Created by malchun on 2/25/16.
 */
class PropertiesInfoRepoImpl implements PropertiesInfo {

    private DictionaryService dictionaryService;
    private List<PropertyDefinition> properties;

    public PropertiesInfoRepoImpl(QName model, DictionaryService ds)
    {
        dictionaryService = ds;
        DataTypeDefinition typeDef = dictionaryService.getDataType(model);
        //Map<QName, PropertyDefinition> typeProps = dictionaryService.getAllTypeProperties(typeDef);

    }

    @Override
    public List<PropertyDefinition> getProperties()
    {
        return null;
    }

    @Override
    public Map<QName, ConstraintDefinition> getPropertiesWithConstraints()
    {
        return null;
    }

    @Override
    public Map<QName, QName> getPropertiesWithDataType()
    {
        return null;
    }
}
