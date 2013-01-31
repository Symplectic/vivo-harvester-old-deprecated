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

    <xsl:variable name="activity-biography">53</xsl:variable>
    <xsl:variable name="activity-external-responsibility">49</xsl:variable>
    <xsl:variable name="activity-honor-award">19</xsl:variable>
    <xsl:variable name="activity-invited-talk">47</xsl:variable>
    <xsl:variable name="activity-member">38</xsl:variable>

    <xsl:variable name="activity-academic-group">academic-group-id</xsl:variable>
    <xsl:variable name="activity-keyword">keyword-id</xsl:variable>
    <xsl:variable name="activity-network">network-id</xsl:variable>
    <xsl:variable name="activity-phd-students">phd-student-id</xsl:variable>
    <xsl:variable name="activity-profile-teaching-pg">teaching-pg-id</xsl:variable>
    <xsl:variable name="activity-profile-teaching-ug">teaching-ug-id</xsl:variable>
    <xsl:variable name="activity-public-engagement">public-id</xsl:variable>
    <xsl:variable name="activity-qualification-award">award-id</xsl:variable>
    <xsl:variable name="activity-research-center">research-center-id</xsl:variable>
    <xsl:variable name="activity-research-theme">research-theme-id</xsl:variable>
    <xsl:variable name="activity-school">school-id</xsl:variable>
    <xsl:variable name="activity-social-media">social-media-id</xsl:variable>
    <xsl:variable name="activity-webpage">webpage-id</xsl:variable>

    <!-- Utility template to reduce repetition -->
    <xsl:template name="outputRDF">
        <xsl:param name="content" />
        <xsl:if test="$content">
            <rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
                     xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
                     xmlns:core='http://vivoweb.org/ontology/core#'
                     xmlns:api='http://www.symplectic.co.uk/publications/api'
                     xmlns:svo='http://www.symplectic.co.uk/vivo/'
                     xmlns:ufVivo='http://vivo.ufl.edu/ontology/vivo-ufl/'
                     xmlns:symp='http://www.symplectic.co.uk/ontology/elements/'
                    >
                <xsl:copy-of select="$content" />
            </rdf:RDF>
        </xsl:if>
    </xsl:template>

    <!--
        Biography
    -->
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-biography]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="fullActivityObj" select="document(concat('data/raw-records/',@category,'/',@id))" />
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}">
                    <core:overview><xsl:value-of select="$fullActivityObj//api:records/api:record/api:native/api:field[@name='c-description']/api:text"/></core:overview>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--
        External Responsibility
    -->
    <!-- URI Generator -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-external-responsibility]" mode="professionalActivityURI">
        <xsl:value-of select="$baseURI" /><xsl:text>role</xsl:text><xsl:value-of select="@id" />
    </xsl:template>
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-external-responsibility]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}">
                    <core:hasRole rdf:resource="{$activityURI}"/>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}">
                    <core:roleOf rdf:resource="{$baseURI}{$username}"/>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- Object Details -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-external-responsibility]">
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <!--  role -->
                <rdf:Description rdf:about="{$activityURI}">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Role"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#MemberRole"/>
                    <core:roleContributesTo rdf:resource="{$activityURI}-org"/>
                    <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                    <xsl:if test="api:records/api:record/api:native/api:field[@name='c-awarded-year']" >
                        <core:dateTimeValue rdf:resource="{$activityURI}-start"/>
                    </xsl:if>
                    <xsl:if test="api:records/api:record/api:native/api:field[@name='c-end-year']" >
                        <core:dateTimeValue rdf:resource="{$activityURI}-end"/>
                    </xsl:if>
                    <rdfs:label>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-title']/api:text"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-role']/api:text"/>
                    </rdfs:label>
                </rdf:Description>
                <!--  organisation -->
                <rdf:Description rdf:about="{$activityURI}-org">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
                    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Agent"/>
                    <core:contributingRole rdf:resource="{$activityURI}"/>
                    <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                    <rdfs:label>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-organisation']/api:text"/>
                    </rdfs:label>
                    <svo:smush>organization:<xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-organisation']/api:text"/>
                    </svo:smush>
                </rdf:Description>
                <!--  start role -->
                <xsl:if test="api:records/api:record/api:native/api:field[@name='c-awarded-year']" >
                    <rdf:Description rdf:about="{$activityURI}-start">
                        <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                        <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/role-start"/>
                        <core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision" />
                        <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime">
                            <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-awarded-year']/api:integer" />-01-01T00:00:00Z
                        </core:dateTime>
                    </rdf:Description>
                </xsl:if>
                <!--  end role -->
                <xsl:if test="api:records/api:record/api:native/api:field[@name='c-end-year']" >
                    <rdf:Description rdf:about="{$activityURI}-end">
                        <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                        <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/role-end"/>
                        <core:dateTimePrecision
                                rdf:resource="http://vivoweb.org/ontology/core#yearPrecision" />
                        <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime">
                            <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-end-year']/api:integer" />-01-01T00:00:00Z
                        </core:dateTime>
                    </rdf:Description>
                </xsl:if>
            </xsl:with-param>
        </xsl:call-template>
        <!--
            type also has
            c-hyperlink,  example
        -->
    </xsl:template>

    <!--
        Honor Award
    -->
    <!-- URI Generator -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-honor-award]" mode="professionalActivityURI">
        <xsl:value-of select="$baseURI" /><xsl:text>award</xsl:text><xsl:value-of select="@id" />
    </xsl:template>
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-honor-award]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}">
                    <core:awardOrHonor rdf:resource="{$activityURI}"/>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}">
                    <core:awardOrHonorFor rdf:resource="{$baseURI}{$username}"/>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- Object Details -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-honor-award]">
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <!--  award -->
                <rdf:Description rdf:about="{$activityURI}">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#AwardReceipt"/>
                    <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                    <xsl:if test="api:records/api:record/api:native/api:field[@name='c-begin-date']" >
                        <core:dateTimeValue rdf:resource="{$activityURI}-date"/>
                    </xsl:if>
                    <rdfs:label>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-name']/api:text"/>
                    </rdfs:label>
                    <svo:smush>award:<xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-name']/api:text"/>
                    </svo:smush>
                    <!--  What do we do about award date, was not in the examples but is in the /activities/type -->
                </rdf:Description>
                <!--  award date -->
                <xsl:if test="api:records/api:record/api:native/api:field[@name='c-begin-date']" >
                    <rdf:Description rdf:about="{$activityURI}-date">
                        <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                        <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/award-date"/>
                        <core:dateTimePrecision
                                rdf:resource="http://vivoweb.org/ontology/core#yearPrecision" />
                        <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime">
                            <xsl:apply-templates select="api:records/api:record/api:native/api:field[@name='c-begin-date']/api:date" mode="dateTimeValue" /><xsl:text>-01-01T00:00:00Z</xsl:text>
                        </core:dateTime>
                    </rdf:Description>
                </xsl:if>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--
        Invited Talk
    -->
    <!-- URI Generator -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-invited-talk]" mode="professionalActivityURI">
        <xsl:value-of select="$baseURI" /><xsl:text>invitedtalk</xsl:text><xsl:value-of select="@id" />
    </xsl:template>
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-invited-talk]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}-role">
                    <core:hasPresenterRole rdf:resource="{$activityURI}"/>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}-role">
                    <core:presenterRoleOf rdf:resource="{$baseURI}{$username}"/>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- Object Details -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-invited-talk]">
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$activityURI}-role">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Role"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#PresenterRole"/>
                    <core:roleRealizedIn rdf:resource="{$activityURI}"/>
                    <rdfs:label>Speaker</rdfs:label>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Presentation"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#InvitedTalk"/>
                    <rdf:type rdf:resource="http://purl.org/NET/c4dm/event.owl#Event"/>
                    <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                    <rdfs:label>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-details']/api:text"/>
                    </rdfs:label>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--
        Member
    -->
    <!-- URI Generator -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-member]" mode="professionalActivityURI">
        <xsl:value-of select="$baseURI" /><xsl:text>member</xsl:text><xsl:value-of select="@id" />
    </xsl:template>
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-member]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}">
                    <core:hasMemberRole rdf:resource="{$activityURI}-role"/>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}-role">
                    <core:memberRoleOf rdf:resource="{$baseURI}{$username}"/>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- Object Details -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-member]">
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <!--  role -->
                <rdf:Description rdf:about="{$activityURI}-role">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Role"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#MemberRole"/>
                    <core:roleContributesTo rdf:resource="{$activityURI}-org"/>
                    <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                    <xsl:if test="api:records/api:record/api:native/api:field[@name='c-awarded-year']" >
                        <core:dateTimeValue rdf:resource="{$activityURI}-start"/>
                    </xsl:if>
                    <xsl:if test="api:records/api:record/api:native/api:field[@name='c-end-year']" >
                        <core:dateTimeValue rdf:resource="{$activityURI}-end"/>
                    </xsl:if>
                    <rdfs:label>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-role']/api:text"/>
                    </rdfs:label>
                </rdf:Description>
                <!--  organization -->
                <rdf:Description rdf:about="{$activityURI}-org">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
                    <core:contributingRole rdf:resource="{$activityURI}-role"/>
                    <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                    <rdfs:label>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-organisation']/api:text"/>
                    </rdfs:label>
                    <!--  for some reason smushing this fails -->
                    <svo:smush>organization:<xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-organisation']/api:text"/></svo:smush>
                </rdf:Description>
                <!--  start role -->
                <xsl:if test="api:records/api:record/api:native/api:field[@name='c-awarded-year']" >
                    <rdf:Description rdf:about="{$activityURI}-start">
                        <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                        <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/role-start"/>
                        <core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision" />
                        <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime">
                            <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-awarded-year']/api:integer" />-01-01T00:00:00Z
                        </core:dateTime>
                    </rdf:Description>
                </xsl:if>
                <!--  end role -->
                <xsl:if test="api:records/api:record/api:native/api:field[@name='c-end-year']" >
                    <rdf:Description rdf:about="{$activityURI}-end">
                        <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                        <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/role-end"/>
                        <core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision" />
                        <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime">
                            <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-end-year']/api:integer" />-01-01T00:00:00Z
                        </core:dateTime>
                    </rdf:Description>
                </xsl:if>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--
        Additional Types
    -->

    <!--
        Keywords
    -->
    <!-- URI Generator -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-keyword]" mode="professionalActivityURI">
        <xsl:value-of select="$baseURI" /><xsl:text>keyword</xsl:text><xsl:value-of select="@id" />
    </xsl:template>
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-keyword]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}">
                    <symp:hasKeyword rdf:resource="{$activityURI}"/>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}">
                    <symp:keywordFor rdf:resource="{$baseURI}{$username}"/>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- Object details -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-keyword]">
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$activityURI}">
                    <!-- rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Group"/ -->
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
                    <rdfs:label><xsl:value-of select="api:records/api:record/api:native/api:field/api:text"/></rdfs:label>
                    <svo:smush>keyword:<xsl:value-of select="api:records/api:record/api:native/api:field/api:text"/></svo:smush>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--
        Academic Group
    -->
    <!-- URI Generator -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-academic-group]" mode="professionalActivityURI">
        <xsl:value-of select="$baseURI" /><xsl:text>academicgroup</xsl:text><xsl:value-of select="@id" />
    </xsl:template>
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-academic-group]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}">
                    <core:currentMemberOf rdf:resource="{$activityURI}"/>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}">
                    <core:hasCurrentMember rdf:resource="{$baseURI}{$username}"/>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- Object details -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-academic-group]">
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$activityURI}">
                    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Group"/>
                    <rdfs:label><xsl:value-of select="api:records/api:record/api:native/api:field/api:text"/></rdfs:label>
                    <svo:smush>organization:<xsl:value-of select="api:records/api:record/api:native/api:field/api:text"/></svo:smush>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--
        Research Center
    -->
    <!-- URI Generator -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-research-center]" mode="professionalActivityURI">
        <xsl:value-of select="$baseURI" /><xsl:text>researchcenter</xsl:text><xsl:value-of select="@id" />
    </xsl:template>
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-research-center]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="fullActivityObj" select="document(concat('data/raw-records/',@category,'/',@id))" />
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}">
                    <xsl:for-each select="$fullActivityObj//api:records/api:record/api:native/api:field/api:items/api:item">
                        <core:currentMemberOf rdf:resource="{$activityURI}-{position()}" />
                    </xsl:for-each>
                </rdf:Description>
                <xsl:for-each select="$fullActivityObj//api:records/api:record/api:native/api:field/api:items/api:item">
                    <rdf:Description rdf:about="{$activityURI}-{position()}">
                        <core:hasCurrentMember rdf:resource="{$baseURI}{$username}"/>
                    </rdf:Description>
                </xsl:for-each>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- Object details -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-research-center]">
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <xsl:for-each select="api:records/api:record/api:native/api:field/api:items/api:item">
                    <rdf:Description rdf:about="{$activityURI}-{position()}">
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#ResearchOrganization"/>
                        <rdfs:label><xsl:value-of select="."/></rdfs:label>
                        <svo:smush>organization:<xsl:value-of select="."/></svo:smush>
                    </rdf:Description>
                </xsl:for-each>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--
        School
    -->
    <!-- URI Generator -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-school]" mode="professionalActivityURI">
        <xsl:value-of select="$baseURI" /><xsl:text>school</xsl:text><xsl:value-of select="@id" />
    </xsl:template>
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-school]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}">
                    <core:currentMemberOf rdf:resource="{$activityURI}"/>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}">
                    <core:hasCurrentMember rdf:resource="{$baseURI}{$username}"/>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- Object Details -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-school]">
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$activityURI}">
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#School"/>
                    <rdfs:label><xsl:value-of select="api:records/api:record/api:native/api:field/api:text"/></rdfs:label>
                    <svo:smush>organization:<xsl:value-of select="api:records/api:record/api:native/api:field/api:text"/></svo:smush>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--
        Network
    -->
    <!-- URI Generator -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-network]" mode="professionalActivityURI">
        <xsl:value-of select="$baseURI" /><xsl:text>network</xsl:text><xsl:value-of select="@id" />
    </xsl:template>
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-network]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}">
                    <core:hasColaborator rdf:resource="{$activityURI}"/>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}">
                    <core:hasColaborator rdf:resource="{$baseURI}{$username}"/>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- Object Details -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-network]">
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$activityURI}">
                    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Group"/>
                    <xsl:if test="api:records/api:record/api:native/api:field[@name='c-awarded-year']" >
                        <core:dateTimeValue rdf:resource="{$activityURI}-date"/>
                    </xsl:if>
                </rdf:Description>
                <xsl:if test="api:records/api:record/api:native/api:field[@name='c-awarded-year']" >
                    <rdf:Description rdf:about="{$activityURI}-date">
                        <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                        <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/award-date"/>
                        <core:dateTimePrecision rdf:resource="http://vivoweb.org/ontology/core#yearPrecision" />
                        <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime">
                            <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-awarded-year']/api:integer" />-01-01T00:00:00Z
                        </core:dateTime>
                    </rdf:Description>
                </xsl:if>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--
        PhD Student
    -->
    <!-- URI Generator -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-phd-students]" mode="professionalActivityURI">
        <xsl:value-of select="$baseURI" /><xsl:text>graduate-student</xsl:text><xsl:value-of select="@id" />
    </xsl:template>
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-phd-students]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}">
                    <core:advisorIn rdf:resource="{$activityURI}-relationship" />
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}-relationship">
                    <core:advisor rdf:resource="{$baseURI}{$username}" />
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- Object Details -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-phd-students]">
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$activityURI}-relationship">
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#GraduateAdvisingRelationship"/>
                    <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                    <core:advisee rdf:resource="{$activityURI}" />
                    <core:hasSubjectArea rdf:resource="{$activityURI}-subject" />
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}">
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#GraduateStudent"/>
                    <core:adviseeIn rdf:resource="{$activityURI}-relationship" />
                    <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                    <rdfs:label><xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-title']/api:text"/></rdfs:label>
                    <svo:smush>person:<xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-title']/api:text"/></svo:smush>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}-subject">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
                    <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                    <rdfs:label><xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-details']/api:text"/></rdfs:label>
                    <svo:smush>concept:<xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-details']/api:text"/></svo:smush>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--
        Profile of PG teaching
    -->
    <!-- URI Generator -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-profile-teaching-pg]" mode="professionalActivityURI">
        <xsl:value-of select="$baseURI" /><xsl:text>pgteacher</xsl:text><xsl:value-of select="@id" />
    </xsl:template>
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-profile-teaching-pg]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}">
                    <core:hasTeacherRole rdf:resource="{$activityURI}"/>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}">
                    <core:teacherRoleOf rdf:resource="{$baseURI}{$username}"/>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- Object Details -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-profile-teaching-pg]">
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$activityURI}">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Role"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#TeacherRole"/>
                    <core:roleRealizedIn rdf:resource="{$activityURI}-course" />
                    <rdfs:label ><xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-details']/api:text"/></rdfs:label>
                    <core:description><xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-details']/api:text"/></core:description>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}-course">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Course"/>
                    <core:realizedRole rdf:resource="{$activityURI}" />
                    <rdfs:label>Post Graduate Teaching Course</rdfs:label>
                    <!--  add properties to enable smushing -->
                    <svo:smush>course:Post Graduate Teaching Course</svo:smush>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--
        Profile of UG teaching
    -->
    <!-- URI Generator -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-profile-teaching-ug]" mode="professionalActivityURI">
        <xsl:value-of select="$baseURI" /><xsl:text>ugteacher</xsl:text><xsl:value-of select="@id" />
    </xsl:template>
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-profile-teaching-ug]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}">
                    <core:hasTeacherRole rdf:resource="{$activityURI}"/>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}">
                    <core:teacherRoleOf rdf:resource="{$baseURI}{$username}"/>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- Object Details -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-profile-teaching-ug]">
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$activityURI}">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Role"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#TeacherRole"/>
                    <core:roleRealizedIn rdf:resource="{$activityURI}-course" />
                    <rdfs:label>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-details']/api:text"/>
                    </rdfs:label>
                    <core:description>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-details']/api:text"/>
                    </core:description>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}-course">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Course"/>
                    <core:realizedRole rdf:resource="{$activityURI}" />
                    <rdfs:label>
                        Undergraduate Teaching Course
                    </rdfs:label>
                    <!--  add properties to enable smushing -->
                    <svo:smush>course:Undergraduate Teaching Course</svo:smush>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--
        Public Engagement
    -->
    <!-- URI Generator -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-public-engagement]" mode="professionalActivityURI">
        <xsl:value-of select="$baseURI" /><xsl:text>outreach</xsl:text><xsl:value-of select="@id" />
    </xsl:template>
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-public-engagement]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}">
                    <core:hasOutreachProviderRole rdf:resource="{$activityURI}"/>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}">
                    <core:outreachProviderRoleOf rdf:resource="{$baseURI}{$username}"/>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- Object Details -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-public-engagement]">
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$activityURI}">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#Role"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#OutreachProviderRole"/>
                    <core:roleContributesTo rdf:resource="{$baseURI}outreach-external-org" />
                    <rdfs:label><xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-details']/api:text"/></rdfs:label>
                    <!--  add properties to enable smushing -->
                    <svo:smush>outreachrole:<xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-details']/api:text"/></svo:smush>
                </rdf:Description>
                <!--  not enough information to be able to specify this from the Elements API -->
                <rdf:Description rdf:about="{$baseURI}outreach-external-org">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
                    <core:contributingRole rdf:resource="{$baseURI}outreach{@id}"/>
                    <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                    <rdfs:label>External Organization</rdfs:label>
                    <svo:smush>organization:External Organization</svo:smush>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--
        Social Media
    -->
    <!-- URI Generator -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-social-media]" mode="professionalActivityURI">
        <xsl:value-of select="$baseURI" /><xsl:text>socialmedia</xsl:text><xsl:value-of select="@id" />
    </xsl:template>
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-social-media]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}">
                    <core:webpage rdf:resource="{$activityURI}"/>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}">
                    <core:webpageOf rdf:resource="{$baseURI}{$username}"/>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- Object Details -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-social-media]">
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$activityURI}">
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#URLLink" />
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing" />
                    <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/author-url"/>
                    <core:linkURI>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-hyperlink']/api:text"/>
                    </core:linkURI>
                    <core:linkAnchorText><xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-title']/api:text"/></core:linkAnchorText>
                    <svo:smush>
                        webpage:
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-title']/api:text"/>:
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-hyperlink']/api:text"/>
                    </svo:smush>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--
        Webpage
    -->
    <!-- URI Generator -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-webpage]" mode="professionalActivityURI">
        <xsl:value-of select="$baseURI" /><xsl:text>webpage</xsl:text><xsl:value-of select="@id" />
    </xsl:template>
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-webpage]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}">
                    <core:webpage rdf:resource="{$activityURI}"/>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}">
                    <core:webpageOf rdf:resource="{$baseURI}{$username}"/>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- Object Details -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-webpage]">
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$activityURI}">
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#URLLink" />
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing" />
                    <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/author-url"/>
                    <core:linkURI>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-hyperlink']/api:text"/>
                    </core:linkURI>
                    <core:linkAnchorText>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-title']/api:text"/>
                    </core:linkAnchorText>
                    <svo:smush>
                        webpage:
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-title']/api:text"/>:
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-hyperlink']/api:text"/>
                    </svo:smush>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--
        Qualification Award
    -->
    <!-- URI Generator -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-qualification-award]" mode="professionalActivityURI">
        <xsl:value-of select="$baseURI" /><xsl:text>academic-degree</xsl:text><xsl:value-of select="@id" />
    </xsl:template>
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-qualification-award]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}">
                    <core:educationalTraining rdf:resource="{$activityURI}"/>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}">
                    <core:educationalTrainingOf rdf:resource="{$baseURI}{$username}"/>
                </rdf:Description>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- Object Details -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-qualification-award]">
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$activityURI}">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#EducationalTraining"/>
                    <core:degreeEarned rdf:resource="{$activityURI}-degree"/>
                    <core:majorField><xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-subject']/api:text"/></core:majorField>
                    <xsl:if test="api:records/api:record/api:native/api:field[@name='c-awarded-year']" >
                        <core:dateTimeValue rdf:resource="{$activityURI}-date"/>
                    </xsl:if>
                    <rdfs:label>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-qualification-level']/api:text"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-subject']/api:text"/>
                    </rdfs:label>
                    <core:trainingAtOrganization rdf:resource="{$activityURI}-org"/>
                    <!--
                        <core:departmentOrSchool>Funding organization</core:departmentOrSchool>
                        <core:supplementalInformation>Post Doc training</core:supplementalInformation>
                    -->
                </rdf:Description>
                <!--  organization -->
                <rdf:Description rdf:about="{$activityURI}-org">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#School"/>
                    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
                    <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Agent"/>
                    <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                    <rdfs:label>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-organisation']/api:text"/>
                    </rdfs:label>
                    <svo:smush>organization:<xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-organisation']/api:text"/></svo:smush>
                </rdf:Description>
                <rdf:Description rdf:about="{$activityURI}-degree">
                    <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                    <rdf:type rdf:resource="http://vivoweb.org/ontology/core#AcademicDegree"/>
                    <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                    <core:abbreviation><xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-qualification-level']/api:text"/></core:abbreviation>
                    <rdfs:label>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-qualification-level']/api:text"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-subject']/api:text"/>
                    </rdfs:label>
                    <svo:smush>academic-degree:<xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-qualification-level']/api:text"/><xsl:text> </xsl:text>
                        <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-subject']/api:text"/>
                    </svo:smush>
                </rdf:Description>
                <!--  award date -->
                <xsl:if test="api:records/api:record/api:native/api:field[@name='c-awarded-year']" >
                    <rdf:Description rdf:about="{$activityURI}-date">
                        <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                        <rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue"/>
                        <rdf:type rdf:resource="http://www.symplectic.co.uk/vivo/award-date"/>
                        <core:dateTimePrecision
                                rdf:resource="http://vivoweb.org/ontology/core#yearPrecision" />
                        <core:dateTime rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime">
                            <xsl:value-of select="api:records/api:record/api:native/api:field[@name='c-awarded-year']/api:integer" />-01-01T00:00:00Z
                        </core:dateTime>
                    </rdf:Description>
                </xsl:if>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!--
        Research themes
    -->
    <!-- URI Generator -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-research-theme]" mode="professionalActivityURI">
        <xsl:value-of select="$baseURI" /><xsl:text>concept</xsl:text><xsl:value-of select="@id" />
    </xsl:template>
    <!-- Relationship links -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-research-theme]" mode="professionalActivityRelationship">
        <xsl:param name="username" />
        <xsl:variable name="fullActivityObj" select="document(concat('data/raw-records/',@category,'/',@id))" />
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <rdf:Description rdf:about="{$baseURI}{$username}">
                    <xsl:for-each select="$fullActivityObj//api:records/api:record/api:native/api:field/api:items/api:item">
                        <core:hasResearchArea rdf:resource="{$activityURI}-{position()}" />
                    </xsl:for-each>
                </rdf:Description>
                <xsl:for-each select="$fullActivityObj//api:records/api:record/api:native/api:field/api:items/api:item">
                    <rdf:Description rdf:about="{$activityURI}-{position()}">
                        <core:researchAreaOf rdf:resource="{$baseURI}{$username}"/>
                    </rdf:Description>
               </xsl:for-each>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <!-- Object Details -->
    <xsl:template match="api:object[@category='activity' and @type-id=$activity-research-theme]">
        <xsl:variable name="activityURI"><xsl:apply-templates select="." mode="professionalActivityURI" /></xsl:variable>
        <xsl:call-template name="outputRDF">
            <xsl:with-param name="content">
                <xsl:for-each select="api:records/api:record/api:native/api:field/api:items/api:item">
                    <rdf:Description rdf:about="{$activityURI}-{position()}">
                        <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                        <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
                        <ufVivo:harvestedBy>Symplectic-Harvester</ufVivo:harvestedBy>
                        <rdfs:label><xsl:value-of select="."/></rdfs:label>
                        <svo:smush>concept:<xsl:value-of select="."/></svo:smush>
                    </rdf:Description>
                </xsl:for-each>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>