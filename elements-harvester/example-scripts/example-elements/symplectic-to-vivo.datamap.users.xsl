<?xml version="1.0" encoding="UTF-8"?>
<!--
 | Copyright (c) 2012 Symplectic Limited. All rights reserved.
 | This Source Code Form is subject to the terms of the Mozilla Public
 | License, v. 2.0. If a copy of the MPL was not distributed with this
 | file, You can obtain one at http://mozilla.org/MPL/2.0/.
 -->
<xsl:stylesheet version="2.0"
                xmlns:svo="http://www.symplectic.co.uk/vivo/" xmlns:api="http://www.symplectic.co.uk/publications/api"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:core="http://vivoweb.org/ontology/core#" xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:score='http://vivoweb.org/ontology/score#' xmlns:bibo='http://purl.org/ontology/bibo/'
                xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'>

    <xsl:import href="symplectic-to-vivo.datamap.config.xsl" />

    <xsl:template match="api:object[@category='user']">
        <rdf:RDF xmlns:owlPlus='http://www.w3.org/2006/12/owl2-xml#'
                 xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' xmlns:skos='http://www.w3.org/2008/05/skos#'
                 xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' xmlns:owl='http://www.w3.org/2002/07/owl#'
                 xmlns:vocab='http://purl.org/vocab/vann/' xmlns:swvocab='http://www.w3.org/2003/06/sw-vocab-status/ns#'
                 xmlns:dc='http://purl.org/dc/elements/1.1/' xmlns:vitro='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#'
                 xmlns:core='http://vivoweb.org/ontology/core#' xmlns:foaf='http://xmlns.com/foaf/0.1/'
                 xmlns:score='http://vivoweb.org/ontology/score#' xmlns:xs='http://www.w3.org/2001/XMLSchema#'
                 xmlns:svo='http://www.symplectic.co.uk/vivo/' xmlns:api='http://www.symplectic.co.uk/publications/api'
                 xmlns:vitro-public="http://vitro.mannlib.cornell.edu/ns/vitro/public#"
                 xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'
                 xmlns:bibo='http://purl.org/ontology/bibo/'>

            <!--  Main user object -->
            <rdf:Description rdf:about="{$baseURI}{@username}">
                <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
                <rdfs:label>
                    <xsl:value-of select="api:last-name" />, <xsl:value-of select="api:first-name" />
                </rdfs:label>
                <core:preferredTitle>
                    <xsl:value-of select="api:title" />
                </core:preferredTitle>
                <core:primaryEmail>
                    <xsl:value-of select="api:email-address" />
                </core:primaryEmail>
                <foaf:lastName>
                    <xsl:value-of select="api:last-name" />
                </foaf:lastName>
                <foaf:firstName>
                    <xsl:value-of select="api:first-name" />
                </foaf:firstName>
                <!-- Not used -->
                <!-- score:initials>
                             <xsl:value-of select="api:initials" />
                         </score:initials -->
                <rdf:type rdf:resource="http://vivoweb.org/harvester/excludeEntity" />
                <rdf:type
                        rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing" />
                <rdf:type
                        rdf:resource="http://www.symplectic.co.uk/vivo/User" />
                <vitro-public:mainImage rdf:resource="{$baseURI}{@username}-image"/>

                <xsl:apply-templates select="api:records/api:record[1]" mode="objectReferences" />
                <xsl:apply-templates select="api:organisation-defined-data" mode="objectReferences" />

                <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                <xsl:apply-templates select="api:records/api:record[1]" />
                <xsl:apply-templates select="api:organisation-defined-data" />

            </rdf:Description>
            <!--  users Icon.
                    The users Icon file is expected to already be present in
                    /users/{@username}.jpg with the thumbnail at /users/thumbnails/user{@username}.thumbnail.jpg
                    on the server. If hosting under Apache HTTPD, re-write rules should be put in place
                    to ensure that a replacement image is created when those files are not found.
                     -->
            <rdf:Description rdf:about="{$baseURI}{@username}-image">
                <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                <rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/public#File"/>
                <vitro-public:downloadLocation rdf:resource="{$baseURI}{@username}-imageDownload"/>
                <vitro-public:thumbnailImage rdf:resource="{$baseURI}{@username}-imageThumbnail"/>
                <vitro-public:filename><xsl:value-of select="@username" />.jpg</vitro-public:filename>
                <vitro-public:mimeType>image/jpg</vitro-public:mimeType>
            </rdf:Description>
            <rdf:Description rdf:about="{$baseURI}{@username}-imageDownload">
                <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                <rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/public#FileByteStream"/>
                <vitro-public:directDownloadUrl>    </vitro-public:directDownloadUrl>
            </rdf:Description>
            <rdf:Description rdf:about="{$baseURI}{@username}-imageThumbnail">
                <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                <rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/public#File"/>
                <vitro-public:downloadLocation rdf:resource="{$baseURI}{@username}-imageThumbnailDownload"/>
                <vitro-public:filename><xsl:value-of select="@username" />.thumbnail.jpg</vitro-public:filename>
                <vitro-public:mimeType>image/jpeg</vitro-public:mimeType>
            </rdf:Description>
            <rdf:Description rdf:about="{$baseURI}{@username}-imageThumbnailDownload">
                <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                <rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/public#FileByteStream"/>
                <vitro-public:directDownloadUrl>/users/thumbnails/<xsl:value-of select="@username" />.thumbnail.jpg</vitro-public:directDownloadUrl>
            </rdf:Description>
            <xsl:apply-templates select="api:records/api:record[1]" mode="objectEntries" />
            <xsl:apply-templates select="api:organisation-defined-data" mode="objectEntries" />
        </rdf:RDF>
    </xsl:template>
</xsl:stylesheet>