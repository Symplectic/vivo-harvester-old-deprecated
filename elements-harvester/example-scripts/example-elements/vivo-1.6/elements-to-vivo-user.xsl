<?xml version="1.0" encoding="UTF-8"?>
<!--
 | Copyright (c) 2012 Symplectic. All rights reserved.
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
    <xsl:template match="api:object[@category='user']">

        <!-- Define URI and object variables -->
        <xsl:variable name="userId"><xsl:value-of select="@username" /></xsl:variable>
        <xsl:variable name="isAcademic"><xsl:value-of select="api:is-academic" /></xsl:variable>
        <xsl:variable name="firstName"><xsl:value-of select="api:first-name" /></xsl:variable>
        <xsl:variable name="lastName"><xsl:value-of select="api:last-name" /></xsl:variable>
        <xsl:variable name="vcardEmail"><xsl:value-of select="api:email-address" /></xsl:variable>

        <!-- XXX: XSL from Emory pull request and is not generalized. Included as an example of
                  referencing organisation-defined data via API, with related XSL commented out below
        <xsl:variable name="employeeType"><xsl:value-of select="api:organisation-defined-data[@field-name='Employee Type']" /></xsl:variable>
        <xsl:variable name="vcardPhone"><xsl:value-of select="api:organisation-defined-data[@field-name='Work Telephone']" /></xsl:variable>
        -->

        <xsl:variable name="preferredTitle"><xsl:value-of select="api:title" /></xsl:variable>
        <xsl:variable name="overview"><xsl:value-of select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='overview']/api:text" /></xsl:variable>

        <xsl:variable name="degrees"><xsl:value-of select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='degrees']" /></xsl:variable>
        <xsl:variable name="academic-appointments"><xsl:value-of select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='academic-appointments']" /></xsl:variable>
        <xsl:variable name="non-academic-employments"><xsl:value-of select="api:records/api:record[@source-name='manual']/api:native/api:field[@name='non-academic-employments']" /></xsl:variable>

        <xsl:variable name="userURI" select="svfn:userURI(.)" />

        <xsl:variable name="vcardURI" select="concat($baseURI, 'vcard-', $userId)" />
        <xsl:variable name="vcardEmailURI" select="concat($baseURI, 'vcardEmail-', $userId)" />
        <xsl:variable name="vcardNameURI" select="concat($baseURI, 'vcardName-', $userId)" />
        <!-- XXX: Example of using organisation-defined data from API
        <xsl:variable name="vcardPhoneURI" select="concat($baseURI, 'vcardTelephone-', $userId)" /> -->

        <!-- Output RDF for individual representing the user -->
        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="$userURI" />
            <xsl:with-param name="rdfNodes">
                <xsl:choose>
                    <!-- TODO: should this be only presented as a custom field? -->
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

                <!-- TODO: is this in the right place??? Should this be an asserted property of the vCard??? -->
                <xsl:if test="$vcardEmail">
                    <rdf:type rdf:resource="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing" />
                </xsl:if>

                <rdfs:label><xsl:value-of select="$lastName" />, <xsl:value-of select="$firstName" /></rdfs:label>
                <obo:ARG_2000028 rdf:resource="{$vcardURI}" />

                <!-- TODO: this has been moved into the vCard, but also seeing an empty string "" property in v1.7 -->
                <xsl:if test="$preferredTitle">
                    <vivo:preferredTitle><xsl:value-of select="$preferredTitle" /></vivo:preferredTitle>
                </xsl:if>

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
                <xsl:if test="$vcardEmail">
                    <vcard:hasEmail rdf:resource="{$vcardEmailURI}" />
                </xsl:if>
                <!-- XXX: Example of using organisation-defined data from API
                <xsl:if test="$vcardPhone">
                    <vcard:hasTelephone rdf:resource="{$vcardPhoneURI}" />
                </xsl:if> -->
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

        <!-- Output RDF for work vcard:Email individual -->
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
        </xsl:if>

        <!-- Output RDF for vcard:Telephone individual -->
        <!-- XXX: Example of using organisation-defined data from API
        <xsl:if test="$vcardPhone">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$vcardPhoneURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Telephone" />
                    <vcard:telephone>
                        <xsl:text>(</xsl:text>
                        <xsl:value-of select="substring($vcardPhone,1,3)" />
                        <xsl:text>)</xsl:text>
                        <xsl:value-of select="substring($vcardPhone,4,3)" />
                        <xsl:text>-</xsl:text>
                        <xsl:value-of select="substring($vcardPhone,7,4)" />
                    </vcard:telephone>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if> -->

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
                        <!-- TODO: api:line output for sub-organisation, street, city, state/province, zip/postal, and country -->
                    </xsl:with-param>
                </xsl:call-template>

                <!-- Output RDF for vivo:AcademicDegree individual -->
                <!-- TODO: Might be possible to map to pre-defined VIVO 1.7 degrees, e.g. http://vivoweb.org/ontology/degree/academicDegree33 (B.S. Bachelor of Science) -->
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
                <xsl:variable name="appointmentURI" select="concat($baseURI, 'appointment-', $userId, '-', position())" />  <!-- TODO: using position() is weak!!! -->

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
                        <!-- TODO: api:line output for sub-organisation, street, city, state/province, zip/postal, and country -->
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
                <xsl:variable name="appointmentURI" select="concat($baseURI, 'employment-', $userId, '-', position())" />  <!-- TODO: using position() is weak!!! -->

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
                        <!-- TODO: api:line output for sub-organisation, street, city, state/province, zip/postal, and country -->
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
