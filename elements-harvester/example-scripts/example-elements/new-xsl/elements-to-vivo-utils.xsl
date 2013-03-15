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
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
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
                exclude-result-prefixes="rdf rdfs bibo core foaf score ufVivo api svo symp xs fn"
                >

    <!-- ======================================
         Function Library
         ======================================- -->

    <xsl:function name="symp:datasourceField">
        <xsl:param name="object" />
        <xsl:param name="fieldName" as="xs:string" />

        <xsl:copy-of select="symp:datasourceField($object, $fieldName, $datasource-precedence, $datasource-precedence-select-by, 1)" />
    </xsl:function>

    <xsl:function name="symp:datasourceField">
        <xsl:param name="object" />
        <xsl:param name="fieldName" as="xs:string" />
        <xsl:param name="datasources" as="xs:string" />

        <xsl:copy-of select="symp:datasourceField($object, $fieldName, fn:tokenize($datasources,','), $datasource-precedence-select-by, 1)" />
    </xsl:function>

    <xsl:function name="symp:datasourceField">
        <xsl:param name="object" />
        <xsl:param name="fieldName" as="xs:string" />
        <xsl:param name="datasources" as="xs:string" />
        <xsl:param name="select-by" as="xs:string" />

        <xsl:copy-of select="symp:datasourceField($object, $fieldName, fn:tokenize($datasources,','), $select-by, 1)" />
    </xsl:function>

    <xsl:function name="symp:datasourceField">
        <xsl:param name="object" />
        <xsl:param name="fieldName" as="xs:string" />
        <xsl:param name="datasources" />
        <xsl:param name="select-by" />
        <xsl:param name="position" as="xs:integer" />

        <!-- xsl:variable name="current-record" select="api:records/api:record[@source-name = '']" / -->
        <xsl:if test="$datasources[$position]">
            <xsl:choose>
                <xsl:when test="$select-by='field'">
                    <xsl:choose>
                        <xsl:when test="$object/api:records/api:record[@source-name=$datasources[$position]]/api:native/api:field[@name=$fieldName]">
                            <xsl:copy-of select="$object/api:records/api:record[@source-name=$datasources[$position]]/api:native/api:field[@name=$fieldName]" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:copy-of select="symp:datasourceField($object,$fieldName,$datasources,$select-by,$position+1)" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:choose>
                        <xsl:when test="$object/api:records/api:record[@source-name=$datasources[$position]]/api:native">
                            <xsl:copy-of select="$object/api:records/api:record[@source-name=$datasources[$position]]/api:native/api:field[@name=$fieldName]" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:copy-of select="symp:datasourceField($object,$fieldName,$datasources,$select-by,$position+1)" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:function>

    <xsl:function name="symp:objectURI" as="xs:string">
        <xsl:param name="object" />

        <xsl:value-of select="concat($baseURI,$object/@category,$object/@id)" />
    </xsl:function>

    <!-- Example function -->
    <xsl:function name="symp:reverse" as="xs:string">
        <xsl:param name="sentence" as="xs:string"/>
        <xsl:sequence select="if (contains($sentence, ' '))
                              then concat(symp:reverse(substring-after($sentence, ' ')), ' ', substring-before($sentence, ' '))
                              else $sentence" />
    </xsl:function>

    <!-- ======================================
         Template Library
         ======================================- -->

    <xsl:template name="_render_rdf_datetime">
        <xsl:param name="objectURI" />
        <xsl:param name="formattedDate" />

        <xsl:call-template name="_render_rdf_object">
            <xsl:with-param name="objectURI" select="$objectURI" />
            <xsl:with-param name="rdf-nodes">
                <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/publication-date"/>
                <core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearMonthPrecision" />
                <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="$formattedDate" /></core:dateTime>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!-- _render_rdf_document -->
    <xsl:template name="_render_rdf_document">
        <xsl:param name="rdf-nodes" />

        <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                 xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                 xmlns:core="http://vivoweb.org/ontology/core#"
                 xmlns:dc="http://purl.org/dc/elements/1.1/"
                 xmlns:foaf="http://xmlns.com/foaf/0.1/"
                 xmlns:owl="http://www.w3.org/2002/07/owl#"
                 xmlns:owlPlus="http://www.w3.org/2006/12/owl2-xml#"
                 xmlns:score="http://vivoweb.org/ontology/score#"
                 xmlns:skos="http://www.w3.org/2008/05/skos#"
                 xmlns:swvocab="http://www.w3.org/2003/06/sw-vocab-status/ns#"
                 xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/"
                 xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
                 xmlns:vocab="http://purl.org/vocab/vann/"
                 xmlns:xs="http://www.w3.org/2001/XMLSchema#"
                 xmlns:svo="http://www.symplectic.co.uk/vivo/"
                 xmlns:symp="http://www.symplectic.co.uk/ontology/elements/"
                >
            <xsl:copy-of select="$rdf-nodes" />
        </rdf:RDF>
    </xsl:template>

    <!-- _render_rdf_object -->
    <xsl:template name="_render_rdf_object">
        <xsl:param name="rdf-nodes" />
        <xsl:param name="objectURI" />

        <rdf:Description rdf:about="{$objectURI}">
            <xsl:copy-of select="$rdf-nodes" />
        </rdf:Description>
    </xsl:template>
</xsl:stylesheet>