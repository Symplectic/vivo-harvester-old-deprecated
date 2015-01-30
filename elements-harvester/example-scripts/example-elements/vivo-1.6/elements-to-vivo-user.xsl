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

    <!-- TODO: added here to support debugging/testing, and should be removed prior to committing changes
    <xsl:import href="elements-to-vivo-config.xsl" />
    -->

    <!-- Import XSLT files that are used -->
    <xsl:import href="elements-to-vivo-utils.xsl" />

    <!-- Match Elements objects of category 'user' -->
    <xsl:template match="api:object[@category='user']">

        <!-- Define URI and object variables -->
        <xsl:variable name="userId"><xsl:value-of select="@username" /></xsl:variable>
        <xsl:variable name="isAcademic"><xsl:value-of select="api:is-academic" /></xsl:variable>
        <xsl:variable name="firstName"><xsl:value-of select="api:first-name" /></xsl:variable>
        <xsl:variable name="lastName"><xsl:value-of select="api:last-name" /></xsl:variable>
        <xsl:variable name="overview"><xsl:value-of select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='overview']/api:text" /></xsl:variable>

        <xsl:variable name="vcardEmail"><xsl:value-of select="api:email-address" /></xsl:variable>
        <xsl:variable name="vcardOtherEmails"><xsl:value-of select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='email-addresses']" /></xsl:variable>
        <xsl:variable name="vcardAddresses"><xsl:value-of select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='addresses']" /></xsl:variable>
        <xsl:variable name="vcardPhoneNumbers"><xsl:value-of select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='phone-numbers']" /></xsl:variable>
        <xsl:variable name="vcardWebSites"><xsl:value-of select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='personal-websites']" /></xsl:variable>

        <!-- XXX: XSL from Emory pull request and is not generalized. Included as an example of
                  referencing organisation-defined data via API, with related XSL commented out below
        <xsl:variable name="vcardPhone"><xsl:value-of select="api:organisation-defined-data[@field-name='Work Telephone']" /></xsl:variable>
        <xsl:variable name="employeeType"><xsl:value-of select="api:organisation-defined-data[@field-name='Employee Type']" /></xsl:variable>
        -->

        <xsl:variable name="degrees"><xsl:value-of select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='degrees']" /></xsl:variable>
        <xsl:variable name="academic-appointments"><xsl:value-of select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='academic-appointments']" /></xsl:variable>
        <xsl:variable name="non-academic-employments"><xsl:value-of select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='non-academic-employments']" /></xsl:variable>

        <xsl:variable name="userURI" select="svfn:userURI(.)" />
        <xsl:variable name="vcardURI" select="concat($baseURI, 'vcard-', $userId)" />
        <xsl:variable name="vcardNameURI" select="concat($baseURI, 'vcardName-', $userId)" />
        <xsl:variable name="vcardEmailURI" select="concat($baseURI, 'vcardEmail-', $userId)" />

        <!-- XXX: Some values may come through in different fields depending on your version of Elements,
                  your institutional data sources, or your custom HR ingest
        <xsl:variable name="preferredTitle"><xsl:value-of select="api:title" /></xsl:variable>
        -->
        <xsl:variable name="titlePosition"><xsl:value-of select="api:position" /></xsl:variable>
        <xsl:variable name="titleDepartment"><xsl:value-of select="api:department" /></xsl:variable>
        <xsl:variable name="preferredTitle" select="concat($titlePosition, ', ', $titleDepartment)" />
        <xsl:variable name="vcardTitleURI" select="concat($baseURI, 'vcardTitle-', $userId)" />

        <!-- Output RDF for individual representing the user -->
        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="$userURI" />
            <xsl:with-param name="rdfNodes">
                <xsl:choose>
                    <!-- XXX: should this be only presented as a custom field? -->
                    <xsl:when test="$isAcademic = 'true'">
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyMember" />
                    </xsl:when>
                    <xsl:otherwise>
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Staff" />
                    </xsl:otherwise>
                </xsl:choose>

                <!-- XXX: Are all of these types necessary and can't be inferred??? -->
                <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
                <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000001" />
                <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000002" />
                <rdf:type rdf:resource="http://purl.obolibrary.org/obo/BFO_0000004" />
                <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Agent" />
                <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing" />
                <rdf:type rdf:resource="http://vivoweb.org/harvester/excludeEntity" />

                <rdfs:label><xsl:value-of select="$lastName" />, <xsl:value-of select="$firstName" /></rdfs:label>
                <obo:ARG_2000028 rdf:resource="{$vcardURI}" />

                <xsl:if test="$overview">
                    <vivo:overview><xsl:value-of select="$overview" /></vivo:overview>
                </xsl:if>
            </xsl:with-param>
        </xsl:call-template>

        <!-- Output RDF for work vcard:Individual individual -->
        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="$vcardURI" />
            <xsl:with-param name="rdfNodes">
                <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Individual" />
                <vcard:hasName rdf:resource="{$vcardNameURI}" />
                <obo:ARG_2000029 rdf:resource="{svfn:userURI(.)}" />
            </xsl:with-param>
        </xsl:call-template>

        <!-- Output RDF for work vcard:Name individual -->
        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="$vcardNameURI" />
            <xsl:with-param name="rdfNodes">
                <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Name" />
                <vcard:givenName><xsl:value-of select="api:first-name" /></vcard:givenName>
                <vcard:familyName><xsl:value-of select="api:last-name" /></vcard:familyName>
            </xsl:with-param>
        </xsl:call-template>

        <!-- Output RDF for vcard:Title individual -->
        <xsl:if test="$preferredTitle">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$vcardTitleURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Title" />
                    <vcard:title><xsl:value-of select="$preferredTitle" /></vcard:title>
                </xsl:with-param>
            </xsl:call-template>

            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$vcardURI" />
                <xsl:with-param name="rdfNodes">
                    <vcard:hasTitle rdf:resource="{$vcardTitleURI}" />
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>

        <!-- Output RDF for primary (work) vcard:Email individual -->
        <xsl:if test="$vcardEmail">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$vcardEmailURI" />
                <xsl:with-param name="rdfNodes">
                    <!-- Making the Email both an "Email" and "Work" type will render the field in the "Primary" section -->
                    <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Email" />
                    <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Work" />
                    <vcard:email><xsl:value-of select="$vcardEmail" /></vcard:email>
                </xsl:with-param>
            </xsl:call-template>

            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$vcardURI" />
                <xsl:with-param name="rdfNodes">
                    <vcard:hasEmail rdf:resource="{$vcardEmailURI}" />
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>

        <!-- Output RDF for additional vcard:Email individuals -->
        <xsl:if test="$vcardOtherEmails">
            <xsl:for-each select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='email-addresses']/api:email-addresses/api:email-address[@privacy='public']">
                <xsl:variable name="vcardOtherEmail" select="api:address" />
                <xsl:variable name="vcardOtherEmailURI" select="concat($baseURI, 'vcardOtherEmail-', $userId, '-', translate($vcardOtherEmail, ' ', ''))" />

                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$vcardOtherEmailURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Email" />
                        <vcard:email><xsl:value-of select="$vcardOtherEmail" /></vcard:email>
                    </xsl:with-param>
                </xsl:call-template>

                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$vcardURI" />
                    <xsl:with-param name="rdfNodes">
                        <vcard:hasEmail rdf:resource="{$vcardOtherEmailURI}" />
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>

        <!-- Output RDF for vcard:Address individuals -->
        <xsl:if test="$vcardAddresses">
            <xsl:for-each select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='addresses']/api:addresses/api:address[@privacy='public']">
                <xsl:variable name="vcardAddressOrganisation" select="if (api:line[@type='organisation']) then concat(api:line[@type='organisation'], '; ') else ''" />
                <xsl:variable name="vcardAddressDepartment" select="if (api:line[@type='suborganisation']) then concat(api:line[@type='suborganisation'], '; ') else ''" />
                <xsl:variable name="vcardAddressStreet" select="api:line[@type='street-address']" />
                <xsl:variable name="vcardAddressCity" select="api:line[@type='city']" />
                <xsl:variable name="vcardAddressState" select="api:line[@type='state']" />
                <xsl:variable name="vcardAddressZip" select="api:line[@type='zip-code']" />
                <xsl:variable name="vcardAddressCountry" select="api:line[@type='country']" />
                <xsl:variable name="vcardAddressURI" select="concat($baseURI, 'vcardAddress-', $userId, '-', translate($vcardAddressStreet, ' ', ''))" />

                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$vcardAddressURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Address" />
                        <vcard:streetAddress><xsl:value-of select="concat($vcardAddressOrganisation, $vcardAddressDepartment, $vcardAddressStreet)" /></vcard:streetAddress>
                        <vcard:locality><xsl:value-of select="$vcardAddressCity" /></vcard:locality>
                        <vcard:postalCode><xsl:value-of select="$vcardAddressZip" /></vcard:postalCode>
                        <vcard:region><xsl:value-of select="$vcardAddressState" /></vcard:region>
                        <vcard:country><xsl:value-of select="$vcardAddressCountry" /></vcard:country>
                    </xsl:with-param>
                </xsl:call-template>

                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$vcardURI" />
                    <xsl:with-param name="rdfNodes">
                        <vcard:hasAddress rdf:resource="{$vcardAddressURI}" />
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>

        <!-- Output RDF for vcard:Telephone individuals -->
        <xsl:if test="$vcardPhoneNumbers">
            <xsl:for-each select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='phone-numbers']/api:phone-numbers/api:phone-number[@privacy='public']">
                <xsl:variable name="vcardPhoneNumber" select="api:number" />
                <xsl:variable name="vcardPhoneNumberURI" select="concat($baseURI, 'vcardPhoneNumber-', $userId, '-', translate($vcardPhoneNumber, ' ', ''))" />

                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$vcardPhoneNumberURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Telephone" />
                        <vcard:telephone><xsl:value-of select="$vcardPhoneNumber" /></vcard:telephone>
                    </xsl:with-param>
                </xsl:call-template>

                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$vcardURI" />
                    <xsl:with-param name="rdfNodes">
                        <vcard:hasTelephone rdf:resource="{$vcardPhoneNumberURI}" />
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>

        <!-- Output RDF for vcard:URL individuals -->
        <xsl:if test="$vcardWebSites">
            <xsl:for-each select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='personal-websites']/api:web-addresses/api:web-address[@privacy='public']">
                <xsl:variable name="vcardUrl" select="api:url" />
                <xsl:variable name="vcardUrlLabel" select="api:label" />
                <xsl:variable name="vcardUrlURI" select="concat($baseURI, 'vcardUrl-', $userId, '-', translate($vcardUrl, ' ', ''))" />

                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$vcardUrlURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#URL" />
                        <vcard:url><xsl:value-of select="$vcardUrl" /></vcard:url>
                        <rdfs:label><xsl:value-of select="$vcardUrlLabel" /></rdfs:label>
                    </xsl:with-param>
                </xsl:call-template>

                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$vcardURI" />
                    <xsl:with-param name="rdfNodes">
                        <vcard:hasURL rdf:resource="{$vcardUrlURI}" />
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>

        <xsl:if test="$degrees">
            <xsl:for-each select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='degrees']/api:degrees/api:degree[@privacy='public']">
                <xsl:variable name="awardedDegreeName" select="api:name" />
                <xsl:variable name="awardedDegreeField" select="api:field-of-study" />
                <!-- XXX: Tried using encode-for-uri() instead of translate() to handle spaces and other
                     invalid characters for a URI, but VIVO has a rendering issue, having been designed to
                     expect paths like /individual/n999 -->
                <xsl:variable name="awardedDegreeURI" select="concat($baseURI, 'degree-', $userId, '-', translate($awardedDegreeName, ' ', ''), '-', translate($awardedDegreeField, ' ', ''))" />
                <xsl:variable name="eduProcessURI" select="concat($baseURI, 'eduprocess-', $userId, '-', translate($awardedDegreeName, ' ', ''), '-', translate($awardedDegreeField, ' ', ''))" />

                <!-- XXX: Ideally these will be unique identifiers in the future that can map to unique individuals in VIVO -->
                <xsl:variable name="orgName" select="api:institution/api:line[@type='organisation']" />
                <xsl:variable name="orgURI" select="concat($baseURI, 'institution-', translate($orgName, ' ', ''))" />
                <xsl:variable name="degreeURI" select="concat($baseURI, 'degree-', translate($awardedDegreeName, ' ', ''), '-', translate($awardedDegreeField, ' ', ''))" />

                <!-- Output RDF for vivo:University individual -->
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$orgURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#University" />
                        <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization" />
                        <vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#University" />
                        <rdfs:label><xsl:value-of select="$orgName" /></rdfs:label>
                        <!-- XXX: api:line output for sub-organisation, street, city, state/province, zip/postal, and country -->
                    </xsl:with-param>
                </xsl:call-template>

                <!-- Output RDF for vivo:AcademicDegree individual -->
                <!-- XXX: Consider future mapping to existing VIVO-ISF degree individuals, e.g. http://vivoweb.org/ontology/degree/academicDegree33 (B.S. Bachelor of Science) -->
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$degreeURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#AcademicDegree" />
                        <vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#AcademicDegree" />
                        <rdfs:label><xsl:value-of select="concat($awardedDegreeName, ' ', $awardedDegreeField)" /></rdfs:label>
                    </xsl:with-param>
                </xsl:call-template>

                <!-- Output RDF for vivo:DateTimeInterval and child vivo:DateTimeValue individuals -->
                <xsl:variable name="startDate" select="api:start-date/api:year" />
                <xsl:variable name="endDate" select="api:end-date/api:year" />
                <xsl:variable name="startDateURI" select="concat($awardedDegreeURI,'-startDate')" />
                <xsl:variable name="endDateURI" select="concat($awardedDegreeURI,'-endDate')" />
                <xsl:variable name="dateIntervalURI">
                    <xsl:choose>
                        <xsl:when test="$endDate">
                            <xsl:value-of select="concat($awardedDegreeURI,'-dateInterval-',$startDate,'-',$endDate)" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="concat($awardedDegreeURI,'-dateInterval-',$startDate)" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:if test="$startDate">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$startDateURI" />
                        <xsl:with-param name="rdfNodes">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue" />
                            <!-- XXX: Some Elements date values include a month, and might even include a day? -->
                            <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="concat($startDate,'-01-01T00:00:00')" /></vivo:dateTime>
                            <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision" />
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>

                <xsl:if test="$endDate">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$endDateURI" />
                        <xsl:with-param name="rdfNodes">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue" />
                            <!-- XXX: Some Elements date values include a month, and might even include a day? -->
                            <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="concat($endDate,'-01-01T00:00:00')" /></vivo:dateTime>
                            <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision" />
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>

                <xsl:if test="$startDate or $endDate">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$dateIntervalURI" />
                        <xsl:with-param name="rdfNodes">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeInterval" />
                            <xsl:if test="$startDate">
                                <vivo:start rdf:resource="{$startDateURI}" />
                            </xsl:if>
                            <!-- XXX: Add a condition to "close" the interval with the start date if end date doesn't exist to avoid the appearance of it looking current -->
                            <xsl:choose>
                                <xsl:when test="$endDate">
                                    <vivo:end rdf:resource="{$endDateURI}" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <vivo:end rdf:resource="{$startDateURI}" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>

                <!-- Output RDF for vivo:EducationalProcess individual-->
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$eduProcessURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#EducationalProcess" />
                        <vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#EducationalProcess" />
                        <vivo:dateTimeInterval rdf:resource="{$dateIntervalURI}" />
                        <obo:RO_0000057 rdf:resource="{$orgURI}" />
                        <obo:RO_0000057 rdf:resource="{$userURI}" />
                        <obo:RO_0002234 rdf:resource="{$awardedDegreeURI}" />
                    </xsl:with-param>
                </xsl:call-template>

                <!-- Output RDF for vivo:AwardedDegree individual-->
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$awardedDegreeURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#AwardedDegree" />
                        <vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#AwardedDegree" />
                        <rdfs:label><xsl:value-of select="concat($lastName, ', ', $firstName, ': ', $awardedDegreeName, ' ', $awardedDegreeField)" /></rdfs:label>  <!-- VIVO includes name, e.g. "Smith, John: B.S. Bachelor of Science" -->
                        <vivo:assignedBy rdf:resource="{$orgURI}" />
                        <vivo:relates rdf:resource="{$degreeURI}" />
                        <vivo:relates rdf:resource="{$userURI}" />
                        <obo:RO_0002353 rdf:resource="{$eduProcessURI}" />
                    </xsl:with-param>
                </xsl:call-template>

                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$userURI" />
                    <xsl:with-param name="rdfNodes">
                        <vivo:relatedBy rdf:resource="{$awardedDegreeURI}" />
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>

        <xsl:if test="$academic-appointments">
            <xsl:for-each select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='academic-appointments']/api:academic-appointments/api:academic-appointment[@privacy='public']">
                <xsl:variable name="appointmentURI" select="concat($baseURI, 'appointment-', $userId, '-', position())" />  <!-- XXX: consider alternatives to using position() as a counter -->

                <!-- XXX: Ideally these will be unique identifiers in the future that can map to unique individuals in VIVO -->
                <xsl:variable name="orgName" select="api:institution/api:line[@type='organisation']" />
                <xsl:variable name="orgURI" select="concat($baseURI, 'institution-', translate($orgName, ' ', ''))" />

                <!-- Output RDF for vivo:University individual -->
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$orgURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#University" />
                        <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization" />
                        <vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#University" />
                        <rdfs:label><xsl:value-of select="$orgName" /></rdfs:label>
                        <!-- XXX: api:line output for sub-organisation, street, city, state/province, zip/postal, and country -->
                    </xsl:with-param>
                </xsl:call-template>

                <!-- Output RDF for vivo:DateTimeInterval and child vivo:DateTimeValue individuals -->
                <xsl:variable name="startDate" select="api:start-date/api:year" />
                <xsl:variable name="endDate" select="api:end-date/api:year" />
                <xsl:variable name="startDateURI" select="concat($appointmentURI,'-startDate')" />
                <xsl:variable name="endDateURI" select="concat($appointmentURI,'-endDate')" />
                <xsl:variable name="dateIntervalURI">
                    <xsl:choose>
                        <xsl:when test="$endDate">
                            <xsl:value-of select="concat($appointmentURI,'-dateInterval-',$startDate,'-',$endDate)" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="concat($appointmentURI,'-dateInterval-',$startDate)" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:if test="$startDate">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$startDateURI" />
                        <xsl:with-param name="rdfNodes">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue" />
                            <!-- XXX: Some Elements date values include a month, and might even include a day? -->
                            <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="concat($startDate,'-01-01T00:00:00')" /></vivo:dateTime>
                            <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision" />
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>

                <xsl:if test="$endDate">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$endDateURI" />
                        <xsl:with-param name="rdfNodes">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue" />
                            <!-- XXX: Some Elements date values include a month, and might even include a day? -->
                            <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="concat($endDate,'-01-01T00:00:00')" /></vivo:dateTime>
                            <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision" />
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>

                <xsl:if test="$startDate or $endDate">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$dateIntervalURI" />
                        <xsl:with-param name="rdfNodes">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeInterval" />
                            <xsl:if test="$startDate">
                                <vivo:start rdf:resource="{$startDateURI}" />
                            </xsl:if>
                            <!-- XXX: Add a condition to "close" the interval with the start date if end date doesn't exist to avoid the appearance of it looking current -->
                            <xsl:choose>
                                <xsl:when test="$endDate">
                                    <vivo:end rdf:resource="{$endDateURI}" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <vivo:end rdf:resource="{$startDateURI}" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>

                <!-- Output RDF for vivo:Position individual -->
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$appointmentURI" />
                    <xsl:with-param name="rdfNodes">
                        <!-- XXX: vivo:Position is the "Other" select option in VIVO 1.7 user interface. This
                             could also be vivo:FacultyPosition, vivo:FacultyAdministrativePosition,
                             vivo:LibrarianPosition, vivo:NonFacultyAcademicPosition, vivo:PostdocPosition,
                             or vivo:PrimaryPosition -->
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Position" />
                        <vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#Position" />
                        <rdfs:label><xsl:value-of select="api:position" /></rdfs:label>
                        <vivo:dateTimeInterval rdf:resource="{$dateIntervalURI}" />
                        <vivo:relates rdf:resource="{$orgURI}" />
                        <vivo:relates rdf:resource="{$userURI}" />
                    </xsl:with-param>
                </xsl:call-template>

                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$userURI" />
                    <xsl:with-param name="rdfNodes">
                        <vivo:relatedBy rdf:resource="{$appointmentURI}" />
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>

        <xsl:if test="$non-academic-employments">
            <xsl:for-each select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='non-academic-employments']/api:non-academic-employments/api:non-academic-employment[@privacy='public']">
                <xsl:variable name="appointmentURI" select="concat($baseURI, 'employment-', $userId, '-', position())" />  <!-- XXX: consider alternatives to using position() as a counter -->

                <!-- XXX: Ideally these will be unique identifiers in the future that can map to unique individuals in VIVO -->
                <xsl:variable name="orgName" select="api:employer/api:line[@type='organisation']" />
                <xsl:variable name="orgURI" select="concat($baseURI, 'employer-', translate($orgName, ' ', ''))" />

                <!-- Output RDF for foaf:Organization individual -->
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$orgURI" />
                    <xsl:with-param name="rdfNodes">
                        <!-- XXX: This could be more specific, i.e. vivo:University or vivo:Company instead of the generic "organization" -->
                        <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization" />
                        <vitro:mostSpecificType rdf:resource="http://xmlns.com/foaf/0.1/Organization" />
                        <rdfs:label><xsl:value-of select="$orgName" /></rdfs:label>
                        <!-- XXX: api:line output for sub-organisation, street, city, state/province, zip/postal, and country -->
                    </xsl:with-param>
                </xsl:call-template>

                <!-- Output RDF for vivo:DateTimeInterval and child vivo:DateTimeValue individuals -->
                <xsl:variable name="startDate" select="api:start-date/api:year" />
                <xsl:variable name="endDate" select="api:end-date/api:year" />
                <xsl:variable name="startDateURI" select="concat($appointmentURI,'-startDate')" />
                <xsl:variable name="endDateURI" select="concat($appointmentURI,'-endDate')" />
                <xsl:variable name="dateIntervalURI">
                    <xsl:choose>
                        <xsl:when test="$endDate">
                            <xsl:value-of select="concat($appointmentURI,'-dateInterval-',$startDate,'-',$endDate)" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="concat($appointmentURI,'-dateInterval-',$startDate)" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:if test="$startDate">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$startDateURI" />
                        <xsl:with-param name="rdfNodes">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue" />
                            <vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue" />
                            <!-- XXX: Some Elements date values include a month, and might even include a day? -->
                            <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="concat($startDate,'-01-01T00:00:00')" /></vivo:dateTime>
                            <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision" />
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>

                <xsl:if test="$endDate">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$endDateURI" />
                        <xsl:with-param name="rdfNodes">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue" />
                            <!-- XXX: Some Elements date values include a month, and might even include a day? -->
                            <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="concat($endDate,'-01-01T00:00:00')" /></vivo:dateTime>
                            <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision" />
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>

                <xsl:if test="$startDate or $endDate">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$dateIntervalURI" />
                        <xsl:with-param name="rdfNodes">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeInterval" />
                            <xsl:if test="$startDate">
                                <vivo:start rdf:resource="{$startDateURI}" />
                            </xsl:if>
                            <!-- XXX: Add a condition to "close" the interval with the start date if end date doesn't exist to avoid the appearance of it looking current -->
                            <xsl:choose>
                                <xsl:when test="$endDate">
                                    <vivo:end rdf:resource="{$endDateURI}" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <vivo:end rdf:resource="{$startDateURI}" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>

                <!-- Output RDF for vivo:NonAcademicPosition individual -->
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$appointmentURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademicPosition" />
                        <vitro:mostSpecificType rdf:resource="http://vivoweb.org/ontology/core#NonAcademicPosition" />
                        <rdfs:label><xsl:value-of select="api:position" /></rdfs:label>
                        <vivo:dateTimeInterval rdf:resource="{$dateIntervalURI}" />
                        <vivo:relates rdf:resource="{$orgURI}" />
                        <vivo:relates rdf:resource="{$userURI}" />
                    </xsl:with-param>
                </xsl:call-template>

                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$userURI" />
                    <xsl:with-param name="rdfNodes">
                        <vivo:relatedBy rdf:resource="{$appointmentURI}" />
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>

    </xsl:template>

</xsl:stylesheet>
