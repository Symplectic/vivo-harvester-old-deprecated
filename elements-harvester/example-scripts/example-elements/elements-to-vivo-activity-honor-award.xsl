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

    <xsl:import href="elements-to-vivo-config.xsl" />
    <xsl:import href="elements-to-vivo-utils.xsl" />

    <!-- Output the RDF objects for award data -->
    <xsl:template match="api:object[@category='activity' and @type='c-19']">
        <xsl:variable name="honorAwardName" select="api:records/api:record/api:native/api:field[@name='c-name']/api:text" />
        <xsl:if test="$honorAwardName">
            <!--
                Create a distinct object (URI) for each award.
                As the award may contain an award date (i.e. the date it was awarded to an individual),
                then the entire object with date must be distinct to each individual, even if it is the same award.
            -->
            <xsl:variable name="honorAwardURI" select="svfn:objectURI(.)" />

            <!-- If we were associating multiple individuals to the same award, we would use the following URI generator -->
            <!-- xsl:variable name="honorAwardURI" select="concat($baseURI, 'award-', svfn:stringToURI($honorAwardName))" / -->

            <xsl:variable name="honorAwardBeginDateObjectURI" select="concat($honorAwardURI, '-begin-date')" />
            <xsl:variable name="honorAwardBeginDateObject" select="svfn:renderDateObject(.,$honorAwardBeginDateObjectURI,api:records/api:record/api:native/api:field[@name='c-begin-date'])" />

            <xsl:call-template name="_render_rdf_object">
                <xsl:with-param name="objectURI" select="$honorAwardURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#AwardReceipt"/>

                    <xsl:if test="$honorAwardBeginDateObject/*" >
                        <vivo:dateTimeValue rdf:resource="{$honorAwardBeginDateObjectURI}"/>
                    </xsl:if>
                    <rdfs:label><xsl:value-of select="$honorAwardName" /></rdfs:label>
                </xsl:with-param>
            </xsl:call-template>

            <xsl:copy-of select="$honorAwardBeginDateObject" />
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>