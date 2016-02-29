# alfresco-repo-content-generator v0.0.1
Second PoC

## Usage
Content generator is configuring with 2 conf files:
* generator-config/data-struct.xml

Explains the structure of content. Example:

``` xml
<data store="workspace://SpacesStore" path="Company Home/Test folder">

    <node object="folder" count="3" name="AAAA">
        <node object="folder" count="100" name="XYZ">
          <node object="document" count="2" name="BBB"/>
          <node object="document" count="2" name="CCCCC"/>
        </node>
        <node object="another-document" count="2" name="aergheargh">
        </node>
    </node>
</data>
```

* generator-config/objects-config.xml

Describes types of node. Example:

```xml
<objects>
    <object id="folder">
        <type>{http://www.alfresco.org/model/content/1.0}folder</type>
    </object>

    <object id="document">
        <type>{http://www.alfresco.org/model/content/1.0}content</type>
        <content>
            <format>txt</format>
            <minTextSize>1024</minTextSize>
            <maxTextSize>10240</maxTextSize>
        </content>
    </object>

    <object id="another-document">
        <type>{http://some-custom-model/basic}another-document</type>
        <content>
            <format>txt</format>
            <minTextSize>1024</minTextSize>
            <maxTextSize>10240</maxTextSize>
        </content>
    </object>
</objects>
```

Very unstable dev version
