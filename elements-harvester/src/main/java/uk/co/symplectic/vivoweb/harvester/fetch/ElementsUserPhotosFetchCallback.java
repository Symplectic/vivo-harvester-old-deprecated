/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch;

import uk.co.symplectic.utils.ImageUtils;
import uk.co.symplectic.vivoweb.harvester.fetch.resources.PostFetchCallback;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStore;
import uk.co.symplectic.xml.XMLAttribute;
import uk.co.symplectic.xml.XMLUtils;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

public class ElementsUserPhotosFetchCallback implements PostFetchCallback {
    List<XMLAttribute> objectAttributes = null;
    private ElementsRdfStore rdfStore = null;
    private File vivoImageDir;
    private String imageUrlBase = "/harvestedImages/";

    private static String baseUrl      = "http://vivo.symplectic.co.uk/individual/";

    private static int VIVO_THUMBNAIL_WIDTH = 200;
    private static int VIVO_THUMBNAIL_HEIGHT = 200;

    public ElementsUserPhotosFetchCallback(List<XMLAttribute> objectAttributes, ElementsRdfStore rdfStore, File vivoImageDir, String imageUrlBase) {
        this.objectAttributes = objectAttributes;
        this.rdfStore = rdfStore;
        this.vivoImageDir = vivoImageDir;
        if (imageUrlBase != null) {
            this.imageUrlBase = imageUrlBase;
        }
    }

    @Override
    public void fetchSuccess(File downloadedFile) {
        String username = getUsername(objectAttributes);
        BufferedImage image = ImageUtils.readFile(downloadedFile);
        if (image != null) {
            // Write out full size image
            File fullImageDir = new File(new File(vivoImageDir, "harvestedImages"), "fullImages");
            if (!fullImageDir.exists()) {
                fullImageDir.mkdirs();
            }

            ImageUtils.writeFile(image, new File(fullImageDir, username + ".jpg"), "jpeg");

            // Write out thumbnail
            File thumbnailDir = new File(new File(vivoImageDir, "harvestedImages"), "thumbnails");
            if (!thumbnailDir.exists()) {
                thumbnailDir.mkdirs();
            }

            ImageUtils.writeFile(ImageUtils.getScaledInstance(image, VIVO_THUMBNAIL_WIDTH, VIVO_THUMBNAIL_HEIGHT, true), new File(thumbnailDir, username + ".thumbnail.jpg"), "jpeg");

            // Write out XML
            Writer photoXml = new StringWriter();
            try {
                photoXml.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                photoXml.write("<rdf:RDF xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:score=\"http://vivoweb.org/ontology/score#\" xmlns:vitro=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#\" xmlns:bibo=\"http://purl.org/ontology/bibo/\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:ufVivo=\"http://vivo.ufl.edu/ontology/vivo-ufl/\" xmlns:owlPlus=\"http://www.w3.org/2006/12/owl2-xml#\" xmlns:svo=\"http://www.symplectic.co.uk/vivo/\" xmlns:skos=\"http://www.w3.org/2008/05/skos#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema#\" xmlns:api=\"http://www.symplectic.co.uk/publications/api\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:vitro-public=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#\" xmlns:vocab=\"http://purl.org/vocab/vann/\" xmlns:core=\"http://vivoweb.org/ontology/core#\" xmlns:swvocab=\"http://www.w3.org/2003/06/sw-vocab-status/ns#\">");

                photoXml.write("<rdf:Description rdf:about=\"" + baseUrl + username + "\">");
                photoXml.write("    <vitro-public:mainImage rdf:resource=\"" + baseUrl + username + "-image\"/>");
                photoXml.write("</rdf:Description>");

                photoXml.write("<rdf:Description rdf:about=\"" + baseUrl + username + "-image\">");
                photoXml.write("    <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>");
                photoXml.write("    <rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#File\"/>");
                photoXml.write("    <vitro-public:downloadLocation rdf:resource=\"" + baseUrl + username + "-imageDownload\"/>");
                photoXml.write("    <vitro-public:thumbnailImage rdf:resource=\"" + baseUrl + username + "-imageThumbnail\"/>");
                photoXml.write("    <vitro-public:filename>" + username + ".jpg</vitro-public:filename>");
                photoXml.write("    <vitro-public:mimeType>image/jpeg</vitro-public:mimeType>");
                photoXml.write("</rdf:Description>");

                photoXml.write("<rdf:Description rdf:about=\"" + baseUrl + username + "-imageDownload\">");
                photoXml.write("    <rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#FileByteStream\"/>");
                photoXml.write("    <vitro-public:directDownloadUrl>" + imageUrlBase + "fullImages/" + username + ".jpg</vitro-public:directDownloadUrl>");
                photoXml.write("</rdf:Description>");

                photoXml.write("<rdf:Description rdf:about=\"" + baseUrl + username + "-imageThumbnail\">");
                photoXml.write("    <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>");
                photoXml.write("    <rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#File\"/>");
                photoXml.write("    <vitro-public:downloadLocation rdf:resource=\"" + baseUrl + username + "-imageThumbnailDownload\"/>");
                photoXml.write("    <vitro-public:filename>" + username + ".thumbnail.jpg</vitro-public:filename>");
                photoXml.write("    <vitro-public:mimeType>image/jpeg</vitro-public:mimeType>");
                photoXml.write("</rdf:Description>");

                photoXml.write("<rdf:Description rdf:about=\"" + baseUrl + username + "-imageThumbnailDownload\">");
                photoXml.write("    <rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#FileByteStream\"/>");
                photoXml.write("    <vitro-public:directDownloadUrl>" + imageUrlBase + "thumbnails/" + username + ".thumbnail.jpg</vitro-public:directDownloadUrl>");
                photoXml.write("</rdf:Description>");

                photoXml.write("</rdf:RDF>");

                rdfStore.writeObjectExtra(objectAttributes, "photo", photoXml.toString());
            } catch (IOException e) {

            } finally {
            }
        }
    }

    private String getId(List<XMLAttribute> attributeList) {
        XMLAttribute idAttr = XMLUtils.getAttribute(attributeList, null, "id");
        if (idAttr == null) {
            throw new IllegalStateException();
        }

        return idAttr.getValue();
    }

    private String getUsername(List<XMLAttribute> attributeList) {
        XMLAttribute idAttr = XMLUtils.getAttribute(attributeList, null, "username");
        if (idAttr == null) {
            throw new IllegalStateException();
        }

        return idAttr.getValue();
    }
}

/*
<rdf:Description rdf:about="{$baseURI}peopleImage/ufid{self::*}">
<public:mainImage rdf:resource="{$baseURI}mainImg/ufid{self::*}"/>
<ufVivo:ufid><xsl:value-of select="substring(current(),1,8)" /></ufVivo:ufid>
</rdf:Description>

<rdf:Description rdf:about="{$baseURI}mainImg/ufid{self::*}">
<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/public#File"/>
<public:downloadLocation rdf:resource="{$baseURI}fullDirDownload/ufid{self::*}"/>
<public:thumbnailImage rdf:resource="{$baseURI}thumbImg/ufid{self::*}"/>
<public:filename rdf:datatype="http://www.w3.org/2001/XMLSchema#string"><xsl:value-of select="." /></public:filename>
<public:mimeType rdf:datatype="http://www.w3.org/2001/XMLSchema#string">image/<xsl:value-of select="substring(current(),10)" /></public:mimeType>
</rdf:Description>

<rdf:Description rdf:about="{$baseURI}thumbImg/ufid{self::*}">
<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/public#File"/>
<public:downloadLocation rdf:resource="{$baseURI}thumbDirDownload/ufid{self::*}"/>
<public:filename rdf:datatype="http://www.w3.org/2001/XMLSchema#string">thumbnail<xsl:value-of select="." /></public:filename>
<public:mimeType rdf:datatype="http://www.w3.org/2001/XMLSchema#string">image/<xsl:value-of select="substring(current(),10)" /></public:mimeType>
</rdf:Description>

<rdf:Description rdf:about="{$baseURI}thumbDirDownload/ufid{self::*}">
<public:directDownloadUrl>/harvestedImages/thumbnails/thumbnail<xsl:value-of select="." /></public:directDownloadUrl>
<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/public#FileByteStream"/>
<vitro:modTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="datetime:dateTime()" /></vitro:modTime>
</rdf:Description>

<rdf:Description rdf:about="{$baseURI}fullDirDownload/ufid{self::*}">
<public:directDownloadUrl>/harvestedImages/fullImages/<xsl:value-of select="." /></public:directDownloadUrl>
<rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/public#FileByteStream"/>
<vitro:modTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="datetime:dateTime()" /></vitro:modTime>
</rdf:Description>
*/