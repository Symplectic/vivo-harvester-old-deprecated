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
                xmlns:core="http://vivoweb.org/ontology/core#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:score="http://vivoweb.org/ontology/score#"
                xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/"
                xmlns:api="http://www.symplectic.co.uk/publications/api"
                xmlns:svo="http://www.symplectic.co.uk/vivo/"
                xmlns:symp="http://vivo.symplectic.org/ontology/elements/"
                exclude-result-prefixes="rdf rdfs bibo core foaf score ufVivo api svo symp xs"
        >

    <xsl:import href="elements-to-vivo-config.xsl" />
    <xsl:import href="elements-to-vivo-utils.xsl" />

    <xsl:output method="xml" indent="yes" />

    <xsl:template match="/">
        <xsl:call-template name="_render_rdf_document">
            <xsl:with-param name="rdf-nodes">
                <xsl:apply-templates select="//api:object" />
            </xsl:with-param>
        </xsl:call-template>
        <!-- output>
            <xsl:value-of select="symp:reverse('DOG BITES MAN')"/>
        </output -->
    </xsl:template>

    <xsl:template match="api:object[@category='publication']">
        <xsl:call-template name="_render_rdf_object">
            <xsl:with-param name="rdf-nodes"/>
            <xsl:with-param name="objectURI" select="symp:objectURI(.)" />
        </xsl:call-template>

        <!-- TEST><xsl:value-of select="symp:datasourceField(.,'publication-date')" /></TEST>
        <TEST><xsl:value-of select="symp:datasourceField(.,'publication-date','arxiv,pubmed')" /></TEST>
        <TEST><xsl:value-of select="symp:datasourceField(.,'publication-date','arxiv,manual')" /></TEST -->

        <xsl:call-template name="_render_rdf_datetime">
            <xsl:with-param name="objectURI" select="concat(symp:objectURI(.),'-publicationDate')" />
            <xsl:with-param name="formattedDate"><xsl:apply-templates select="symp:datasourceField(.,'publication-date')" mode="dateTimeValue" /></xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="api:date" mode="dateTimeValue">
        <xsl:variable name="datePrecision">
            <xsl:choose>
                <xsl:when
                        test="string(api:day) and string(api:month) and string(api:year)">yearMonthDayPrecision</xsl:when>
                <xsl:when test="string(api:month) and string(api:year)">yearMonthPrecision</xsl:when>
                <xsl:when test="string(api:year)">yearPrecision</xsl:when>
                <xsl:otherwise>none</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="month">
            <xsl:choose>
                <xsl:when
                        test="string-length(api:month)=1">0<xsl:value-of select="api:month" /></xsl:when>
                <xsl:otherwise><xsl:value-of select="api:month" /></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="day">
            <xsl:choose>
                <xsl:when
                        test="string-length(api:day)=1">0<xsl:value-of select="api:day" /></xsl:when>
                <xsl:otherwise><xsl:value-of select="api:day" /></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="aboutURI">
            <xsl:choose>
                <xsl:when test="$datePrecision='yearMonthDayPrecision'" >pub/daymonthyear<xsl:value-of select="api:year" /><xsl:value-of select="$month" /><xsl:value-of select="$day" /></xsl:when>
                <xsl:when test="$datePrecision='yearMonthPrecision'" >pub/monthyear<xsl:value-of select="api:year" /><xsl:value-of select="$month" /></xsl:when>
                <xsl:when test="$datePrecision='yearPrecision'" >pub/year<xsl:value-of select="api:year" /></xsl:when>
            </xsl:choose>
        </xsl:variable>

        <xsl:if test="$datePrecision!='none'">
            <core:dateTimePrecision
                    rdf:resource="http://vivoweb.org/ontology/core#{$datePrecision}" />
            <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime">
                <xsl:choose>
                    <xsl:when test="$datePrecision='yearMonthDayPrecision'" ><xsl:value-of select="api:year" />-<xsl:value-of select="$month" />-<xsl:value-of select="$day" />T00:00:00Z</xsl:when>
                    <xsl:when test="$datePrecision='yearMonthPrecision'" ><xsl:value-of select="api:year" />-<xsl:value-of select="$month" />-01T00:00:00Z</xsl:when>
                    <xsl:when test="$datePrecision='yearPrecision'" ><xsl:value-of select="api:year" />-01-01T00:00:00Z</xsl:when>
                </xsl:choose>
            </core:dateTime>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>