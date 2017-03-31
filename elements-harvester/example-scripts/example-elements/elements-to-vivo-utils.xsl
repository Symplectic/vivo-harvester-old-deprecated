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
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:bibo="http://purl.org/ontology/bibo/"
                xmlns:vivo="http://vivoweb.org/ontology/core#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:score="http://vivoweb.org/ontology/score#"
                xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/"
                xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
                xmlns:api="http://www.symplectic.co.uk/publications/api"
                xmlns:config="http://www.symplectic.co.uk/vivo/namespaces/config"
                xmlns:symp="http://www.symplectic.co.uk/ontology/elements/"
                xmlns:svfn="http://www.symplectic.co.uk/vivo/namespaces/functions"
                xmlns:obo="http://purl.obolibrary.org/obo/"
                exclude-result-prefixes="api config xs fn svfn"
                >

    <xsl:import href="elements-to-vivo-datatypes.xsl" />

    <xsl:param name="recordDir">data/raw-records/</xsl:param>
    <xsl:param name="includeDept">true</xsl:param>

    <xsl:variable name="organization-types" select="document('elements-to-vivo-config-organization-types.xml')//config:organization-types" />

    <!-- ======================================
         Function Library
         ======================================- -->

    <!--
    -->
    <xsl:function name="svfn:getOrganizationType">
        <xsl:param name="name" />
        <xsl:param name="default" />

        <xsl:choose>
            <xsl:when test="$organization-types/config:organization-type[@name=$name]"><xsl:value-of select="$organization-types/config:organization-type[@name=$name]/@type" /></xsl:when>
            <xsl:when test="contains($name,'University')"><xsl:text>http://vivoweb.org/ontology/core#University</xsl:text></xsl:when>
            <xsl:when test="contains($name,'College')"><xsl:text>http://vivoweb.org/ontology/core#College</xsl:text></xsl:when>
            <xsl:when test="contains($name,'Museum')"><xsl:text>http://vivoweb.org/ontology/core#Museum</xsl:text></xsl:when>
            <xsl:when test="contains($name,'Hospital')"><xsl:text>http://vivoweb.org/ontology/core#Hospital</xsl:text></xsl:when>
            <xsl:when test="contains($name,'Institute')"><xsl:text>http://vivoweb.org/ontology/core#Institute</xsl:text></xsl:when>
            <xsl:when test="contains($name,'School')"><xsl:text>http://vivoweb.org/ontology/core#School</xsl:text></xsl:when>
            <xsl:when test="contains($name,'Association')"><xsl:text>http://vivoweb.org/ontology/core#Association</xsl:text></xsl:when>
            <xsl:when test="contains($name,'Library')"><xsl:text>http://vivoweb.org/ontology/core#Library</xsl:text></xsl:when>
            <xsl:when test="contains($name,'Foundation')"><xsl:text>http://vivoweb.org/ontology/core#Foundation</xsl:text></xsl:when>
            <xsl:when test="contains($name,'Ltd')"><xsl:text>http://vivoweb.org/ontology/core#PrivateCompany</xsl:text></xsl:when>
            <xsl:otherwise><xsl:value-of select="$default" /></xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        svfn:makeURI
        ============
        Format a URI
    -->
    <xsl:function name="svfn:makeURI">
        <xsl:param name="prefix" as="xs:string" />
        <xsl:param name="id" as="xs:string" />

        <xsl:value-of select="concat($baseURI,svfn:stringToURI($prefix),svfn:stringToURI($id))" />
    </xsl:function>

    <!--
        svfn:objectURI
        ==============
        Create a URI for the RDF objects based on the passed Elements object
    -->
    <xsl:function name="svfn:objectURI" as="xs:string">
        <xsl:param name="object" />

        <xsl:value-of select="svfn:makeURI($object/@category,$object/@id)" />
    </xsl:function>

    <!--
        svfn:userURI
        ============
        Create a URI for a user based on the passed Elements object
    -->
    <xsl:function name="svfn:userURI" as="xs:string">
        <xsl:param name="object" />

        <xsl:value-of select="svfn:makeURI('',$object/@username)" />
    </xsl:function>

    <!--
        svfn:objectToObjectURI
        ======================
    -->
    <xsl:function name="svfn:objectToObjectURI" as="xs:string">
        <xsl:param name="prefix" as="xs:string" />
        <xsl:param name="objectid1" as="xs:string" />
        <xsl:param name="objectid2" as="xs:string" />

        <xsl:value-of select="svfn:makeURI($prefix,concat($objectid1,'-',$objectid2))" />
    </xsl:function>

    <!--
        svfn:relationshipURI
        ====================
        Create a URI for a relationship object, based on the given Elements relationship object
    -->
    <xsl:function name="svfn:relationshipURI" as="xs:string">
        <xsl:param name="relationship" />
        <xsl:param name="type" />

        <xsl:value-of select="svfn:makeURI($type,$relationship/@id)" />
    </xsl:function>

    <!--
        svfn:departmentName
        ===================
        Get the department from an api:address or api:institution object
    -->
    <xsl:function name="svfn:departmentName" as="xs:string">
        <xsl:param name="address" />

        <xsl:value-of select="$address/api:line[@type='suborganisation']" />
    </xsl:function>

    <!--
        svfn:departmentURI
        ==================
        Create a URI for a department from an api:address or api:institution object
    -->
    <xsl:function name="svfn:departmentURI" as="xs:string">
        <xsl:param name="address" />

        <xsl:variable name="orgName" select="svfn:institutionName($address)" />
        <xsl:variable name="deptName" select="svfn:departmentName($address)" />
        <xsl:choose>
            <xsl:when test="not($deptName='') and not($orgName='')"><xsl:value-of select="svfn:makeURI('dept-',concat(fn:substring($deptName,1,100),'-',fn:substring($orgName,1,50)))" /></xsl:when>
            <xsl:when test="not($deptName='')"><xsl:value-of select="svfn:makeURI('dept-',fn:substring($deptName,1,150))" /></xsl:when>
            <xsl:otherwise><xsl:text /></xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        svfn:institutionName
        ===================
        Get the institution from an api:address or api:institution object
    -->
    <xsl:function name="svfn:institutionName" as="xs:string">
        <xsl:param name="address" />

        <xsl:value-of select="$address/api:line[@type='organisation']" />
    </xsl:function>

    <!--
        svfn:institutionURI
        ====================
        Create a URI for an institution from an api:address or api:institution object
    -->
    <xsl:function name="svfn:institutionURI" as="xs:string">
        <xsl:param name="address" />

        <xsl:variable name="orgName" select="svfn:institutionName($address)" />
        <xsl:choose>
            <xsl:when test="not($orgName='')"><xsl:value-of select="svfn:makeURI('institution-',$orgName)" /></xsl:when>
            <xsl:otherwise><xsl:text /></xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        svfn:institutionURI
        ====================
        Create a URI for an institution from an api:address or api:institution object
    -->
    <xsl:function name="svfn:organisationObjects">
        <xsl:param name="address" />

        <xsl:variable name="deptURI" select="svfn:departmentURI($address)"/>
        <xsl:variable name="instURI" select="svfn:institutionURI($address)"/>

        <xsl:if test="$includeDept='true'">
            <xsl:if test="$address/api:line[@type='suborganisation']">
                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$deptURI" />
                    <xsl:with-param name="rdfNodes">
                        <!-- TODO Implement dictionary to determine department type -->
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#AcademicDepartment"/>
                        <rdfs:label><xsl:value-of select="svfn:departmentName($address)" /></rdfs:label>
                        <xsl:if test="$address/api:line[@type='organisation']">
                            <obo:BFO_0000050 rdf:resource="{$instURI}" />
                        </xsl:if>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:if>
        </xsl:if>

        <xsl:if test="$address/api:line[@type='organisation']">
            <xsl:variable name="insName" select="svfn:institutionName($address)" />
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$instURI" />
                <xsl:with-param name="rdfNodes">
                    <rdf:type rdf:resource="{svfn:getOrganizationType($insName,'http://vivoweb.org/ontology/core#University')}" />
                    <rdfs:label><xsl:value-of select="$insName" /></rdfs:label>
                    <xsl:if test="$address/api:line[@type='suborganisation'] and $includeDept='true'">
                        <obo:BFO_0000051 rdf:resource="{$deptURI}" />
                    </xsl:if>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:function>

    <xsl:function name="svfn:organisationObjectsMainURI">
        <xsl:param name="orgObjects" />
        <xsl:choose>
            <xsl:when test="$orgObjects/*"><xsl:value-of select="$orgObjects[1]/@rdf:about" /></xsl:when>
            <xsl:otherwise><xsl:text /></xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        svfn:renderDateObject
        =====================
        Generate a VIVO date object for the supplied date object
    -->
    <xsl:function name="svfn:renderDateObject">
        <xsl:param name="object" />
        <xsl:param name="dateObjectURI" as="xs:string" />
        <xsl:param name="date" />

        <!-- only output if the date exists -->
        <xsl:if test="$date">
            <xsl:call-template name="render_rdf_object">
                <xsl:with-param name="objectURI" select="$dateObjectURI" />
                <!-- generate property statements - concat generated statements with fixed statements, as they are only required if the generated statements are output successfully -->
                <xsl:with-param name="rdfNodes">
                    <xsl:call-template name="_concat_nodes_if">
                        <xsl:with-param name="nodesRequired">
                            <xsl:apply-templates select="$date" />
                        </xsl:with-param>
                        <xsl:with-param name="nodesToAdd">
                            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:function>

    <!--
        svfn:datePrecision
        ==================
        Determine how precise the Elements date object is
    -->
    <xsl:function name="svfn:datePrecision" as="xs:string">
        <xsl:param name="date" />

        <xsl:choose>
            <xsl:when test="string($date/api:day) and string($date/api:month) and string($date/api:year)">yearMonthDayPrecision</xsl:when>
            <xsl:when test="string($date/api:month) and string($date/api:year)">yearMonthPrecision</xsl:when>
            <xsl:when test="string($date/api:year)">yearPrecision</xsl:when>
            <xsl:otherwise>none</xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        svfn:dateYear
        =============
        Return the formatted year
    -->
    <xsl:function name="svfn:dateYear">
        <xsl:param name="date" />

        <xsl:value-of select="$date/api:year" />
    </xsl:function>

    <!--
        svfn:dateMonth
        ==============
        Return the formatted month
    -->
    <xsl:function name="svfn:dateMonth">
        <xsl:param name="date" />

        <xsl:choose>
            <xsl:when test="string-length($date/api:month)=1">0<xsl:value-of select="$date/api:month" /></xsl:when>
            <xsl:when test="string-length($date/api:month)=2"><xsl:value-of select="$date/api:month" /></xsl:when>
            <xsl:otherwise>01</xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        svfn:dateDay
        ============
        Return the formatted day
    -->
    <xsl:function name="svfn:dateDay">
        <xsl:param name="date" />

        <xsl:choose>
            <xsl:when test="string-length($date/api:day)=1">0<xsl:value-of select="$date/api:day" /></xsl:when>
            <xsl:when test="string-length($date/api:day)=2"><xsl:value-of select="$date/api:day" /></xsl:when>
            <xsl:otherwise>01</xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        svfn:stringToURI
        ================
        Convert a string into a URI-friendly form (for identifiers)
    -->
    <xsl:function name="svfn:stringToURI">
        <xsl:param name="string" as="xs:string" />

        <xsl:value-of select="fn:encode-for-uri(fn:replace(fn:replace(fn:lower-case(fn:normalize-space($string)), '\s', '-'), '[^a-z0-9\-]', ''))" />
    </xsl:function>

    <!--
        svfn:fullObject
        ===============
        Load the XML for the full object, given an Elements object reference
    -->
    <xsl:function name="svfn:fullObject">
        <xsl:param name="object" />
        <xsl:variable name="filename" select="concat($recordDir,$object/@category,'/',$object/@id)" />

        <xsl:choose>
            <xsl:when test="fn:doc-available($filename)">
                <xsl:copy-of select="document($filename)//api:object" />
            </xsl:when>
            <xsl:when test="fn:doc-available(concat($filename,'.xml'))">
                <xsl:copy-of select="document(concat($filename,'.xml'))//api:object" />
            </xsl:when>
        </xsl:choose>
    </xsl:function>

    <!--
        svfn:renderPropertyFromFieldOrFirst
        ===================================
        Function to retrieve the specified Elements field (fieldName) from the most preferred record,
        and render it as the RDF property (propertyName)
        If the field is not present in any preferred records, output the value in the first record present.
    -->
    <xsl:function name="svfn:renderPropertyFromFieldOrFirst">
        <xsl:param name="object" />
        <xsl:param name="propertyName" as="xs:string" />
        <xsl:param name="fieldName" as="xs:string" />

        <xsl:copy-of select="svfn:_renderPropertyFromField($object, $propertyName, $fieldName, svfn:getRecordFieldOrFirst($object, $fieldName))" />
    </xsl:function>

    <!--
        svfn:renderPropertyFromField
        ============================
        Function to retrieve the specified Elements field (fieldName) from the most preferred record,
        and render it as the RDF property (propertyName)
    -->
    <xsl:function name="svfn:renderPropertyFromField">
        <xsl:param name="object" />
        <xsl:param name="propertyName" as="xs:string" />
        <xsl:param name="fieldName" as="xs:string" />

        <xsl:copy-of select="svfn:_renderPropertyFromField($object, $propertyName, $fieldName, svfn:getRecordField($object, $fieldName))" />
    </xsl:function>

    <!--
        svfn:renderPropertyFromField
        ============================
        Function to retrieve the specified Elements field (fieldName) from the most preferred record,
        and render it as the RDF property (propertyName)
        Overloaded method that takes a comma delimited list of record names to use as the preference order (override the central configuration)
    -->
    <xsl:function name="svfn:renderPropertyFromField">
        <xsl:param name="object" />
        <xsl:param name="propertyName" as="xs:string" />
        <xsl:param name="fieldName" as="xs:string" />
        <xsl:param name="records" as="xs:string" />

        <xsl:copy-of select="svfn:_renderPropertyFromField($object, $propertyName, $fieldName, svfn:getRecordField($object, $fieldName, $records))" />
    </xsl:function>

    <!--
        svfn:getRecordFieldOrFirst
        ==========================
        Function to retrieve the specified Elements field (fieldName) from the most preferred record,
        If the field is not present in any preferred records, output the value in the first record present.
    -->
    <xsl:function name="svfn:getRecordFieldOrFirst">
        <xsl:param name="object" />
        <xsl:param name="fieldName" as="xs:string" />

        <xsl:choose>
            <xsl:when test="$record-precedences[@for=$object/@category]">
                <xsl:variable name="record-precedence" select="$record-precedences[@for=$object/@category]/config:record-precedence" />
                <xsl:variable name="record-precedence-select-by" select="$record-precedences[@for=$object/@category]/@select-by" />

                <xsl:copy-of select="svfn:_getRecordField($object, $fieldName, $record-precedence, $record-precedence-select-by, 1, true())" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="record-precedence" select="$record-precedences[@for='default']/config:record-precedence" />
                <xsl:variable name="record-precedence-select-by" select="$record-precedences[@for='default']/@select-by" />

                <xsl:copy-of select="svfn:_getRecordField($object, $fieldName, $record-precedence, $record-precedence-select-by, 1, true())" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        svfn:getRecordField
        ================
        Function to retrieve the specified Elements field (fieldName) from the most preferred record,
    -->
    <xsl:function name="svfn:getRecordField">
        <xsl:param name="object" />
        <xsl:param name="fieldName" as="xs:string" />

        <xsl:choose>
            <xsl:when test="$record-precedences[@for=$object/@category]">
                <xsl:variable name="record-precedence" select="$record-precedences[@for=$object/@category]/config:record-precedence" />
                <xsl:variable name="record-precedence-select-by" select="$record-precedences[@for=$object/@category]/@select-by" />

                <xsl:copy-of select="svfn:_getRecordField($object, $fieldName, $record-precedence, $record-precedence-select-by, 1, false())" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="record-precedence" select="$record-precedences[@for='default']/config:record-precedence" />
                <xsl:variable name="record-precedence-select-by" select="$record-precedences[@for='default']/@select-by" />

                <xsl:copy-of select="svfn:_getRecordField($object, $fieldName, $record-precedence, $record-precedence-select-by, 1, false())" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        svfn:getRecordField
        ================
        Function to retrieve the specified Elements field (fieldName) from the most preferred record,
        Overloaded method that takes a comma delimited list of record names to use as the preference order (override the central configuration)
    -->
    <xsl:function name="svfn:getRecordField">
        <xsl:param name="object" />
        <xsl:param name="fieldName" as="xs:string" />
        <xsl:param name="records" as="xs:string" />

        <xsl:choose>
            <xsl:when test="$record-precedences[@for=$object/@category]">
                <xsl:variable name="record-precedence-select-by" select="$record-precedences[@for=$object/@category]/@select-by" />

                <xsl:copy-of select="svfn:_getRecordField($object, $fieldName, fn:tokenize($records,','), $record-precedence-select-by, 1, false())" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="record-precedence-select-by" select="$record-precedences[@for='default']/@select-by" />

                <xsl:copy-of select="svfn:_getRecordField($object, $fieldName, fn:tokenize($records,','), $record-precedence-select-by, 1, false())" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        svfn:getRecordField
        ================
        Function to retrieve the specified Elements field (fieldName) from the most preferred record,
        Overloaded method that takes a comma delimited list of record names to use as the preference order (override the central configuration)
        and select-by is 'field' (find first occurrence of the field) or 'record' (use first preferred record, even if the field is not present)
    -->
    <xsl:function name="svfn:getRecordField">
        <xsl:param name="object" />
        <xsl:param name="fieldName" as="xs:string" />
        <xsl:param name="records" as="xs:string" />
        <xsl:param name="select-by" as="xs:string" />

        <xsl:copy-of select="svfn:_getRecordField($object, $fieldName, fn:tokenize($records,','), $select-by, 1, false())" />
    </xsl:function>

    <!--
        Internal XSLT Functions (should not be called from outside this file)
    -->

    <xsl:function name="svfn:_renderPropertyFromField">
        <xsl:param name="object" />
        <xsl:param name="propertyName" as="xs:string" />
        <xsl:param name="fieldName" as="xs:string" />
        <xsl:param name="fieldNode" />

        <xsl:apply-templates select="$fieldNode" mode="renderForProperty">
            <xsl:with-param name="propertyName" select="$propertyName" />
            <xsl:with-param name="fieldName" select="$fieldName" />
        </xsl:apply-templates>
    </xsl:function>

    <xsl:function name="svfn:_getRecordField">
        <xsl:param name="object" />
        <xsl:param name="fieldName" as="xs:string" />
        <xsl:param name="records" />
        <xsl:param name="select-by" />
        <xsl:param name="position" as="xs:integer" />
        <xsl:param name="useDefault" as="xs:boolean" />

        <xsl:choose>
            <xsl:when test="$records[$position]">
                <xsl:choose>
                    <xsl:when test="$select-by='field'">
                        <xsl:choose>
                            <!-- Don't use fields from Scopus that are restricted -->
                            <xsl:when test="$records[$position]='scopus' and ($fieldName='abstract')">
                                <xsl:copy-of select="svfn:_getRecordField($object,$fieldName,$records,$select-by,$position+1,$useDefault)" />
                            </xsl:when>
                            <xsl:when test="$object/api:records/api:record[@source-name=$records[$position]][1]/api:native/api:field[@name=$fieldName]">
                                <xsl:copy-of select="$object/api:records/api:record[@source-name=$records[$position]][1]/api:native/api:field[@name=$fieldName]" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:copy-of select="svfn:_getRecordField($object,$fieldName,$records,$select-by,$position+1,$useDefault)" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <!-- Don't use fields from Scopus that are restricted -->
                            <xsl:when test="$records[$position]='scopus' and ($fieldName='abstract')">
                                <xsl:copy-of select="svfn:_getRecordField($object,$fieldName,$records,$select-by,$position+1,$useDefault)" />
                            </xsl:when>
                            <xsl:when test="$object/api:records/api:record[@source-name=$records[$position]][1]/api:native">
                                <xsl:copy-of select="$object/api:records/api:record[@source-name=$records[$position]][1]/api:native/api:field[@name=$fieldName]" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:copy-of select="svfn:_getRecordField($object,$fieldName,$records,$select-by,$position+1,$useDefault)" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="$useDefault">
                <xsl:copy-of select="$object/api:records/api:record[1]/api:native/api:field[@name=$fieldName]" />
            </xsl:when>
        </xsl:choose>
    </xsl:function>

    <!--
    -->
    <xsl:function name="svfn:renderLinksAndExternalPeople">
        <xsl:param name="people" />
        <xsl:param name="linkedId" as="xs:string" />
        <xsl:param name="linkedUri" as="xs:string" />

        <xsl:variable name="linkType">
            <xsl:choose>
                <xsl:when test="$people/@name='authors'">authorship</xsl:when>
                <xsl:when test="$people/@name='associated-authors'"></xsl:when>
                <xsl:when test="$people/@name='editors'">editorship</xsl:when>
            </xsl:choose>
        </xsl:variable>

        <xsl:if test="not($linkType='') and $people/api:people/api:person/api:links[api:link/@type='elements/user']/*">
            <xsl:for-each select="$people/api:people/api:person">
                <xsl:choose>
                    <xsl:when test="api:links/api:link/@type='elements/user'">
                        <xsl:variable name="contextURI" select="svfn:objectToObjectURI($linkType,$linkedId,api:links/api:link[@type='elements/user']/@id)" />

                        <!-- Add rank to context object -->
                        <xsl:call-template name="render_rdf_object">
                            <xsl:with-param name="objectURI" select="$contextURI" />
                            <xsl:with-param name="rdfNodes">
                                <vivo:rank rdf:datatype="http://www.w3.org/2001/XMLSchema#int"><xsl:value-of select="position()" /></vivo:rank>
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="$externalPersons='true'">
                        <xsl:variable name="personId">
                            <xsl:choose>
                                <xsl:when test="api:initials and not(api:initials='')">
                                    <xsl:value-of select="concat(fn:lower-case(fn:normalize-space(api:last-name)),'-',fn:lower-case(fn:normalize-space(api:initials)))" />
                                </xsl:when>
                                <xsl:when test="api:first-names and not(api:first-names='')">
                                    <xsl:value-of select="concat(fn:lower-case(fn:normalize-space(api:last-name)),'-',fn:substring(fn:lower-case(fn:normalize-space(api:first-names)),1,1))" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="fn:lower-case(fn:normalize-space(api:last-name))" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:variable name="contextURI" select="svfn:objectToObjectURI($linkType,$linkedId,$personId)" />

                        <!-- Create context object -->
                        <xsl:call-template name="render_rdf_object">
                            <xsl:with-param name="objectURI" select="$contextURI" />
                            <xsl:with-param name="rdfNodes">
                                <xsl:choose>
                                    <xsl:when test="$linkType='authorship'"><rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship"/></xsl:when>
                                    <xsl:when test="$linkType='editorship'"><rdf:type rdf:resource="http://vivoweb.org/ontology/core#Editorship"/></xsl:when>
                                    <xsl:otherwise><rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship"/></xsl:otherwise>
                                </xsl:choose>
                                <vivo:relates rdf:resource="{svfn:makeURI('person-',$personId)}"/>
                                <vivo:relates rdf:resource="{$linkedUri}"/>
                                <vivo:rank rdf:datatype="http://www.w3.org/2001/XMLSchema#int"><xsl:value-of select="position()" /></vivo:rank>
                            </xsl:with-param>
                        </xsl:call-template>

                        <!-- Create person object -->
                        <xsl:call-template name="render_rdf_object">
                            <xsl:with-param name="objectURI" select="svfn:makeURI('person-',$personId)" />
                            <xsl:with-param name="rdfNodes">
                                <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person"/>
                                <rdfs:label><xsl:value-of select="$externalPersonLabelPrefix" />
                                    <xsl:choose>
                                        <xsl:when test="api:last-name and api:first-names">
                                            <xsl:value-of select="api:last-name" />, <xsl:value-of select="api:first-names" />
                                        </xsl:when>
                                        <xsl:when test="api:last-name and api:initials">
                                            <xsl:value-of select="api:last-name" />, <xsl:value-of select="api:initials" />
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="api:last-name" />
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:value-of select="$externalPersonLabelSuffix" /></rdfs:label>
                                <obo:ARG_2000028 rdf:resource="{svfn:makeURI('personvcard-',$personId)}"/>
                                <vivo:relatedBy rdf:resource="{$contextURI}" />
                            </xsl:with-param>
                        </xsl:call-template>

                        <!-- Create person vcard object -->
                        <xsl:call-template name="render_rdf_object">
                            <xsl:with-param name="objectURI" select="svfn:makeURI('personvcard-',$personId)" />
                            <xsl:with-param name="rdfNodes">
                                <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Individual"/>
                                <obo:ARG_2000029 rdf:resource="{svfn:makeURI('person-',$personId)}"/>
                                <vcard:hasName rdf:resource="{svfn:makeURI('personvcardname-',$personId)}"/>
                            </xsl:with-param>
                        </xsl:call-template>

                        <!-- Create person vcard name object -->
                        <xsl:call-template name="render_rdf_object">
                            <xsl:with-param name="objectURI" select="svfn:makeURI('personvcardname-',$personId)" />
                            <xsl:with-param name="rdfNodes">
                                <rdf:type rdf:resource="http://www.w3.org/2006/vcard/ns#Name"/>
                                <xsl:choose>
                                    <xsl:when test="api:first-names">
                                        <vcard:givenName><xsl:value-of select="api:first-names" /></vcard:givenName>
                                    </xsl:when>
                                    <xsl:when test="api:initials">
                                        <vcard:givenName><xsl:value-of select="api:initials" /></vcard:givenName>
                                    </xsl:when>
                                </xsl:choose>
                                <vcard:familyName><xsl:value-of select="api:last-name" /></vcard:familyName>
                            </xsl:with-param>
                        </xsl:call-template>

                        <!-- Add publication relationship -->
                        <xsl:call-template name="render_rdf_object">
                            <xsl:with-param name="objectURI" select="$linkedUri" />
                            <xsl:with-param name="rdfNodes">
                                <vivo:relatedBy rdf:resource="{$contextURI}" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                </xsl:choose>
            </xsl:for-each>
        </xsl:if>
    </xsl:function>

    <xsl:function name="svfn:renderControlledSubjectLinks">
        <xsl:param name="allLabels" />
        <xsl:param name="scheme" as="xs:string" />
        <xsl:param name="schemeDefinedBy" as="xs:string" />

        <xsl:if test="not($schemeDefinedBy='')">
            <xsl:for-each select="fn:distinct-values($allLabels/api:keywords/api:keyword[@scheme=$scheme])">
                <vivo:hasSubjectArea rdf:resource="{svfn:makeURI(concat('vocab-',$scheme,'-'),.)}" />
            </xsl:for-each>
        </xsl:if>
    </xsl:function>

    <xsl:function name="svfn:renderControlledSubjectObjects">
        <xsl:param name="allLabels" />
        <xsl:param name="publicationUri" as="xs:string" />
        <xsl:param name="publicationVenueUri" as="xs:string" />
        <xsl:param name="scheme" as="xs:string" />
        <xsl:param name="schemeDefinedBy" as="xs:string" />

        <xsl:if test="not($schemeDefinedBy='')">
            <xsl:for-each select="fn:distinct-values($allLabels/api:keywords/api:keyword[@scheme=$scheme])">
                <xsl:variable name="definitionUri" select="svfn:makeURI(concat('vocab-',$scheme,'-'),.)" />

                <xsl:call-template name="render_rdf_object">
                    <xsl:with-param name="objectURI" select="$definitionUri" />
                    <xsl:with-param name="rdfNodes">
                        <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept" />
                        <rdfs:label><xsl:value-of select="." /></rdfs:label>
                        <rdfs:isDefinedBy rdf:resource="{$schemeDefinedBy}" />
                        <xsl:if test="not($publicationUri='')">
                            <vivo:subjectAreaOf rdf:resource="{$publicationUri}" />
                        </xsl:if>
                        <xsl:if test="not($publicationVenueUri='')">
                            <vivo:subjectAreaOf rdf:resource="{$publicationVenueUri}" />
                        </xsl:if>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>
    </xsl:function>

    <!-- ======================================
         Template Library
         ======================================- -->

    <!-- _render_rdf_document -->
    <xsl:template name="render_rdf_document">
        <xsl:param name="rdfNodes" />

        <xsl:if test="$rdfNodes/*">
            <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                     xmlns:bibo="http://purl.org/ontology/bibo/"
                     xmlns:dc="http://purl.org/dc/elements/1.1/"
                     xmlns:foaf="http://xmlns.com/foaf/0.1/"
                     xmlns:obo="http://purl.obolibrary.org/obo/"
                     xmlns:owl="http://www.w3.org/2002/07/owl#"
                     xmlns:owlPlus="http://www.w3.org/2006/12/owl2-xml#"
                     xmlns:score="http://vivoweb.org/ontology/score#"
                     xmlns:skos="http://www.w3.org/2008/05/skos#"
                     xmlns:swvocab="http://www.w3.org/2003/06/sw-vocab-status/ns#"
                     xmlns:symp="http://www.symplectic.co.uk/ontology/elements/"
                     xmlns:ufVivo="http://vivo.ufl.edu/ontology/vivo-ufl/"
                     xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                     xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
                     xmlns:vivo="http://vivoweb.org/ontology/core#"
                     xmlns:vocab="http://purl.org/vocab/vann/"
                    >
                <xsl:copy-of select="$rdfNodes" />
            </rdf:RDF>
        </xsl:if>
    </xsl:template>

    <!-- _render_rdf_object -->
    <xsl:template name="render_rdf_object">
        <xsl:param name="rdfNodes" />
        <xsl:param name="objectURI" />

        <xsl:if test="$rdfNodes/*">
            <rdf:Description rdf:about="{$objectURI}">
                <xsl:copy-of select="$rdfNodes" />
                <xsl:if test="$harvestedBy!=''">
                    <ufVivo:harvestedBy><xsl:value-of select="$harvestedBy" /></ufVivo:harvestedBy>
                </xsl:if>
            </rdf:Description>
        </xsl:if>
    </xsl:template>

    <xsl:template name="render_empty_rdf">
        <rdf:RDF></rdf:RDF>
    </xsl:template>

    <xsl:template name="_concat_nodes_if">
        <xsl:param name="nodesRequired" />
        <xsl:param name="nodesToAdd" />

        <xsl:if test="$nodesRequired/*">
            <xsl:copy-of select="$nodesRequired" />
            <xsl:copy-of select="$nodesToAdd" />
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
