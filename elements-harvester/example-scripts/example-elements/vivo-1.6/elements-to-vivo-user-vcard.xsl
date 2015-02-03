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

    <!-- Import XSLT files that are used -->
    <xsl:import href="elements-to-vivo-utils.xsl" />

    <!-- Match Elements objects of category 'user' -->
    <xsl:template match="api:object[@category='user']" mode="vcard">
        <!-- Define URI and object variables -->
        <xsl:variable name="userId"><xsl:value-of select="@username" /></xsl:variable>
        <xsl:variable name="userURI" select="svfn:userURI(.)" />

        <xsl:variable name="vcardURI" select="concat($baseURI, 'vcard-', $userId)" />

        <xsl:variable name="vcardEmailURI" select="concat($baseURI, 'vcardEmail-', $userId)" />
        <xsl:variable name="vcardEmailObject" select="svfn:renderVcardEmailObject(.,$vcardEmailURI,api:email-address)" />

        <xsl:variable name="vcardNameURI" select="concat($baseURI, 'vcardName-', $userId)" />
        <xsl:variable name="vcardNameObject" select="svfn:renderVcardNameObject(.,$vcardNameURI)" />

        <xsl:variable name="vcardTitleURI" select="concat($baseURI, 'vcardTitle-', $userId)" />
        <xsl:variable name="vcardTitleObject" select="svfn:renderVcardTitleObject(.,$vcardTitleURI)" />

        <xsl:variable name="vcardAddresses"><xsl:value-of select="svfn:getRecordFieldOrFirst(.,'addresses')" /></xsl:variable>
        <xsl:variable name="vcardOtherEmails"><xsl:value-of select="svfn:getRecordFieldOrFirst(.,'email-addresses')" /></xsl:variable>
        <xsl:variable name="vcardPhoneNumbers"><xsl:value-of select="svfn:getRecordFieldOrFirst(.,'phone-numbers')" /></xsl:variable>
        <xsl:variable name="vcardWebSites"><xsl:value-of select="svfn:getRecordFieldOrFirst(.,'personal-websites')" /></xsl:variable>

        <!-- Individual to vcard link -->
        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="$userURI" />
            <xsl:with-param name="rdfNodes">
                <obo:ARG_2000028 rdf:resource="{$vcardURI}" />
            </xsl:with-param>
        </xsl:call-template>

        <!-- vcard -->
        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="$vcardURI" />
            <xsl:with-param name="rdfNodes">
                <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Individual" />
                <obo:ARG_2000029 rdf:resource="{svfn:userURI(.)}" />
                <xsl:if test="$vcardEmailObject">
                    <vcard:hasEmail rdf:resource="{$vcardEmailURI}" />
                </xsl:if>
                <xsl:if test="$vcardNameObject">
                    <vcard:hasName rdf:resource="{$vcardNameURI}" />
                </xsl:if>
                <xsl:if test="$vcardTitleObject">
                    <vcard:hasTitle rdf:resource="{$vcardTitleURI}" />
                </xsl:if>
                <xsl:if test="$vcardAddresses">
                    <xsl:for-each select="$vcardAddresses/api:addresses/api:address[@privacy='public']">
                        <xsl:variable name="vcardStreetAddress">
                            <xsl:value-of select="api:line[@type='organisation'],api:line[@type='suborganisation'],api:line[@type='street-address']" separator="; " />
                        </xsl:variable>
                        <xsl:variable name="vcardAddressURI" select="concat($baseURI, 'vcardAddress-', $userId, '-', translate($vcardStreetAddress, ' ', ''))" />
                        <vcard:hasAddress rdf:resource="{$vcardAddressURI}" />
                    </xsl:for-each>
                </xsl:if>
                <xsl:if test="$vcardOtherEmails">
                    <xsl:for-each select="$vcardOtherEmails/api:email-addresses/api:email-address[@privacy='public']">
                        <vcard:hasEmail rdf:resource="{concat($baseURI, 'vcardOtherEmail-', $userId, '-', translate(api:address, ' ', ''))}" />
                    </xsl:for-each>
                </xsl:if>
                <xsl:if test="$vcardPhoneNumbers">
                    <xsl:for-each select="$vcardPhoneNumbers/api:phone-numbers/api:phone-number[@privacy='public']">
                        <vcard:hasTelephone rdf:resource="{concat($baseURI, 'vcardPhoneNumber-', $userId, '-', translate(api:number, ' ', ''))}" />
                    </xsl:for-each>
                </xsl:if>
                <xsl:if test="$vcardWebSites">
                    <xsl:for-each select="$vcardWebSites/api:web-addresses/api:web-address[@privacy='public']">
                        <vcard:hasURL rdf:resource="{concat($baseURI, 'vcardUrl-', $userId, '-', translate(api:url, ' ', ''))}" />
                    </xsl:for-each>
                </xsl:if>
            </xsl:with-param>
        </xsl:call-template>

        <xsl:copy-of select="$vcardEmailObject" />
        <xsl:copy-of select="$vcardNameObject" />
        <xsl:copy-of select="$vcardTitleObject" />
        <xsl:if test="$vcardAddresses">
            <xsl:for-each select="$vcardAddresses/api:addresses/api:address[@privacy='public']">
                <xsl:variable name="vcardStreetAddress">
                    <xsl:value-of select="api:line[@type='organisation'],api:line[@type='suborganisation'],api:line[@type='street-address']" separator="; " />
                </xsl:variable>
                <xsl:variable name="vcardAddressURI" select="concat($baseURI, 'vcardAddress-', $userId, '-', translate($vcardStreetAddress, ' ', ''))" />
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$vcardAddressURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Address" />
                        <vcard:streetAddress><xsl:value-of select="$vcardStreetAddress" /></vcard:streetAddress>
                        <vcard:locality><xsl:value-of select="api:line[@type='city']" /></vcard:locality>
                        <vcard:region><xsl:value-of select="api:line[@type='state']" /></vcard:region>
                        <vcard:postalCode><xsl:value-of select="api:line[@type='zip-code']" /></vcard:postalCode>
                        <vcard:country><xsl:value-of select="api:line[@type='country']" /></vcard:country>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>
        <xsl:if test="$vcardOtherEmails">
            <xsl:for-each select="$vcardOtherEmails/api:email-addresses/api:email-address[@privacy='public']">
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="concat($baseURI, 'vcardOtherEmail-', $userId, '-', translate(api:address, ' ', ''))" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Email" />
                        <vcard:email><xsl:value-of select="api:address" /></vcard:email>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>
        <xsl:if test="$vcardPhoneNumbers">
            <xsl:for-each select="$vcardPhoneNumbers/api:phone-numbers/api:phone-number[@privacy='public']">
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="concat($baseURI, 'vcardPhoneNumber-', $userId, '-', translate(api:number, ' ', ''))" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Telephone" />
                        <vcard:telephone><xsl:value-of select="api:number" /></vcard:telephone>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>
        <xsl:if test="$vcardWebSites">
            <xsl:for-each select="$vcardWebSites/api:web-addresses/api:web-address[@privacy='public']">
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="concat($baseURI, 'vcardUrl-', $userId, '-', translate(api:url, ' ', ''))" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#URL" />
                        <vcard:url><xsl:value-of select="api:url" /></vcard:url>
                        <rdfs:label><xsl:value-of select="api:label" /></rdfs:label>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <xsl:function name="svfn:renderVcardEmailObject">
        <xsl:param name="object" />
        <xsl:param name="vcardEmailURI" as="xs:string" />
        <xsl:param name="vcardEmail" as="xs:string" />

        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="$vcardEmailURI" />
            <xsl:with-param name="rdfNodes">
                <!-- Making the Email both an "Email" and "Work" type will render the field in the "Primary" section -->
                <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Email" />
                <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Work" />
                <vcard:email><xsl:value-of select="$vcardEmail" /></vcard:email>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:function>

    <xsl:function name="svfn:renderVcardNameObject">
        <xsl:param name="object" />
        <xsl:param name="vcardNameURI" as="xs:string" />

        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="$vcardNameURI" />
            <xsl:with-param name="rdfNodes">
                <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Name" />
                <vcard:givenName><xsl:value-of select="$object/api:first-name" /></vcard:givenName>
                <vcard:familyName><xsl:value-of select="$object/api:last-name" /></vcard:familyName>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:function>

    <xsl:function name="svfn:renderVcardTitleObject">
        <xsl:param name="object" />
        <xsl:param name="vcardTitleURI" as="xs:string" />

        <xsl:variable name="positionAndDept"><xsl:value-of select="$object/api:position,$object/api:department" separator=", '" /></xsl:variable>
        <xsl:variable name="preferredTitle">
            <xsl:choose>
                <xsl:when test="not(string($positionAndDept))"><xsl:value-of select="$object/api:title" /></xsl:when>
                <xsl:otherwise><xsl:value-of select="$positionAndDept" /></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:if test="$preferredTitle">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$vcardTitleURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Title" />
                    <vcard:title><xsl:value-of select="$preferredTitle" /></vcard:title>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:function>
</xsl:stylesheet>
