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
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'
    xmlns:symp='http://www.symplectic.co.uk/ontology/elements/'>

    <xsl:import href="symplectic-to-vivo.datamap.config.xsl" />

    <xsl:import href="symplectic-to-vivo.datamap.users.xsl" />
    <xsl:import href="symplectic-to-vivo.datamap.publications.xsl" />
    <xsl:import href="symplectic-to-vivo.datamap.relationships.xsl" />
    <xsl:import href="symplectic-to-vivo.datamap.activities.xsl" />

    <!-- This will create indenting in xml readers -->
	<xsl:output method="xml" encoding="UTF-8" indent="yes" />

	<xsl:template match="text()"></xsl:template>
	<xsl:template match="text()" mode="dateTimeValue"></xsl:template>
    <xsl:template match="text()" mode="objectReferences"></xsl:template>
    <xsl:template match="text()" mode="objectEntries"></xsl:template>
    <xsl:template match="text()" mode="type23"></xsl:template>
    <xsl:template match="text()" mode="professionalActivity"></xsl:template>
    <xsl:template match="text()" mode="professionalActivityURI"></xsl:template>
    <xsl:template match="text()" mode="professionalActivityRelationship"></xsl:template>

    <!--  user metadata  -->
	<xsl:template match="api:organisation-defined-data[@field-name='UoA']">
		<svo:UoA>
			<xsl:value-of select="." />
		</svo:UoA>
	</xsl:template>

	<xsl:template match="api:organisation-defined-data[@field-name='Birth date']">
		<svo:BirthDate>
			<xsl:value-of select="." />
		</svo:BirthDate>
	</xsl:template>

	<xsl:template
		match="api:organisation-defined-data[@field-name='Staff category (RAE)']">
		<svo:StaffCategory>
			<xsl:value-of select="." />
		</svo:StaffCategory>
	</xsl:template>

    <xsl:template
        match="api:organisation-defined-data[@field-name='Telephone Number']">
        <core:phoneNumber>
            <xsl:value-of select="." />
        </core:phoneNumber>
    </xsl:template>


    <!-- core publication metadata -->
	<xsl:template match="api:field[@name='title']">
		<rdfs:label>
			<xsl:value-of select="api:text" />
		</rdfs:label>
		<core:Title>
			<xsl:value-of select="api:text" />
		</core:Title>
	</xsl:template>
	<xsl:template match="api:field[@name='abstract']">
		<bibo:abstract>
			<xsl:value-of select="api:text" />
		</bibo:abstract>
	</xsl:template>
	<xsl:template match="api:field[@name='edition']">
		<bibo:edition>
			<xsl:value-of select="api:text" />
		</bibo:edition>
	</xsl:template>
	<xsl:template match="api:field[@name='volume']">
		<bibo:volume>
			<xsl:value-of select="api:text" />
		</bibo:volume>
	</xsl:template>
	<!--  Need a home for this
	 -->
	<xsl:template match="api:field[@name='pagination']">
		<xsl:choose>
		   <xsl:when test="string(api:pagination/api:begin-page) and string(api:pagination/api:end-page)">
		    <bibo:pageStart><xsl:value-of select="api:pagination/api:begin-page" /></bibo:pageStart>
		    <bibo:pageEnd><xsl:value-of select="api:pagination/api:end-page" /></bibo:pageEnd>
		   </xsl:when>
		   <xsl:when test="string(api:pagination/api:begin-page)">
            <bibo:pageStart><xsl:value-of select="api:pagination/api:begin-page" /></bibo:pageStart>
		   </xsl:when>
		   <xsl:when test="string(api:pagination/api:end-page)">
            <bibo:pageEnd><xsl:value-of select="api:pagination/api:end-page" /></bibo:pageEnd>
		   </xsl:when>
		</xsl:choose>
	</xsl:template>
	<!-- 
	<xsl:template match="api:begin-page">
		<svo:begin-page>
			<xsl:value-of select="." />
		</svo:begin-page>
	</xsl:template>
	<xsl:template match="api:end-page">
		<svo:end-page>
			<xsl:value-of select="." />
		</svo:end-page>
	</xsl:template>
	 -->
	<xsl:template match="api:field[@name='publisher']">
	<!--  TODO: convert this to core:publisher link t foaf:Organization -->
		<svo:publisher>
			<xsl:value-of select="api:text" />
		</svo:publisher>
	</xsl:template>
	<xsl:template match="api:field[@name='place-of-publication']">
		<bibo:placeOfPublication>
			<xsl:value-of select="api:text" />
		</bibo:placeOfPublication>
	</xsl:template>
    <xsl:template match="api:field[@name='authors']">
        <!-- Can't convert these to svo:authors, as they are labels, not people objects -->
        <symp:authors>
            <xsl:apply-templates select="api:people" />
        </symp:authors>
    </xsl:template>
    <xsl:template match="api:field[@name='editors']">
        <!-- Can't convert these to svo:editors, as they are labels, not people objects -->
        <symp:editors>
            <xsl:apply-templates select="api:people" />
        </symp:editors>
    </xsl:template>
    <xsl:template match="api:people/api:person">
        <xsl:if test="preceding-sibling::*">
            <xsl:text>, </xsl:text>
        </xsl:if>
        <xsl:value-of select="api:last-name" /><xsl:text> </xsl:text><xsl:value-of select="api:initials" />
    </xsl:template>


    <xsl:template match="api:date" mode="dateTimeValue">
		<xsl:variable name="datePrecision">
			<xsl:choose>
				<xsl:when
					test="string(api:day) and string(api:month) and string(api:year)">yearMonthDayPrecision</xsl:when>
				<xsl:when test="string(api:month) and string(api:year)">yearMonthPrecision</xsl:when>
				<xsl:when test="string(api:year)">yearPrecision</xsl:when>
				<xsl:otherwise>none</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="month">
			<xsl:choose>
				<xsl:when
					test="string-length(api:month)=1">0<xsl:value-of select="api:month" /></xsl:when>
				<xsl:otherwise><xsl:value-of select="api:month" /></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
        <xsl:variable name="day">
            <xsl:choose>
                <xsl:when
                    test="string-length(api:day)=1">0<xsl:value-of select="api:day" /></xsl:when>
                <xsl:otherwise><xsl:value-of select="api:day" /></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
		
		<xsl:variable name="aboutURI">
			<xsl:choose>
			<xsl:when test="$datePrecision='yearMonthDayPrecision'" >pub/daymonthyear<xsl:value-of select="api:year" /><xsl:value-of select="$month" /><xsl:value-of select="$day" /></xsl:when>
			<xsl:when test="$datePrecision='yearMonthPrecision'" >pub/monthyear<xsl:value-of select="api:year" /><xsl:value-of select="$month" /></xsl:when>
			<xsl:when test="$datePrecision='yearPrecision'" >pub/year<xsl:value-of select="api:year" /></xsl:when>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:if test="$datePrecision!='none'">
			<core:dateTimePrecision
				rdf:resource="http://vivoweb.org/ontology/core#{$datePrecision}" />
			<core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime">
				<xsl:choose>
					<xsl:when test="$datePrecision='yearMonthDayPrecision'" ><xsl:value-of select="api:year" />-<xsl:value-of select="$month" />-<xsl:value-of select="$day" />T00:00:00Z</xsl:when>
					<xsl:when test="$datePrecision='yearMonthPrecision'" ><xsl:value-of select="api:year" />-<xsl:value-of select="$month" />-01T00:00:00Z</xsl:when>
					<xsl:when test="$datePrecision='yearPrecision'" ><xsl:value-of select="api:year" />-01-01T00:00:00Z</xsl:when>
				</xsl:choose>
			</core:dateTime>
		</xsl:if>		
	</xsl:template>
	
	
	
	<xsl:template match="api:field[@name='isbn-10']">
		<bibo:isbn-10>
			<xsl:value-of select="api:text" />
		</bibo:isbn-10>
	</xsl:template>
	<xsl:template match="api:field[@name='isbn-13']">
		<bibo:isbn-13>
			<xsl:value-of select="api:text" />
		</bibo:isbn-13>
	</xsl:template>
	<xsl:template match="api:field[@name='doi']">
		<bibo:doi>
			<xsl:text>http://dx.doi.org/</xsl:text><xsl:value-of select="api:text" />
		</bibo:doi>
	</xsl:template>
	<xsl:template match="api:field[@name='medium']">
		<bibo:status>
			<xsl:value-of select="api:text" />
		</bibo:status>
	</xsl:template>
	<xsl:template match="api:field[@name='issn']">
		<bibo:ISSN>
			<xsl:value-of select="api:text" />
		</bibo:ISSN>
	</xsl:template>
	<xsl:template match="api:field[@name='notes']">
		<svo:notes>
			<xsl:value-of select="api:text" />
		</svo:notes>
	</xsl:template>

    <xsl:template match="api:field[@name='eISSN']">
        <bibo:eissn>
            <xsl:value-of select="api:text" />
        </bibo:eissn>
    </xsl:template>

    <xsl:template match="api:field[@name='book-title']">
        <svo:book-title>
            <xsl:value-of select="api:text" />
        </svo:book-title>
    </xsl:template>

    <xsl:template match="api:field[@name='chapter-number']">
        <svo:chapter-number>
            <xsl:value-of select="api:text" />
        </svo:chapter-number>
    </xsl:template>



    <xsl:template match="api:field[@name='country']">
        <svo:country>
            <xsl:value-of select="api:text" />
        </svo:country>
    </xsl:template>
    <xsl:template match="api:field[@name='confidential-report']">
        <svo:confidential-report>
            <xsl:value-of select="api:text" />
        </svo:confidential-report>
    </xsl:template>
    
    <xsl:template match="api:field[@name='confidential']">
        <svo:confidential>
            <xsl:value-of select="api:text" />
        </svo:confidential>
    </xsl:template>
    
 
    <xsl:template match="api:field[@name='event-title']">
        <svo:event-title>
            <xsl:value-of select="api:text" />
        </svo:event-title>
    </xsl:template>
    <xsl:template match="api:field[@name='event-type']">
        <svo:event-type>
            <xsl:value-of select="api:text" />
        </svo:event-type>
    </xsl:template>
 
    <xsl:template match="api:field[@name='issue']">
        <bibo:issue>
            <xsl:value-of select="api:text" />
        </bibo:issue>
    </xsl:template>
    <xsl:template match="api:field[@name='identification-number']">
        <bibo:number>
            <xsl:value-of select="api:text" />
        </bibo:number>
    </xsl:template>
    <xsl:template match="api:field[@name='language']">
        <svo:language>
            <xsl:value-of select="api:text" />
        </svo:language>
    </xsl:template>
    <xsl:template match="api:field[@name='location']">
        <svo:location>
            <xsl:value-of select="api:text" />
        </svo:location>
    </xsl:template>
    <xsl:template match="api:field[@name='location-of-work']">
        <svo:location-of-work>
            <xsl:value-of select="api:text" />
        </svo:location-of-work>
    </xsl:template>
    <xsl:template match="api:field[@name='c-monograph-type']">
        <svo:monograph-type>
            <xsl:value-of select="api:text" />
        </svo:monograph-type>
    </xsl:template>
    <xsl:template match="api:field[@name='name-of-conference']">
        <svo:name-of-conference>
            <xsl:value-of select="api:text" />
        </svo:name-of-conference>
    </xsl:template>
    <xsl:template match="api:field[@name='number-of-chapters']">
        <svo:number-of-chapters>
            <xsl:value-of select="api:text" />
        </svo:number-of-chapters>
    </xsl:template>
    <xsl:template match="api:field[@name='number-of-pieces']">
        <svo:number-of-pieces>
            <xsl:value-of select="api:text" />
        </svo:number-of-pieces>
    </xsl:template>
    <xsl:template match="api:field[@name='pii']">
        <svo:pii>
            <xsl:value-of select="api:text" />
        </svo:pii>
    </xsl:template>
    <xsl:template match="api:field[@name='patent-number']">
        <svo:patent-number>
            <xsl:value-of select="api:text" />
        </svo:patent-number>
    </xsl:template>
    <xsl:template match="api:field[@name='patent-status']">
        <svo:patent-status>
            <xsl:value-of select="api:text" />
        </svo:patent-status>
    </xsl:template>
    <xsl:template match="api:field[@name='presentation-type']">
        <presentation-type>
            <xsl:value-of select="api:text" />
        </presentation-type>
    </xsl:template>
    <xsl:template match="api:field[@name='producers']">
        <svo:producers>
            <xsl:value-of select="api:text" />
        </svo:producers>
    </xsl:template>    
    <xsl:template match="api:field[@name='published-proceedings']">
        <svo:published-proceedings>
            <xsl:value-of select="api:text" />
        </svo:published-proceedings>
    </xsl:template>
    <xsl:template match="api:field[@name='refereed']">
<!--  TODO:                <bibo:status rdf:resource="http://purl.org/ontology/bibo/peerReviewed"/> -->
        <svo:refereed>
            <xsl:value-of select="api:text" />
        </svo:refereed>
    </xsl:template>
    <xsl:template match="api:field[@name='references']">
<!--  TODO: bibo:cites ?, may be a reference rather than a property -->
        <svo:references>
            <xsl:value-of select="api:text" />
        </svo:references>
    </xsl:template>
    <xsl:template match="api:field[@name='report-number']">
        <bibo:number>
            <xsl:value-of select="api:text" />
        </bibo:number>
    </xsl:template>
    <xsl:template match="api:field[@name='report-title']">
        <svo:report-title>
            <xsl:value-of select="api:text" />
        </svo:report-title>
    </xsl:template>
    <xsl:template match="api:field[@name='running-time']">
        <svo:running-time>
            <xsl:value-of select="api:text" />
        </svo:running-time>
    </xsl:template>
    <xsl:template match="api:field[@name='series-directors']">
        <svo:series-directors>
            <xsl:value-of select="api:text" />
        </svo:series-directors>
    </xsl:template>
    <xsl:template match="api:field[@name='size']">
        <svo:size>
            <xsl:value-of select="api:text" />
        </svo:size>
    </xsl:template>
    <xsl:template match="api:field[@name='status']">
        <xsl:choose>
            <xsl:when test="api:text='accepted'">
                <bibo:status rdf:resource="http://purl.org/ontology/bibo/accepted"/>
            </xsl:when>
            <xsl:when test="api:text='draft'">
                <bibo:status rdf:resource="http://purl.org/ontology/bibo/draft"/>
            </xsl:when>
            <xsl:when test="api:text='in press'">
                <bibo:status rdf:resource="http://vivoweb.org/ontology/core#inPress"/>
            </xsl:when>
            <xsl:when test="api:text='invited'">
                <bibo:status rdf:resource="http://vivoweb.org/ontology/core#invited"/>
            </xsl:when>
            <xsl:when test="api:text='peer reviewed'">
                <bibo:status rdf:resource="http://purl.org/ontology/bibo/peerReviewed"/>
            </xsl:when>
            <xsl:when test="api:text='published'">
                <bibo:status rdf:resource="http://purl.org/ontology/bibo/published"/>
            </xsl:when>
            <xsl:when test="api:text='rejected'">
                <bibo:status rdf:resource="http://purl.org/ontology/bibo/rejected"/>
            </xsl:when>
            <xsl:when test="api:text='submitted'">
                <bibo:status rdf:resource="http://vivoweb.org/ontology/core#submitted"/>
            </xsl:when>
            <xsl:when test="api:text='unpublished'">
                <bibo:status rdf:resource="http://purl.org/ontology/bibo/unpublished"/>
            </xsl:when>
            <xsl:otherwise>
                <bibo:status rdf:resource="http://www.symplectic.co.uk/vivo/status/{api:text}"/>
            </xsl:otherwise>
        </xsl:choose>
        <svo:status>
            <xsl:value-of select="api:text" />
        </svo:status>
    </xsl:template>
    <xsl:template match="api:field[@name='sub-types']">
        <svo:sub-types>
            <xsl:value-of select="api:text" />
        </svo:sub-types>
    </xsl:template>
    <xsl:template match="api:field[@name='territory']">
        <svo:territory>
            <xsl:value-of select="api:text" />
        </svo:territory>
    </xsl:template>
    <xsl:template match="api:field[@name='transmission']">
        <svo:transmission>
            <xsl:value-of select="api:text" />
        </svo:transmission>
    </xsl:template>
    <xsl:template match="api:field[@name='type-of-work']">
        <svo:type-of-work>
            <xsl:value-of select="api:text" />
        </svo:type-of-work>
    </xsl:template>
    <xsl:template match="api:field[@name='venue']">
        <svo:venue>
            <xsl:value-of select="api:text" />
        </svo:venue>
    </xsl:template>
    <xsl:template match="api:field[@name='version']">
        <svo:version>
            <xsl:value-of select="api:text" />
        </svo:version>
    </xsl:template>
    
    
    

	<xsl:template match="api:keyword">
		<core:freetextKeyword>
			<xsl:value-of select="." />
		</core:freetextKeyword>
	</xsl:template>


	<!-- book chapter, but could also be all sorts of other things, need to 
		look at the category to work out which -->
	<xsl:template match="api:field[@name='number']">
		<bibo:number>
			<xsl:value-of select="api:text" />
		</bibo:number>
	</xsl:template>



	<xsl:template match="api:text" mode="symJournalRef">
		<core:hasPublicationVenue rdf:resource="{$baseURI}journal{.}" />
	</xsl:template>
	
	
    <xsl:template match="api:field[@name='publication-date']" mode="objectReferences" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
        <core:dateTimeValue rdf:resource="{$baseURI}publication{$rid}-publicationDate"/>
    </xsl:template>


    <xsl:template match="api:field[@name='publication-date']" mode="objectEntries" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
       <rdf:Description  rdf:about="{$baseURI}publication{$rid}-publicationDate">
         <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
         <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
         <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/publication-date"/>
         <xsl:apply-templates select="."  mode="dateTimeValue" />
       </rdf:Description>
    </xsl:template>
    
    <xsl:template match="api:field[@name='start-date']" mode="objectReferences" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
        <core:dateTimeValue rdf:resource="{$baseURI}publication{$rid}-startDate"/>
    </xsl:template>
    
   <xsl:template match="api:field[@name='start-date']" mode="objectEntries" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
       <rdf:Description  rdf:about="{$baseURI}publication{$rid}-startDate">
         <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
         <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
         <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/start-date"/>
         <xsl:apply-templates select="."  mode="dateTimeValue" />
       </rdf:Description>
    </xsl:template>

    <xsl:template match="api:field[@name='presented-date']" mode="objectReferences" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
        <core:dateTimeValue rdf:resource="{$baseURI}publication{$rid}-presentedDate"/>
    </xsl:template>
    
   <xsl:template match="api:field[@name='presented-date']" mode="objectEntries" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
       <rdf:Description  rdf:about="{$baseURI}publication{$rid}-presentedDate">
         <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
         <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
         <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/presented-date"/>
         <xsl:apply-templates select="."  mode="dateTimeValue" />
       </rdf:Description>
    </xsl:template>

    <xsl:template match="api:field[@name='filed-date']" mode="objectReferences" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
        <core:dateTimeValue rdf:resource="{$baseURI}publication{$rid}-filedDate"/>
    </xsl:template>
    
   <xsl:template match="api:field[@name='filed-date']" mode="objectEntries" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
       <rdf:Description  rdf:about="{$baseURI}publication{$rid}-filedDate">
         <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
         <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
         <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/filed-date"/>
         <xsl:apply-templates select="."  mode="dateTimeValue" />
       </rdf:Description>
    </xsl:template>

    <xsl:template match="api:field[@name='expiry-date']" mode="objectReferences" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
        <core:dateTimeValue rdf:resource="{$baseURI}publication{$rid}-expiryDate"/>
    </xsl:template>
    
   <xsl:template match="api:field[@name='expiry-date']" mode="objectEntries" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
       <rdf:Description  rdf:about="{$baseURI}publication{$rid}-expiryDate">
         <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
         <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
         <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/expiry-date"/>
         <xsl:apply-templates select="."  mode="dateTimeValue" />
       </rdf:Description>
    </xsl:template>
    
    <xsl:template match="api:field[@name='end-date']" mode="objectReferences" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
        <core:dateTimeValue rdf:resource="{$baseURI}publication{$rid}-endDate"/>
    </xsl:template>
    
   <xsl:template match="api:field[@name='end-date']" mode="objectEntries" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
       <rdf:Description  rdf:about="{$baseURI}publication{$rid}-endDate">
         <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
         <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
         <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/end-date"/>
         <xsl:apply-templates select="."  mode="dateTimeValue" />
       </rdf:Description>
    </xsl:template>

    <xsl:template match="api:field[@name='date']" mode="objectReferences" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
        <core:dateTimeValue rdf:resource="{$baseURI}publication{$rid}-date"/>
    </xsl:template>
    
   <xsl:template match="api:field[@name='date']" mode="objectEntries" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
       <rdf:Description  rdf:about="{$baseURI}publication{$rid}-date">
         <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
         <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
         <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/date"/>
         <xsl:apply-templates select="."  mode="dateTimeValue" />
       </rdf:Description>
    </xsl:template>

    <xsl:template match="api:field[@name='date-submitted']" mode="objectReferences" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
        <core:dateTimeValue rdf:resource="{$baseURI}publication{$rid}-dateSubmitted"/>
    </xsl:template>
    
   <xsl:template match="api:field[@name='date-submitted']" mode="objectEntries" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
       <rdf:Description  rdf:about="{$baseURI}publication{$rid}-dateSubmitted">
         <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
         <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
         <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/date-submitted"/>
         <xsl:apply-templates select="."  mode="dateTimeValue" />
       </rdf:Description>
    </xsl:template>

    <xsl:template match="api:field[@name='date-awarded']" mode="objectReferences" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
        <core:dateTimeValue rdf:resource="{$baseURI}publication{$rid}-dateAwarded"/>
    </xsl:template>
    
   <xsl:template match="api:field[@name='date-awarded']" mode="objectEntries" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
       <rdf:Description  rdf:about="{$baseURI}publication{$rid}-dateAwarded">
         <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
         <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
         <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/date-awarded"/>
         <xsl:apply-templates select="."  mode="dateTimeValue" />
       </rdf:Description>
    </xsl:template>

     <!--  TODO: Apply some logic surrounding conference start and end dates, should be combind into a single dateTime value -->
    <xsl:template match="api:field[@name='conference-start-date']" mode="objectReferences" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
        <core:dateTimeValue rdf:resource="{$baseURI}publication{$rid}-conferenceStartDates"/>
    </xsl:template>
    
   <xsl:template match="api:field[@name='conference-start-date']" mode="objectEntries" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
       <rdf:Description  rdf:about="{$baseURI}publication{$rid}-conferenceStartates">
         <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
         <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
         <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/conference-start-date"/>
         <xsl:apply-templates select="."  mode="dateTimeValue" />
       </rdf:Description>
    </xsl:template>

    <xsl:template match="api:field[@name='conference-finish-date']" mode="objectReferences" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
        <core:dateTimeValue rdf:resource="{$baseURI}publication{$rid}-conferenceFinishDate"/>
    </xsl:template>
    
   <xsl:template match="api:field[@name='conference-finish-date']" mode="objectEntries" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
       <rdf:Description  rdf:about="{$baseURI}publication{$rid}-conferenceFinishDate">
         <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
         <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
         <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/conference-finish-date"/>
         <xsl:apply-templates select="."  mode="dateTimeValue" />
       </rdf:Description>
    </xsl:template>
    <xsl:template match="api:field[@name='finish-date']" mode="objectReferences" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
        <core:dateTimeValue rdf:resource="{$baseURI}publication{$rid}-finishDate"/>
    </xsl:template>
    
   <xsl:template match="api:field[@name='finish-date']" mode="objectEntries" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
       <rdf:Description  rdf:about="{$baseURI}publication{$rid}-finishDate">
         <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
         <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
         <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/finish-date"/>
         <xsl:apply-templates select="."  mode="dateTimeValue" />
       </rdf:Description>
    </xsl:template>

    <xsl:template match="api:field[@name='awarded-date']" mode="objectReferences" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
        <core:dateTimeValue rdf:resource="{$baseURI}publication{$rid}-awardedDate"/>
    </xsl:template>
    
   <xsl:template match="api:field[@name='awarded-date']" mode="objectEntries" >
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
       <rdf:Description  rdf:about="{$baseURI}publication{$rid}-awardedDate">
         <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
         <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
         <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/awarded-date"/>
         <xsl:apply-templates select="."  mode="dateTimeValue" />
       </rdf:Description>
    </xsl:template>
    
    
   <xsl:template match="api:field[@name='author-url']" mode="objectReferences">
     <xsl:variable name="rid" select="ancestor::api:object/@id" />
     <core:webpage rdf:resource="{$baseURI}publication{$rid}-authorWebpage"/>
    </xsl:template>

	<xsl:template match="api:field[@name='author-url']" mode="objectEntries">
		<xsl:variable name="rid" select="ancestor::api:object/@id"/>
		<rdf:Description rdf:about="{$baseURI}publication{$rid}-authorWebpage">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#URLLink" />
			<rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing" />
            <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/author-url"/>
			<core:webpageOf rdf:resource="{$baseURI}publication{$rid}" />
			<core:linkURI rdf:datatype="http://www.w3.org/2001/XMLSchema#anyURI">
				<xsl:value-of select="api:text" />
			</core:linkURI>
			<core:linkAnchorText rdf:datatype="http://www.w3.org/2001/XMLSchema#anyURI">Author</core:linkAnchorText>
            <!--  add properties to enable smushing -->
            <svo:smush>author-url:<xsl:value-of select="api:text" /></svo:smush>
		</rdf:Description>
	</xsl:template>

   <xsl:template match="api:field[@name='publisher-url']" mode="objectReferences">
     <xsl:variable name="rid" select="ancestor::api:object/@id" />
     <core:webpage rdf:resource="{$baseURI}publication{$rid}-publisherWebpage"/>
    </xsl:template>

    <xsl:template match="api:field[@name='publisher-url']" mode="objectEntries">
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
        <rdf:Description rdf:about="{$baseURI}publication{$rid}-publisherWebpage">
            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#URLLink" />
            <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing" />
            <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/publisher-url"/>
            <core:webpageOf rdf:resource="{$baseURI}publication{$rid}" />
            <core:linkURI rdf:datatype="http://www.w3.org/2001/XMLSchema#anyURI">
                <xsl:value-of select="api:text" />
            </core:linkURI>
            <core:linkAnchorText rdf:datatype="http://www.w3.org/2001/XMLSchema#anyURI">Download Original</core:linkAnchorText>
            <!--  add properties to enable smushing -->
            <svo:smush>publisher-url:<xsl:value-of select="api:text" /></svo:smush>
        </rdf:Description>
    </xsl:template>
    


    <!--  start of a group of templates that only outputs 1 object reference -->
    <xsl:template match="api:field[@name='presented-at']" mode="objectReferences" >
     <xsl:variable name="rid" select="ancestor::api:object/@id" />
     <bibo:presentedAt rdf:resource="{$baseURI}publication{$rid}-presentedAt"/>
    </xsl:template>
    
    <xsl:template match="api:field[@name='conference-place']" mode="objectReferences" >
     <xsl:variable name="rid" select="ancestor::api:object/@id" />
     <xsl:choose>
                <xsl:when test="ancestor::api:native/api:field[@name='presented-at']">
                </xsl:when>
                <xsl:otherwise>
                    <bibo:presentedAt rdf:resource="{$baseURI}publication{$rid}-presentedAt"/>
                </xsl:otherwise>
     </xsl:choose>
    </xsl:template>
    
    <xsl:template match="api:field[@name='name-of-conference']" mode="objectReferences" >
     <xsl:variable name="rid" select="ancestor::api:object/@id" />
     <xsl:choose>
                <xsl:when test="ancestor::api:native/api:field[@name='presented-at']">
                </xsl:when>
                <xsl:when test="ancestor::api:native/api:field[@name='conference-place']">
                </xsl:when>
                <xsl:otherwise>
                    <bibo:presentedAt rdf:resource="{$baseURI}publication{$rid}-presentedAt"/>
                </xsl:otherwise>
     </xsl:choose>
    </xsl:template>
    <!--  end of group -->
    
    <xsl:template match="api:field[@name='conference-place' or @name='presented-at' or @name='name-of-conference']" mode="objectEntries">
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
        <rdf:Description rdf:about="{$baseURI}publication{$rid}-presentedAt">
            <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing" />
		    <rdf:type rdf:resource="http://purl.org/NET/c4dm/event.owl#Event"/>
		    <rdf:type rdf:resource="http://purl.org/ontology/bibo/Conference"/>
            <xsl:if test="ancestor::api:native/api:field[@name='location']">
                <core:hasGeographicLocation rdf:resource="{$baseURI}publication{$rid}-presentedAtLocation"/>
            </xsl:if>
            <bibo:presents rdf:resource="{$baseURI}publication{$rid}"/>
            
            <xsl:if test="ancestor::api:native/api:field[@name='conference-place']">
                <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/conference-place"/>
            </xsl:if>
            <xsl:if test="ancestor::api:native/api:field[@name='presented-at']">
                <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/presented-at"/>
            </xsl:if>
            <xsl:if test="ancestor::api:native/api:field[@name='name-of-conference']">
                <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/name-of-conference"/>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="ancestor::api:native/api:field[@name='presented-at']">
                            <rdfs:label><xsl:value-of select="ancestor::api:native/api:field[@name='presented-at']/api:text" /></rdfs:label>                                   
                </xsl:when>
	            <xsl:when test="ancestor::api:native/api:field[@name='name-of-conference'] and 
	                            ancestor::api:native/api:field[@name='conference-place']">
                            <rdfs:label>
                                <xsl:value-of select="ancestor::api:native/api:field[@name='name-of-conference']/api:text" />
                                <xsl:value-of select="ancestor::api:native/api:field[@name='conference-place']/api:text" />
                            </rdfs:label>                
	            </xsl:when>
                <xsl:when test="ancestor::api:native/api:field[@name='name-of-conference']">
                            <rdfs:label>
                                <xsl:value-of select="ancestor::api:native/api:field[@name='name-of-conference']/api:text" />
                                <xsl:value-of select="ancestor::api:native/api:field[@name='conference-place']/api:text" />
                            </rdfs:label>                
                </xsl:when>
                <xsl:when test="ancestor::api:native/api:field[@name='conference-place']">
                            <rdfs:label>
                                <xsl:value-of select="ancestor::api:native/api:field[@name='name-of-conference']/api:text" />
                                <xsl:value-of select="ancestor::api:native/api:field[@name='conference-place']/api:text" />
                            </rdfs:label>                
                </xsl:when>
            </xsl:choose>
            <!--  add properties to enable smushing -->
            <svo:smush>presentedat:<xsl:value-of select="api:text" /></svo:smush>
        </rdf:Description>
        <xsl:if test="ancestor::api:native/api:field[@name='location']">
            <rdf:Description rdf:about="{$baseURI}publication{$rid}-presentedAtLocation">
               <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
               <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Location"/>
               <rdf:type rdf:resource="http://vivoweb.org/ontology/core#GeographicLocation"/>
               <core:geographicLocationOf rdf:resource="{$baseURI}publication{$rid}-presentedAt"/>
               <rdfs:label><xsl:value-of select="ancestor::api:native/api:field[@name='location']/api:text" /></rdfs:label>
	            <!--  add properties to enable smushing -->
	            <svo:smush>location:<xsl:value-of select="api:text" /></svo:smush>
            </rdf:Description>
        </xsl:if>
    </xsl:template>
    
    
    <xsl:template match="api:field[@name='series']" mode="objectReferences">
     <xsl:variable name="rid" select="ancestor::api:object/@id" />
     <core:hasPublicationVenue rdf:resource="{$baseURI}publication{$rid}-series"/>
    </xsl:template>

    <xsl:template match="api:field[@name='series']" mode="objectEntries">
        <xsl:variable name="rid" select="ancestor::api:object/@id"/>
        <rdf:Description rdf:about="{$baseURI}publication{$rid}-series">
		    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
		    <rdf:type rdf:resource="http://purl.org/ontology/bibo/Series"/>
		    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#InformationResource"/>
		    <rdf:type rdf:resource="http://purl.org/ontology/bibo/Collection"/>
		    <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/series"/>
		    <rdfs:label>
		                <xsl:value-of select="api:text" />
		    </rdfs:label>
            <!--  add properties to enable smushing -->
            <svo:smush>series:<xsl:value-of select="api:text" /></svo:smush>
        </rdf:Description>
    </xsl:template>
    <xsl:template match="api:field[@name='series-name']" mode="objectReferences">
     <xsl:variable name="rid" select="ancestor::api:object/@id" />
     <core:informationProductOf rdf:resource="{$baseURI}publication{$rid}-seriesName"/>
    </xsl:template>

    <xsl:template match="api:field[@name='series-name']" mode="objectEntries">
        <xsl:variable name="rid" select="ancestor::api:object/@id"/>
        <rdf:Description rdf:about="{$baseURI}publication{$rid}-seriesName">
            <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
            <rdf:type rdf:resource="http://purl.org/ontology/bibo/Series"/>
            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#InformationResource"/>
            <rdf:type rdf:resource="http://purl.org/ontology/bibo/Collection"/>
            <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/series-name"/>
            <rdfs:label>
                        <xsl:value-of select="api:text" />
            </rdfs:label>
            <!--  add properties to enable smushing -->
            <svo:smush>serise-name:<xsl:value-of select="api:text" /></svo:smush>
        </rdf:Description>
    </xsl:template>


    <xsl:template match="api:field[@name='journal']" mode="objectReferences">
     <xsl:variable name="rid" select="ancestor::api:object/@id" />
     <core:hasPublicationVenue rdf:resource="{$baseURI}publication{$rid}-journal"/>
    </xsl:template>

    <xsl:template match="api:field[@name='journal']" mode="objectEntries">
        <xsl:variable name="rid" select="ancestor::api:object/@id"/>
        <rdf:Description rdf:about="{$baseURI}publication{$rid}-journal">
		    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
		    <rdf:type rdf:resource="http://purl.org/ontology/bibo/Periodical"/>
		    <rdf:type rdf:resource="http://purl.org/ontology/bibo/Journal"/>
		    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#InformationResource"/>
		    <rdf:type rdf:resource="http://purl.org/ontology/bibo/Collection"/>
            <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/journal"/>
            <rdfs:label>
                  <xsl:value-of select="api:text" />
            </rdfs:label>
            <!--  add properties to enable smushing -->
            <svo:smush>journal:<xsl:value-of select="api:text" /></svo:smush>
        </rdf:Description>
    </xsl:template>


    <xsl:template match="api:field[@name='commissioning-body']" mode="objectReferences">
     <xsl:variable name="rid" select="ancestor::api:object/@id" />
     <core:informationResourceSupportedBy rdf:resource="{$baseURI}publication{$rid}-commissioningBody"/>
    </xsl:template>

    <xsl:template match="api:field[@name='commissioning-body']" mode="objectEntries">
        <xsl:variable name="rid" select="ancestor::api:object/@id"/>
        <rdf:Description rdf:about="{$baseURI}publication{$rid}-commissioningBody">
		    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
		    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
		    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Agent"/>
		    <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/commissioning-body"/>
            <rdfs:label>
                  <xsl:value-of select="api:text" />
            </rdfs:label>
            <!--  add properties to enable smushing -->
            <svo:smush>organization:<xsl:value-of select="api:text" /></svo:smush>
        </rdf:Description>
    </xsl:template>


    <xsl:template match="api:field[@name='supervisors']" mode="objectReferences">
     <xsl:variable name="rid" select="ancestor::api:object/@id" />
     <core:informationResourceSupportedBy rdf:resource="{$baseURI}publication{$rid}-supervisors"/>
    </xsl:template>

    <xsl:template match="api:field[@name='supervisors']" mode="objectEntries">
        <xsl:variable name="rid" select="ancestor::api:object/@id"/>
        <rdf:Description rdf:about="{$baseURI}publication{$rid}-supervisors">
            <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
            <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person"/>
            <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Agent"/>
            <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/supervisors"/>
            <rdfs:label>
                  <xsl:value-of select="api:text" />
            </rdfs:label>
            <!--  add properties to enable smushing -->
            <svo:smush>supervisors:<xsl:value-of select="api:text" /></svo:smush>
        </rdf:Description>
    </xsl:template>
    
    

    <xsl:template match="api:field[@name='thesis-type']" mode="objectReferences">
     <xsl:variable name="rid" select="ancestor::api:object/@id" />
     <bibo:degree rdf:resource="{$baseURI}publication{$rid}-thesisType"/>
    </xsl:template>

    <xsl:template match="api:field[@name='thesis-type']" mode="objectEntries">
        <xsl:variable name="rid" select="ancestor::api:object/@id"/>
        <rdf:Description rdf:about="{$baseURI}publication{$rid}-thesisType">
            <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
		    <rdf:type rdf:resource="http://purl.org/ontology/bibo/ThesisDegree"/>
		    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#AcademicDegree"/>
            <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/thesis-type"/>
            <rdfs:label>
                  <xsl:value-of select="api:text" />
            </rdfs:label>
            <!--  add properties to enable smushing -->
            <svo:smush>thesis-type:<xsl:value-of select="api:text" /></svo:smush>
        </rdf:Description>
    </xsl:template>
    
    <xsl:template match="api:field[@name='credits']" mode="objectReferences">
     <xsl:variable name="rid" select="ancestor::api:object/@id" />
     <core:informationResourceSupportedBy rdf:resource="{$baseURI}publication{$rid}-credits"/>
    </xsl:template>

    <xsl:template match="api:field[@name='credits']" mode="objectEntries">
        <xsl:variable name="rid" select="ancestor::api:object/@id"/>
        <rdf:Description rdf:about="{$baseURI}publication{$rid}-credits">
            <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
            <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/credits"/>
            <rdfs:label>
                  <xsl:value-of select="api:text" />
            </rdfs:label>
        </rdf:Description>
    </xsl:template>



    <xsl:template match="api:field[@name='distributors']" mode="objectReferences">
     <xsl:variable name="rid" select="ancestor::api:object/@id" />
     <bibo:distributor rdf:resource="{$baseURI}publication{$rid}-distributors"/>
    </xsl:template>

    <xsl:template match="api:field[@name='distributors']" mode="objectEntries">
        <xsl:variable name="rid" select="ancestor::api:object/@id"/>
        <rdf:Description rdf:about="{$baseURI}publication{$rid}-distributors">
            <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
            <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
            <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Agent"/>
            <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/distributors"/>
            <rdfs:label>
                  <xsl:value-of select="api:text" />
            </rdfs:label>
            <!--  add properties to enable smushing -->
            <svo:smush>organization:<xsl:value-of select="api:text" /></svo:smush>
        </rdf:Description>
    </xsl:template>

    <!-- Repository Items -->
    <xsl:template match="api:repository-items/api:repository-item[api:public-url]" mode="objectReferences">
        <xsl:variable name="rid" select="ancestor::api:object/@id" />
        <core:webpage rdf:resource="{$baseURI}publication{$rid}-repo-{position()}"/>
    </xsl:template>

    <xsl:template match="api:repository-items/api:repository-item[api:public-url]" mode="objectEntries">
        <xsl:variable name="rid" select="ancestor::api:object/@id"/>
        <rdf:Description rdf:about="{$baseURI}publication{$rid}-repo-{position()}">
            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#URLLink" />
            <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing" />
            <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/repository-url"/>
            <core:linkURI rdf:datatype="http://www.w3.org/2001/XMLSchema#anyURI">
                <xsl:value-of select="api:public-url" />
            </core:linkURI>
            <core:linkAnchorText rdf:datatype="http://www.w3.org/2001/XMLSchema#anyURI"><xsl:value-of select="@repository-name" /></core:linkAnchorText>
            <!--  add properties to enable smushing -->
            <svo:smush>repository-url:<xsl:value-of select="api:public-url" /></svo:smush>
        </rdf:Description>
    </xsl:template>

    <!--  BU specific -->
    <xsl:template
        match="api:organisation-defined-data[@field-name='Job Title']" mode="objectReferences">
        <xsl:variable name="rid" select="ancestor::api:object/@id"/>
        <xsl:if test="normalize-space(.)">
        <core:personInPosition rdf:resource="{$baseURI}{$rid}-jobTitle"/>
        </xsl:if>
    </xsl:template>
    <xsl:template
        match="api:organisation-defined-data[@field-name='Job Title']" mode="objectEntries">
        <xsl:variable name="rid" select="ancestor::api:object/@id"/>
        <xsl:variable name="username" select="ancestor::api:object/@username"/>
        
        <xsl:if test="normalize-space(.)">
        <rdf:Description rdf:about="{$baseURI}{$rid}-jobTitle">
		    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
		    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Position"/>
		    <core:positionForPerson rdf:resource="{$baseURI}{$username}"/>
		    <core:positionInOrganization rdf:resource="{$baseURI}{$organizationURIPart}"/>
            <core:hrJobTitle><xsl:value-of select="." /></core:hrJobTitle>
            <rdfs:label><xsl:value-of select="." /></rdfs:label>
            <!--  add properties to enable smushing -->
            <svo:smush>jobtitle:<xsl:value-of select="." /></svo:smush>
        </rdf:Description>        
        <rdf:Description rdf:about="{$baseURI}{$organizationURIPart}">
		    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#University"/>
		    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
		    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
		    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Agent"/>
		    <core:organizationForPosition rdf:resource="{$baseURI}{$rid}-jobTitle"/>
		    <rdfs:label><xsl:value-of select="$organizationName" /></rdfs:label>
            <svo:smush>organization:<xsl:value-of select="$organizationName" /></svo:smush>
        </rdf:Description>
        </xsl:if>
    </xsl:template>

    <xsl:template
            match="api:organisation-defined-data[@field-name='Location' or @field-name='Office']" mode="objectReferences">
        <xsl:variable name="rid" select="ancestor::api:object/@id"/>
        <xsl:if test="normalize-space(.)">
            <core:mailingAddress rdf:resource="{$baseURI}{$rid}-mailingAddress" />
	</xsl:if>
    </xsl:template>
    <xsl:template
            match="api:organisation-defined-data[@field-name='Location' or @field-name='Office']" mode="objectEntries">
        <xsl:variable name="rid" select="ancestor::api:object/@id"/>
        <xsl:variable name="username" select="ancestor::api:object/@username"/>

        <xsl:if test="normalize-space(.)">
        <rdf:Description rdf:about="{$baseURI}{$rid}-mailingAddress">
            <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
            <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Address"/>
            <core:mailingAddressFor rdf:resource="{$baseURI}{$username}"/>
            <core:address1><xsl:value-of select="." /></core:address1>
            <rdfs:label><xsl:value-of select="." /></rdfs:label>
            <!--  add properties to enable smushing -->
            <svo:smush>mailingAddress:<xsl:value-of select="." /></svo:smush>
        </rdf:Description>
        </xsl:if>
    </xsl:template>



</xsl:stylesheet>
