<?xml version="1.0" encoding="UTF-8"?>
<!--
 | Copyright (c) 2012 Symplectic Limited. All rights reserved.
 | This Source Code Form is subject to the terms of the Mozilla Public
 | License, v. 2.0. If a copy of the MPL was not distributed with this
 | file, You can obtain one at http://mozilla.org/MPL/2.0/.
 -->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:bibo="http://purl.org/ontology/bibo/"
                xmlns:vivo="http://vivoweb.org/ontology/core#"
                xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:score="http://vivoweb.org/ontology/score#"
                xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/"
                xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
                xmlns:api="http://www.symplectic.co.uk/publications/api"
                xmlns:symp="http://www.symplectic.co.uk/vivo/"
                xmlns:svfn="http://www.symplectic.co.uk/vivo/namespaces/functions"
                xmlns:config="http://www.symplectic.co.uk/vivo/namespaces/config"
                xmlns:obo="http://purl.obolibrary.org/obo/"
                exclude-result-prefixes="xsl xs rdf rdfs bibo vivo vcard foaf score ufVivo vitro api symp svfn config obo"
        >

    <xsl:import href="elements-to-vivo-utils.xsl" />

    <!-- Match Elements objects of category 'grant' -->
    <xsl:template match="api:object[@category='grant']">
        <xsl:variable name="grantURI"><xsl:value-of select="svfn:objectURI(.)" /></xsl:variable>

        <xsl:variable name="startDateURI" select="concat(svfn:objectURI(.),'-startDate')" />
        <xsl:variable name="endDateURI" select="concat(svfn:objectURI(.),'-endDate')" />
        <xsl:variable name="startDateObject" select="svfn:renderDateObject(.,$startDateURI,svfn:getRecordField(.,'start-date'))" />
        <xsl:variable name="endDateObject" select="svfn:renderDateObject(.,$endDateURI,svfn:getRecordField(.,'end-date'))" />

        <xsl:variable name="intervalURI" select="concat(svfn:objectURI(.),'-interval')" />

        <xsl:variable name="funderName" select="svfn:getRecordField(.,'funder-name')" />
        <xsl:variable name="funderURI"><xsl:if test="$funderName/api:text"><xsl:value-of select="svfn:makeURI('funder-',$funderName/api:text)" /></xsl:if></xsl:variable>

        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="$grantURI" />
            <xsl:with-param name="rdfNodes">
                <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Grant"/>
                <xsl:copy-of select="svfn:renderPropertyFromFieldOrFirst(.,'rdfs:label','title')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'bibo:abstract','description')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'vivo:sponsorAwardId','funder-reference')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'vivo:localAwardId','institution-reference')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'vivo:totalAwardAmount','amount')" />
                <xsl:if test="$startDateObject/* or $endDateObject/*"><vivo:dateTimeInterval rdf:resource="{$intervalURI}" /></xsl:if>
                <xsl:if test="$funderName/*">
                    <vivo:assignedBy rdf:resource="{$funderURI}" />
                </xsl:if>
            </xsl:with-param>
        </xsl:call-template>

        <xsl:if test="$startDateObject/* or $endDateObject/*">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$intervalURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeInterval"/>
                    <xsl:if test="$startDateObject/*"><vivo:start rdf:resource="{$startDateURI}" /></xsl:if>
                    <xsl:if test="$endDateObject/*"><vivo:end rdf:resource="{$endDateURI}" /></xsl:if>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
        <xsl:copy-of select="$startDateObject" />
        <xsl:copy-of select="$endDateObject" />

        <xsl:if test="$funderName/*">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$funderURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#FundingOrganization"/>
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <xsl:apply-templates select="$funderName" mode="renderForProperty">
                        <xsl:with-param name="propertyName">rdfs:label</xsl:with-param>
                        <xsl:with-param name="fieldName">funder-name</xsl:with-param>
                    </xsl:apply-templates>
                    <vivo:assigns rdf:resource="{$grantURI}"/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
