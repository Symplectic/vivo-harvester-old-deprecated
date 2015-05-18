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
    <xsl:import href="elements-to-vivo-object-user-vcard.xsl" />
    <xsl:import href="elements-to-vivo-utils.xsl" />

    <!-- Match Elements objects of category 'user' -->
    <xsl:template match="api:object[@category='user']">
        <!-- Define URI and object variables -->
        <xsl:variable name="userId"><xsl:value-of select="@username" /></xsl:variable>
        <xsl:variable name="userURI" select="svfn:userURI(.)" />

        <xsl:variable name="isAcademic"><xsl:value-of select="api:is-academic" /></xsl:variable>
        <xsl:variable name="firstName"><xsl:value-of select="api:first-name" /></xsl:variable>
        <xsl:variable name="lastName"><xsl:value-of select="api:last-name" /></xsl:variable>
        <xsl:variable name="degrees" select="svfn:getRecordFieldOrFirst(.,'degrees')" />
        <xsl:variable name="academic-appointments" select="svfn:getRecordFieldOrFirst(.,'academic-appointments')" />
        <xsl:variable name="non-academic-employments" select="svfn:getRecordFieldOrFirst(.,'non-academic-employments')" />

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
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademic" />
                    </xsl:otherwise>
                </xsl:choose>
                <!-- rdf:type rdf:resource="http://vivoweb.org/harvester/excludeEntity" / -->
                <rdfs:label><xsl:value-of select="$lastName" />, <xsl:value-of select="$firstName" /></rdfs:label>
                <xsl:copy-of select="svfn:renderPropertyFromFieldOrFirst(.,'vivo:overview','overview')" />
            </xsl:with-param>
        </xsl:call-template>

        <!--
            Output the VCARD
        -->
        <xsl:apply-templates select="." mode="vcard" />

        <xsl:if test="$degrees/*">
            <xsl:for-each select="$degrees/api:degrees/api:degree[@privacy='public']">
                <xsl:variable name="awardedDegreeName" select="api:name" />
                <xsl:variable name="awardedDegreeField" select="api:field-of-study" />
                <!-- XXX: Tried using encode-for-uri() instead of translate() to handle spaces and other
                     invalid characters for a URI, but VIVO has a rendering issue, having been designed to
                     expect paths like /individual/n999 -->
                <xsl:variable name="awardedDegreeURI" select="svfn:makeURI('degree-', concat($userId,'-',$awardedDegreeName,'-',$awardedDegreeField))" />
                <xsl:variable name="eduProcessURI" select="svfn:makeURI('eduprocess-', concat($userId,'-',$awardedDegreeName,'-',$awardedDegreeField))" />

                <!-- XXX: Ideally these will be unique identifiers in the future that can map to unique individuals in VIVO -->
                <xsl:variable name="orgObjects" select="svfn:organisationObjects(api:institution)" />
                <xsl:variable name="orgURI" select="svfn:organisationObjectsMainURI($orgObjects)" />

                <xsl:variable name="degreeURI" select="svfn:makeURI('degree-', concat($awardedDegreeName,'-',$awardedDegreeField))" />

                <!-- Output RDF for vivo:University individual -->
                <xsl:copy-of select="$orgObjects" />

                <!-- Output RDF for vivo:AcademicDegree individual -->
                <!-- TODO: Might be possible to map to pre-defined VIVO 1.7 degrees, e.g. http://vivoweb.org/ontology/degree/academicDegree33 (B.S. Bachelor of Science) -->
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$degreeURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#AcademicDegree" />
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
                            <!-- For a degree only, add a condition to "close" the interval with the start date if end date doesn't exist to avoid the appearance of it looking current -->
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
                        <vivo:dateTimeInterval rdf:resource="{$dateIntervalURI}" />
                        <xsl:if test="$orgObjects/*"><obo:RO_0000057 rdf:resource="{$orgURI}" /></xsl:if>
                        <obo:RO_0000057 rdf:resource="{$userURI}" />
                        <obo:RO_0002234 rdf:resource="{$awardedDegreeURI}" />
                    </xsl:with-param>
                </xsl:call-template>

                <!-- Output RDF for vivo:AwardedDegree individual-->
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$awardedDegreeURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#AwardedDegree" />
                        <rdfs:label><xsl:value-of select="concat($lastName, ', ', $firstName, ': ', $awardedDegreeName, ' ', $awardedDegreeField)" /></rdfs:label>  <!-- VIVO includes name, e.g. "Smith, John: B.S. Bachelor of Science" -->
                        <xsl:if test="$orgObjects/*"><vivo:assignedBy rdf:resource="{$orgURI}" /></xsl:if>
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

                <xsl:if test="$orgObjects/*">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$orgURI" />
                        <xsl:with-param name="rdfNodes">
                            <vivo:relatedBy rdf:resource="{$awardedDegreeURI}" />
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>
            </xsl:for-each>
        </xsl:if>

        <xsl:if test="$academic-appointments/*">
            <xsl:for-each select="$academic-appointments/api:academic-appointments/api:academic-appointment[@privacy='public']">
                <xsl:variable name="appointmentURI" select="svfn:makeURI('appointment-', concat($userId,'-',position()))" />  <!-- TODO: using position() is weak!!! -->

                <!-- XXX: Ideally these will be unique identifiers in the future that can map to unique individuals in VIVO -->
                <xsl:variable name="orgObjects" select="svfn:organisationObjects(api:institution)" />
                <xsl:variable name="orgURI" select="svfn:organisationObjectsMainURI($orgObjects)" />

                <!-- Output RDF for vivo:University individual -->
                <xsl:copy-of select="$orgObjects" />

                <!-- Output RDF for vivo:DateTimeInterval and child vivo:DateTimeValue individuals -->
                <xsl:variable name="startDate" select="api:start-date" />
                <xsl:variable name="endDate" select="api:end-date" />
                <xsl:variable name="startDateURI" select="concat($appointmentURI,'-startDate')" />
                <xsl:variable name="endDateURI" select="concat($appointmentURI,'-endDate')" />
                <xsl:variable name="dateIntervalURI" select="concat($appointmentURI,'-dateInterval')" />

                <xsl:if test="$startDate/*">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$startDateURI" />
                        <xsl:with-param name="rdfNodes">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue" />
                            <!-- XXX: Some Elements date values include a month, and might even include a day? -->
                            <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="concat($startDate/api:year,'-01-01T00:00:00')" /></vivo:dateTime>
                            <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision" />
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>

                <xsl:if test="$endDate/*">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$endDateURI" />
                        <xsl:with-param name="rdfNodes">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue" />
                            <!-- XXX: Some Elements date values include a month, and might even include a day? -->
                            <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="concat($endDate/api:year,'-01-01T00:00:00')" /></vivo:dateTime>
                            <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision" />
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>

                <xsl:if test="$startDate/* or $endDate/*">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$dateIntervalURI" />
                        <xsl:with-param name="rdfNodes">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeInterval" />
                            <xsl:if test="$startDate">
                                <vivo:start rdf:resource="{$startDateURI}" />
                            </xsl:if>
                            <xsl:if test="$endDate">
                                <vivo:end rdf:resource="{$endDateURI}" />
                            </xsl:if>
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>

                <!-- Output RDF for vivo:Position individual -->
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$appointmentURI" />
                    <xsl:with-param name="rdfNodes">
                        <!-- TODO Implement a dictionary to convert position into type of position -->
                        <!-- XXX: vivo:Position is the "Other" select option in VIVO 1.7 user interface. This
                             could also be vivo:FacultyPosition, vivo:FacultyAdministrativePosition,
                             vivo:LibrarianPosition, vivo:NonFacultyAcademicPosition, vivo:PostdocPosition,
                             or vivo:PrimaryPosition -->
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Position" />
                        <rdfs:label><xsl:value-of select="api:position" /></rdfs:label>
                        <xsl:if test="$startDate/* or $endDate/*">
                            <vivo:dateTimeInterval rdf:resource="{$dateIntervalURI}" />
                        </xsl:if>
                        <!--
                            Link to department if available, otherwise organisation
                        -->
                        <xsl:if test="$orgObjects/*"><vivo:relates rdf:resource="{$orgURI}" /></xsl:if>
                        <vivo:relates rdf:resource="{$userURI}" />
                    </xsl:with-param>
                </xsl:call-template>

                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$userURI" />
                    <xsl:with-param name="rdfNodes">
                        <vivo:relatedBy rdf:resource="{$appointmentURI}" />
                    </xsl:with-param>
                </xsl:call-template>

                <xsl:if test="$orgObjects/*">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$orgURI" />
                        <xsl:with-param name="rdfNodes">
                            <vivo:relatedBy rdf:resource="{$appointmentURI}" />
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>
            </xsl:for-each>
        </xsl:if>

        <xsl:if test="$non-academic-employments">
            <xsl:for-each select="$non-academic-employments/api:non-academic-employments/api:non-academic-employment[@privacy='public']">
                <xsl:variable name="appointmentURI" select="svfn:makeURI('employment-', concat($userId,'-',position()))" />  <!-- TODO: using position() is weak!!! -->

                <!-- XXX: Ideally these will be unique identifiers in the future that can map to unique individuals in VIVO -->
                <xsl:variable name="orgObjects" select="svfn:organisationObjects(api:employer)" />
                <xsl:variable name="orgURI" select="svfn:organisationObjectsMainURI($orgObjects)" />

                <!-- Output RDF for foaf:Organization individual -->
                <xsl:copy-of select="$orgObjects" />

                <!-- Output RDF for vivo:DateTimeInterval and child vivo:DateTimeValue individuals -->
                <xsl:variable name="startDate" select="api:start-date" />
                <xsl:variable name="endDate" select="api:end-date" />
                <xsl:variable name="startDateURI" select="concat($appointmentURI,'-startDate')" />
                <xsl:variable name="endDateURI" select="concat($appointmentURI,'-endDate')" />
                <xsl:variable name="dateIntervalURI" select="concat($appointmentURI,'-dateInterval')" />

                <xsl:if test="$startDate/*">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$startDateURI" />
                        <xsl:with-param name="rdfNodes">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue" />
                            <!-- XXX: Some Elements date values include a month, and might even include a day? -->
                            <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="concat($startDate/api:year,'-01-01T00:00:00')" /></vivo:dateTime>
                            <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision" />
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>

                <xsl:if test="$endDate/*">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$endDateURI" />
                        <xsl:with-param name="rdfNodes">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue" />
                            <!-- XXX: Some Elements date values include a month, and might even include a day? -->
                            <vivo:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="concat($endDate/api:year,'-01-01T00:00:00')" /></vivo:dateTime>
                            <vivo:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision" />
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>

                <xsl:if test="$startDate/* or $endDate/*">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$dateIntervalURI" />
                        <xsl:with-param name="rdfNodes">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeInterval" />
                            <xsl:if test="$startDate">
                                <vivo:start rdf:resource="{$startDateURI}" />
                            </xsl:if>
                            <xsl:if test="$endDate">
                                <vivo:end rdf:resource="{$endDateURI}" />
                            </xsl:if>
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>

                <!-- Output RDF for vivo:NonAcademicPosition individual -->
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$appointmentURI" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademicPosition" />
                        <rdfs:label><xsl:value-of select="api:position" /></rdfs:label>
                        <xsl:if test="$startDate/* or $endDate/*">
                            <vivo:dateTimeInterval rdf:resource="{$dateIntervalURI}" />
                        </xsl:if>
                        <xsl:if test="$orgObjects/*"><vivo:relates rdf:resource="{$orgURI}" /></xsl:if>
                        <vivo:relates rdf:resource="{$userURI}" />
                    </xsl:with-param>
                </xsl:call-template>

                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$userURI" />
                    <xsl:with-param name="rdfNodes">
                        <vivo:relatedBy rdf:resource="{$appointmentURI}" />
                    </xsl:with-param>
                </xsl:call-template>

                <xsl:if test="$orgObjects/*">
                    <xsl:call-template name="render_rdf_object">
                        <xsl:with-param name="objectURI" select="$orgURI" />
                        <xsl:with-param name="rdfNodes">
                            <vivo:relatedBy rdf:resource="{$appointmentURI}" />
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
