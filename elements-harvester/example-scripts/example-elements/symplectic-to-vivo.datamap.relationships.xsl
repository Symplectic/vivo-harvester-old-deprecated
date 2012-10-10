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

    <xsl:variable name="relationship-authored-by">8</xsl:variable>
    <xsl:variable name="relationship-edited-by">9</xsl:variable>
    <xsl:variable name="relationship-funded-by">17</xsl:variable>
    <xsl:variable name="relationship-primary-investigator">43</xsl:variable>
    <xsl:variable name="relationship-secondary-investigator">44</xsl:variable>
    <xsl:variable name="relationship-professional-activity">23</xsl:variable>

    <!--  Relationships -->
    <xsl:template match="api:relationship">
        <xsl:choose>
            <xsl:when test="@type-id=$relationship-authored-by" >
                <!--  author relationship -->
                <rdf:RDF
                        xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
                        xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
                        xmlns:core='http://vivoweb.org/ontology/core#'
                        xmlns:api='http://www.symplectic.co.uk/publications/api'
                        xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'
                        >
                    <xsl:variable name="publicationID" select="api:related[@direction='from']/api:object/@id" />
                    <xsl:variable name="userID" select="api:related[@direction='to']/api:object/@username" />

                    <!--  add the authorship to the person -->
                    <rdf:Description rdf:about="{$baseURI}{$userID}">
                        <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
                        <core:authorInAuthorship rdf:resource="{$baseURI}authorship{@id}"/>
                    </rdf:Description>

                    <!--  add the author to the publication -->
                    <rdf:Description rdf:about="{$baseURI}publication{$publicationID}">
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#InformationResource"/>
                        <core:informationResourceInAuthorship rdf:resource="{$baseURI}authorship{@id}"/>
                    </rdf:Description>

                    <!--  create the link -->
                    <rdf:Description rdf:about="{$baseURI}authorship{@id}">
                        <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Relationship"/>
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship"/>
                        <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                        <core:linkedAuthor rdf:resource="{$baseURI}{$userID}"/>
                        <core:linkedInformationResource rdf:resource="{$baseURI}publication{$publicationID}"/>
                    </rdf:Description>
                </rdf:RDF>
            </xsl:when>
            <xsl:when test="@type-id=$relationship-edited-by" >
                <!--  editor relationship -->
                <rdf:RDF
                        xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
                        xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
                        xmlns:core='http://vivoweb.org/ontology/core#'
                        xmlns:api='http://www.symplectic.co.uk/publications/api'
                        xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'
                        >
                    <xsl:variable name="publicationID" select="api:related[@direction='from']/api:object/@id" />
                    <xsl:variable name="userID" select="api:related[@direction='to']/api:object/@username" />


                    <!--  add the author to the publication -->
                    <rdf:Description rdf:about="{$baseURI}publication{$publicationID}">
                        <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Document"/>
                        <core:editor rdf:resource="{$baseURI}{$userID}"/>
                    </rdf:Description>
                </rdf:RDF>
            </xsl:when>
            <xsl:when test="@type-id=$relationship-funded-by" >
                <!--  (Grant) Funder of (User) eg relationship30585 -->
            </xsl:when>
            <xsl:when test="@type-id=$relationship-primary-investigator" >
                <!-- (User) Primary investigator (Grant) relationship30586 -->
            </xsl:when>
            <xsl:when test="@type-id=$relationship-secondary-investigator" >
                <!-- (User) Secondary investigator (Grant) relationship30587 -->
            </xsl:when>
            <xsl:when test="@type-id=$relationship-professional-activity" >
                <xsl:apply-templates select="api:related[@direction='from']/api:object" mode="professionalActivityRelationship">
                    <xsl:with-param name="username" select="api:related[@direction='to']/api:object/@username" />
                </xsl:apply-templates>
                <!--
                    This applies to single resources. For mutliple occurring resources, process the document
                    $activity-research-center

                    Also, Phd students go to -relationship

                <xsl:variable name="activityURI"><xsl:apply-templates select="api:related[@direction='from']/api:object" mode="activityURI"/></xsl:variable>
                <xsl:variable name="userID" select="api:related[@direction='to']/api:object/@username" />

                <rdf:RDF
                        xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
                        xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
                        xmlns:core='http://vivoweb.org/ontology/core#'
                        xmlns:api='http://www.symplectic.co.uk/publications/api'
                        xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'
                        >
                    <rdf:Description rdf:about="{$baseURI}{$userID}">
                        <core:currentMemberOf rdf:resource="{$activityURI}"/>
                    </rdf:Description>
                    <rdf:Description rdf:about="{$activityURI}">
                        <core:hasCurrentMember rdf:resource="{$baseURI}{$userID}"/>
                    </rdf:Description>
                </rdf:RDF>

                -->

                <!-- xsl:apply-templates select="document(concat('data/raw-records/',api:related[@direction='from']/api:object/@category,'/',api:related[@direction='from']/api:object/@id))"
                                     mode="professionalActivity">
                    <xsl:with-param name="username" select="api:related[@direction='to']/api:object/@username" />
                </xsl:apply-templates -->
            </xsl:when>
            <xsl:otherwise>
                <rdf:RDF
                        xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
                        xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
                        xmlns:core='http://vivoweb.org/ontology/core#'
                        xmlns:api='http://www.symplectic.co.uk/publications/api'
                        xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'
                        >
                    <!--  create the link -->
                    <rdf:Description rdf:about="{$baseURI}-unknown-relationship-{@id}">
                        <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                        <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                        <svo:relationship-type><xsl:value-of select="@type-id" /></svo:relationship-type>
                    </rdf:Description>

                </rdf:RDF>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>