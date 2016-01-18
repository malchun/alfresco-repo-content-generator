package com.itdhq.contentLoader;

import org.alfresco.service.namespace.QName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by malchun on 12/20/15.
 */
public class RepoObject {
    private String name;
    private QName type;
    private String contentType;
    private boolean fill = false;
    private int minSize = 0;
    private int maxSize = 0;
    private List<Integer> size = new ArrayList<Integer>();


    public RepoObject(String name, String type)
    {
        this.name = name;
        this.type = QName.createQName(type);
    }

    public boolean addContentProps(String type, int minSize, int maxSize)
    {
        // Because you have no wheel to change content type!
        if (fill) {
            return false;
        }
        fill = true;
        contentType = type;
        this.minSize = minSize;
        this.maxSize = maxSize;
        return true;
    }

    // TODO check could be better
    public String getContentType()
    {
        if(fill) {
            return contentType;
        } else {
            return "";
        }
    }

    public int getMinSize() { return minSize; }
    public int getMaxSize() { return maxSize; }

    public QName getType() { return type; }
    public String toString()
    {
        return "Name : " + name + " Type : " + type;
    }
    public boolean hasContent() { return fill; }
}
