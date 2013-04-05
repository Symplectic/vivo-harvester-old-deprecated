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
                xmlns:symp="http://www.symplectic.co.uk/vivo/"
                xmlns:svfn="http://www.symplectic.co.uk/vivo/namespaces/functions"
                xmlns:config="http://www.symplectic.co.uk/vivo/namespaces/config"
                exclude-result-prefixes="rdf rdfs bibo vivo foaf score ufVivo vitro api symp svfn config xs"
        >

    <xsl:import href="elements-to-vivo-utils.xsl" />

    <xsl:template match="api:object[@category='publication']">
        <xsl:variable name="publicationDateURI" select="concat(svfn:objectURI(.),'-publicationDate')" />
        <xsl:variable name="publicationDateObject" select="svfn:renderDateObject(.,$publicationDateURI,svfn:datasourceField(.,'publication-date'))" />

        <xsl:variable name="publicationVenueTitle" select="svfn:selectJournalTitle(.)" />
        <xsl:variable name="publicationVenueURI" select="concat($baseURI, 'journal-', svfn:stringToURI($publicationVenueTitle))" />
        <xsl:variable name="publicationVenueObject" select="svfn:renderPublicationVenueObject(.,$publicationVenueURI,$publicationVenueTitle)" />

        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="svfn:objectURI(.)" />
            <xsl:with-param name="rdfNodes">
                <xsl:copy-of select="svfn:getTypesForPublication(@type)" />
                <xsl:copy-of select="svfn:renderPropertyFromFieldOrFirst(.,'rdfs:label','title')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'bibo:abstract','abstract')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'bibo:doi','doi')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'bibo:issue','issue')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'bibo:pageStart','pagination')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'bibo:pageEnd','pagination')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'bibo:volume','volume')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'vivo:freetextKeyword','keywords')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'symp:authors','authors')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'symp:language','language')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'symp:location','location')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'symp:notes','notes')" />
                <xsl:copy-of select="svfn:renderPropertyFromField(.,'symp:pii','pii')" />
                <xsl:if test="$publicationDateObject/*"><vivo:dateTimeValue rdf:resource="{$publicationDateURI}" /></xsl:if>
                <xsl:if test="$publicationVenueObject/*"><vivo:hasPublicationVenue rdf:resource="${$publicationVenueURI}" /></xsl:if>
            </xsl:with-param>
        </xsl:call-template>

        <xsl:copy-of select="$publicationDateObject" />
        <xsl:copy-of select="$publicationVenueObject" />
    </xsl:template>

    <!-- ====================================================
         XSLT Function Library
         ==================================================== -->

    <xsl:variable name="publication-types" select="document('elements-to-vivo-publication-types.xml')//config:publication-types" />
    <xsl:function name="svfn:getTypesForPublication">
        <xsl:param name="type" as="xs:string" />

        <xsl:variable name="publication-type">
            <xsl:choose>
                <xsl:when test="$publication-types/config:publication-type[@type=$type]"><xsl:copy-of select="$publication-types/config:publication-type[@type=$type]/*" /></xsl:when>
                <xsl:when test="$publication-types/config:publication-type[@type='z-default']"><xsl:copy-of select="$publication-types/config:publication-type[@type='z-default']/*" /></xsl:when>
                <xsl:otherwise><xsl:copy-of select="$publication-types/config:publication-type[1]/*" /></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$publication-type/vitro:mostSpecificType">
                <vitro:mostSpecificType rdf:resource="{$publication-type/vitro:mostSpecificType/@rdf:resource}" />
            </xsl:when>
            <xsl:otherwise>
                <vitro:mostSpecificType rdf:resource="{$publication-type/rdf:type[1]/@rdf:resource}" />
            </xsl:otherwise>
        </xsl:choose>
        <xsl:for-each select="$publication-type/rdf:type">
            <rdf:type rdf:resource="{@rdf:resource}" />
        </xsl:for-each>
        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#InformationResource"/>
        <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
    </xsl:function>

    <xsl:function name="svfn:selectJournalTitle">
        <xsl:param name="object" />
        <xsl:copy-of select="string(svfn:_selectJournalTitle($object, 1))" />
    </xsl:function>

    <xsl:function name="svfn:_selectJournalTitle">
        <xsl:param name="object" />
        <xsl:param name="position" as="xs:integer" />

        <xsl:if test="$journal-precedence[$position]">
            <xsl:choose>
                <xsl:when test="$journal-precedence[$position]/@type='authority' and $object/api:journal/api:records/api:record[@source-name=$journal-precedence[$position]]/api:title">
                    <xsl:value-of select="$object/api:journal/api:records/api:record[@source-name=$journal-precedence[$position]]/api:title" />
                </xsl:when>
                <xsl:when test="$journal-precedence[$position]/@type='datasource' and $object/api:records/api:record[@source-name=$journal-precedence[$position]]/api:native/api:field[@name=$journal-precedence[$position]/@field]/api:text">
                    <xsl:value-of select="$object/api:records/api:record[@source-name=$journal-precedence[$position]]/api:native/api:field[@name=$journal-precedence[$position]/@field]/api:text" />
                </xsl:when>
                <xsl:when test="$journal-precedence[$position]/@type='datasource' and $object/api:records/api:record[@source-name=$journal-precedence[$position]]/api:native/api:field[@name='journal']/api:text">
                    <xsl:value-of select="$object/api:records/api:record[@source-name=$journal-precedence[$position]]/api:native/api:field[@name='journal']/api:text" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="svfn:_selectJournalTitle($object, $position+1)" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:function>

    <xsl:function name="svfn:renderPublicationVenueObject">
        <xsl:param name="object" />
        <xsl:param name="journalObjectURI" as="xs:string" />
        <xsl:param name="journalTitle" as="xs:string" />

        <xsl:if test="$journalTitle">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$journalObjectURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://purl.org/ontology/bibo/Periodical"/>
                    <rdf:type rdf:resource="http://purl.org/ontology/bibo/Journal"/>
                    <rdf:type rdf:resource="http://purl.org/ontology/bibo/Collection"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#InformationResource"/>
                    <rdfs:label><xsl:value-of select="$journalTitle" /></rdfs:label>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:function>
</xsl:stylesheet>
