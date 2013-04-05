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
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:score="http://vivoweb.org/ontology/score#"
                xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/"
                xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
                xmlns:api="http://www.symplectic.co.uk/publications/api"
                xmlns:symp="http://www.symplectic.co.uk/ontology/elements/"
                xmlns:svfn="http://www.symplectic.co.uk/vivo/namespaces/functions"
                xmlns:config="http://www.symplectic.co.uk/vivo/namespaces/config"
                exclude-result-prefixes="rdf rdfs bibo vivo config foaf score ufVivo vitro api svfn symp xs"
        >

    <!-- The base URI you are using for VIVO identifiers -->
    <xsl:param name="baseURI">http://vivo.symplectic.co.uk/individual/</xsl:param>

    <!-- Harvested by statement for the URI (set to blank if not required) -->
    <xsl:param name="harvestedBy">Symplectic-Harvester</xsl:param>

    <!-- DO NOT TOUCH: Read the datasource and journal precedence configuration into variables for processing -->
    <xsl:variable name="datasource-precedence-select-by" select="document('')//config:datasource-precedences/@select-by" />
    <xsl:variable name="datasource-precedence" select="document('')//config:datasource-precedences/config:datasource-precedence" />
    <xsl:variable name="journal-precedence" select="document('')//config:journal-precedences/config:journal-precedence" />

    <!--
        Configure precedence for datasources
        ====================================

        Use select-by="field" attribute to choose the field from the highest precedence datasource in which it occurs.

        Otherwise, it will select the highest precedence datasource, regardless of whether field exizts.

        If a datasource is not listed, it will not be used (except when using the "fallback to first datasource" function).
    -->
    <config:datasource-precedences select-by="field">
        <config:datasource-precedence>pubmed</config:datasource-precedence>
        <config:datasource-precedence>manual</config:datasource-precedence>
        <config:datasource-precedence>arxiv</config:datasource-precedence>
    </config:datasource-precedences>

    <!--
        Configure precedence for retrieving journal names
        =================================================

        If type="authority", then attempt to use the named authority source included in the publication

        If type="datasource", then attempt to use the named data source, taking the value from "field" (defaults to "journal")
    -->
    <config:journal-precedences>
        <config:journal-precedence type="authority">sherpa-romeo</config:journal-precedence>
        <config:journal-precedence type="authority">science-metrix</config:journal-precedence>
        <config:journal-precedence type="datasource" field="journal">pubmed</config:journal-precedence>
        <config:journal-precedence type="datasource" field="journal">manual</config:journal-precedence>
        <config:journal-precedence type="datasource" field="journal">arxiv</config:journal-precedence>
    </config:journal-precedences>
</xsl:stylesheet>
