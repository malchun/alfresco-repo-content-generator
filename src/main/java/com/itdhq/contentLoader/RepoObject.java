package com.itdhq.contentLoader;

import org.alfresco.service.namespace.QName;

/**
 * Created by malchun on 12/20/15.
 */
public class RepoObject {
    private String name;
    private QName type;

    public RepoObject(String name, String type)
    {
        this.name = name;
        this.type = QName.createQName(type);
    }

    public QName getType() { return type; }
    public String toString()
    {
        return "Name : " + name + " Type : " + type;
    }
}
