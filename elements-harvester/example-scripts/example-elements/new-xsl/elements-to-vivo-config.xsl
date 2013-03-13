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
                xmlns:datasource="http://vivo.symplectic.co.uk/namespaces/datasource"
                exclude-result-prefixes="rdf rdfs bibo core datasource foaf score ufVivo api svo symp xs"
        >

    <xsl:param name="baseURI">http://vivo.symplectic.co.uk/individual/</xsl:param>

    <xsl:variable name="datasource-precedence-select-by" select="document('')//datasource:precedences/@select-by" />
    <xsl:variable name="datasource-precedence" select="document('')//datasource:precedences/datasource:precedence" />

    <!-- Use select-by="field" attribute to choose the field from the highest precedence datasource in which it occurs -->
    <!-- Otherwise, it will select the highest precedence datasource, regardless of whether field exizts -->
    <datasource:precedences select-by="field">
        <datasource:precedence>pubmed</datasource:precedence>
        <datasource:precedence>manual</datasource:precedence>
        <datasource:precedence>arxiv</datasource:precedence>
    </datasource:precedences>
</xsl:stylesheet>

<!--
<data:mappings>
    <data:mapping vivo="vitro:mostSpecificType" elements="" />
    <data:mapping vivo="core:hasPublicationVenue" elements="" /> - links to journal -
    <data:mapping vivo="core:dateTimeValue" elements="" /> - links to date -
    <data:mapping vivo="ufVivo:harvestedBy" elements="" />
    <data:mapping vivo="bibo:abstract" elements="" />
    <data:mapping vivo="symp:authors" elements="" />
    <data:mapping vivo="bibo:issue" elements="" />
    <data:mapping vivo="bibo:pageStart" elements="" />
    <data:mapping vivo="bibo:pageEnd" elements="" />
    <data:mapping vivo="rdfs:label" elements="" />
    <data:mapping vivo="core:Title" elements="" />
    <data:mapping vivo="bibo:volume" elements="" />
</data:mappings>
<data:mappings> - journal -
    <data:mapping vivo="rdfs:label" elements="" />
</data:mappings>
<data:mappings> - date -
    <data:mapping vivo="core:dateTimePrecision" elements="" />
    <data:mapping vivo="core:dateTime" elements="" />
</data:mappings>
-->
