/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch;

import org.apache.commons.lang.StringUtils;
import uk.co.symplectic.utils.ImageUtils;
import uk.co.symplectic.vivoweb.harvester.model.ElementsUserInfo;
import uk.co.symplectic.vivoweb.harvester.fetch.resources.PostFetchCallback;
import uk.co.symplectic.vivoweb.harvester.store.ElementsRdfStore;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class ElementsUserPhotosFetchCallback implements PostFetchCallback {
    private ElementsUserInfo userInfo = null;
    private ElementsRdfStore rdfStore = null;
    private File vivoImageDir;
    private String imageUrlBase = "/harvestedImages/";

    // TODO: Is there a better way? This causes problems...
    private String baseUrl      = "http://vivo.symplectic.co.uk/individual/";

    private static int VIVO_THUMBNAIL_WIDTH = 200;
    private static int VIVO_THUMBNAIL_HEIGHT = 200;

    public ElementsUserPhotosFetchCallback(ElementsUserInfo userInfo, ElementsRdfStore rdfStore, File vivoImageDir, String vivoBaseURI, String imageUrlBase) {
        this.userInfo = userInfo;
        this.rdfStore = rdfStore;
        this.vivoImageDir = vivoImageDir;
        if (!StringUtils.isEmpty(vivoBaseURI)) {
            this.baseUrl = vivoBaseURI;
        }
        if (imageUrlBase != null) {
            this.imageUrlBase = imageUrlBase;
        }
    }

    @Override
    public void fetchSuccess(File downloadedFile) {
        BufferedImage image = ImageUtils.readFile(downloadedFile);
        if (image != null) {
            // Write out full size image
            File fullImageDir = new File(new File(vivoImageDir, "harvestedImages"), "fullImages");
            if (!fullImageDir.exists()) {
                fullImageDir.mkdirs();
            }

            ImageUtils.writeFile(image, new File(fullImageDir, userInfo.getUsername() + ".jpg"), "jpeg");

            // Write out thumbnail
            File thumbnailDir = new File(new File(vivoImageDir, "harvestedImages"), "thumbnails");
            if (!thumbnailDir.exists()) {
                thumbnailDir.mkdirs();
            }

            int targetHeight = ImageUtils.getTargetHeight(image.getWidth(), image.getHeight(), VIVO_THUMBNAIL_WIDTH);
            ImageUtils.writeFile(ImageUtils.getScaledInstance(image, VIVO_THUMBNAIL_WIDTH, targetHeight, true), new File(thumbnailDir, userInfo.getUsername() + ".thumbnail.jpg"), "jpeg");

            // Write out XML
            Writer photoXml = new StringWriter();
            try {
                photoXml.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                photoXml.write("<rdf:RDF xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:score=\"http://vivoweb.org/ontology/score#\" xmlns:vitro=\"http://vitro.mannlib.cornell.edu/ns/vitro/0.7#\" xmlns:bibo=\"http://purl.org/ontology/bibo/\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:ufVivo=\"http://vivo.ufl.edu/ontology/vivo-ufl/\" xmlns:owlPlus=\"http://www.w3.org/2006/12/owl2-xml#\" xmlns:svo=\"http://www.symplectic.co.uk/vivo/\" xmlns:skos=\"http://www.w3.org/2008/05/skos#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema#\" xmlns:api=\"http://www.symplectic.co.uk/publications/api\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:vitro-public=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#\" xmlns:vocab=\"http://purl.org/vocab/vann/\" xmlns:core=\"http://vivoweb.org/ontology/core#\" xmlns:swvocab=\"http://www.w3.org/2003/06/sw-vocab-status/ns#\">");

                photoXml.write("<rdf:Description rdf:about=\"" + baseUrl + userInfo.getUsername() + "\">");
                photoXml.write("    <vitro-public:mainImage rdf:resource=\"" + baseUrl + userInfo.getUsername() + "-image\"/>");
                photoXml.write("</rdf:Description>");

                photoXml.write("<rdf:Description rdf:about=\"" + baseUrl + userInfo.getUsername() + "-image\">");
                photoXml.write("    <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>");
                photoXml.write("    <rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#File\"/>");
                photoXml.write("    <vitro-public:downloadLocation rdf:resource=\"" + baseUrl + userInfo.getUsername() + "-imageDownload\"/>");
                photoXml.write("    <vitro-public:thumbnailImage rdf:resource=\"" + baseUrl + userInfo.getUsername() + "-imageThumbnail\"/>");
                photoXml.write("    <vitro-public:filename>" + userInfo.getUsername() + ".jpg</vitro-public:filename>");
                photoXml.write("    <vitro-public:mimeType>image/jpeg</vitro-public:mimeType>");
                photoXml.write("</rdf:Description>");

                photoXml.write("<rdf:Description rdf:about=\"" + baseUrl + userInfo.getUsername() + "-imageDownload\">");
                photoXml.write("    <rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#FileByteStream\"/>");
                photoXml.write("    <vitro-public:directDownloadUrl>" + imageUrlBase + "fullImages/" + userInfo.getUsername() + ".jpg</vitro-public:directDownloadUrl>");
                photoXml.write("</rdf:Description>");

                photoXml.write("<rdf:Description rdf:about=\"" + baseUrl + userInfo.getUsername() + "-imageThumbnail\">");
                photoXml.write("    <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>");
                photoXml.write("    <rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#File\"/>");
                photoXml.write("    <vitro-public:downloadLocation rdf:resource=\"" + baseUrl + userInfo.getUsername() + "-imageThumbnailDownload\"/>");
                photoXml.write("    <vitro-public:filename>" + userInfo.getUsername() + ".thumbnail.jpg</vitro-public:filename>");
                photoXml.write("    <vitro-public:mimeType>image/jpeg</vitro-public:mimeType>");
                photoXml.write("</rdf:Description>");

                photoXml.write("<rdf:Description rdf:about=\"" + baseUrl + userInfo.getUsername() + "-imageThumbnailDownload\">");
                photoXml.write("    <rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#FileByteStream\"/>");
                photoXml.write("    <vitro-public:directDownloadUrl>" + imageUrlBase + "thumbnails/" + userInfo.getUsername() + ".thumbnail.jpg</vitro-public:directDownloadUrl>");
                photoXml.write("</rdf:Description>");

                photoXml.write("</rdf:RDF>");

                rdfStore.writeObjectExtra(userInfo, "photo", photoXml.toString());
            } catch (IOException e) {

            } finally {
            }
        }
    }
}
