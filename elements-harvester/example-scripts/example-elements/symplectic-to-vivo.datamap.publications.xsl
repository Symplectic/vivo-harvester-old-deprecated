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

    <xsl:variable name="publication-type-book">2</xsl:variable>
    <xsl:variable name="publication-type-chapter">3</xsl:variable>
    <xsl:variable name="publication-type-conference-paper">4</xsl:variable>
    <xsl:variable name="publication-type-academic-article">5</xsl:variable>
    <xsl:variable name="publication-type-patent">6</xsl:variable>
    <xsl:variable name="publication-type-report">7</xsl:variable>
    <xsl:variable name="publication-type-software">8</xsl:variable>
    <xsl:variable name="publication-type-performance">9</xsl:variable>
    <xsl:variable name="publication-type-composition">10</xsl:variable>
    <xsl:variable name="publication-type-exhibition">13</xsl:variable>
    <xsl:variable name="publication-type-internet-publication">15</xsl:variable>
    <xsl:variable name="publication-type-scholarly-edition">16</xsl:variable>
    <xsl:variable name="publication-type-poster">17</xsl:variable>
    <xsl:variable name="publication-type-thesis">18</xsl:variable>
    <xsl:variable name="publication-type-film">32</xsl:variable>

    <xsl:template match="api:object[@category='publication']">
        <rdf:RDF xmlns:owlPlus='http://www.w3.org/2006/12/owl2-xml#'
                 xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' xmlns:skos='http://www.w3.org/2008/05/skos#'
                 xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' xmlns:owl='http://www.w3.org/2002/07/owl#'
                 xmlns:vocab='http://purl.org/vocab/vann/' xmlns:swvocab='http://www.w3.org/2003/06/sw-vocab-status/ns#'
                 xmlns:dc='http://purl.org/dc/elements/1.1/' xmlns:vitro='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#'
                 xmlns:core='http://vivoweb.org/ontology/core#' xmlns:foaf='http://xmlns.com/foaf/0.1/'
                 xmlns:score='http://vivoweb.org/ontology/score#' xmlns:xs='http://www.w3.org/2001/XMLSchema#'
                 xmlns:svo='http://www.symplectic.co.uk/vivo/' xmlns:api='http://www.symplectic.co.uk/publications/api'
                 xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/' xmlns:symp='http://www.symplectic.co.uk/ontology/elements/'>
            <!--  Main publication object -->
            <rdf:Description rdf:about="{$baseURI}publication{@id}">
                <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                <rdf:type rdf:resource="http://vivoweb.org/ontology/core#InformationResource"/>
                <xsl:choose>
                    <xsl:when test="@type-id=$publication-type-book"> <!-- Book  -->
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Document"/>
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Book"/>
                    </xsl:when>
                    <xsl:when test="@type-id=$publication-type-chapter"> <!-- Chapter  -->
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Document"/>
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Book"/>
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Chapter"/>
                    </xsl:when>
                    <xsl:when test="@type-id=$publication-type-conference-paper"> <!-- Confernce Paper  -->
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Document"/>
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Article"/>
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper"/>
                    </xsl:when>
                    <xsl:when test="@type-id=$publication-type-academic-article"> <!--  Academic Article -->
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Document"/>
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Article"/>
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle"/>
                    </xsl:when>
                    <xsl:when test="@type-id=$publication-type-patent"> <!-- Patent  -->
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Document"/>
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Patent"/>
                    </xsl:when>
                    <xsl:when test="@type-id=$publication-type-report"> <!-- Report  -->
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Document"/>
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Report"/>
                    </xsl:when>
                    <xsl:when test="@type-id=$publication-type-software"> <!-- Software  -->
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Software"/>
                    </xsl:when>
                    <xsl:when test="@type-id=$publication-type-performance"> <!-- Event/Performance  -->
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Event"/>
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Performance"/>
                    </xsl:when>
                    <xsl:when test="@type-id=$publication-type-composition"> <!-- Composition  -->
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Document"/>
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Score"/>
                    </xsl:when>
                    <xsl:when test="@type-id=$publication-type-exhibition"> <!-- Exhibition  -->
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Event"/>
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Exhibit"/>
                    </xsl:when>
                    <xsl:when test="@type-id=$publication-type-internet-publication"> <!-- Internet Publication  -->
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Document"/>
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Webpage"/>
                    </xsl:when>
                    <xsl:when test="@type-id=$publication-type-scholarly-edition"> <!-- Scolarly Edition  -->
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Document"/>
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Article"/>
                    </xsl:when>
                    <xsl:when test="@type-id=$publication-type-poster"> <!-- Poster  -->
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Document"/>
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#ConferencePoster"/>
                    </xsl:when>
                    <xsl:when test="@type-id=$publication-type-thesis"> <!-- Thesis/Disertation  -->
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Document"/>
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Thesis"/>
                    </xsl:when>
                    <xsl:when test="@type-id=$publication-type-film"> <!-- Film  -->
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Document"/>
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/AudioVisualDocument"/>
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Film"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Document"/>
                        <rdf:type rdf:resource="http://purl.org/ontology/bibo/Article"/>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:apply-templates select="api:records/api:record[1]" mode="objectReferences" />
                <xsl:apply-templates select="api:repository-items" mode="objectReferences" />

                <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                <xsl:apply-templates select="api:records/api:record[1]" />
            </rdf:Description>

            <!--  publication date -->
            <xsl:apply-templates select="api:records/api:record[1]" mode="objectEntries" />
            <xsl:apply-templates select="api:repository-items" mode="objectEntries" />

        </rdf:RDF>
    </xsl:template>
</xsl:stylesheet>