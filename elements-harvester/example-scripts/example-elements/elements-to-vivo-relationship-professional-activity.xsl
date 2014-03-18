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

    <!--
        Template for handling relationships between users and professional activities.
    -->

    <!-- Import XSLT files that are used -->
    <xsl:import href="elements-to-vivo-activity.xsl" />
    <xsl:import href="elements-to-vivo-utils.xsl" />

    <xsl:template match="api:relationship[@type='activity-user-association']">

        <xsl:variable name="associationURI" select="svfn:relationshipURI(.,'activity-user-association')" />

        <!-- Get the activity object reference from the relationship -->
        <xsl:variable name="activity" select="api:related/api:object[@category='activity']" />

        <!-- Get the user object reference from the relationship -->
        <xsl:variable name="user" select="api:related/api:object[@category='user']" />

        <xsl:variable name="fullUserObj" select="svfn:fullObject($user)" />

        <xsl:variable name="fullActivityObj" select="svfn:fullObject($activity)" />

        <!-- If the Relationship (Association) record we are processing involves an Honor Award activity, we are going to process it in this way -->

        <xsl:if test="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='c-certification-honor-award-name']">

        <xsl:variable name="honorAwardName" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='c-certification-honor-award-name']/api:text"/>

        <xsl:variable name="honorAwardURI" select="concat($baseURI, 'award-', svfn:stringToURI($honorAwardName))" />

        <xsl:variable name="awardingOrganization" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='c-institution-organization-agency-name']/api:text"/>

        <xsl:variable name="awardingOrganizationURI" select="concat($baseURI, 'organization-', svfn:stringToURI($awardingOrganization))" />

        <xsl:variable name="honorAwardDate" select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='c-start-date']"/>

        <!--Add a reference to the association object to the activity object -->
        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="$honorAwardURI" />
            <xsl:with-param name="rdfNodes">
                <vivo:receipt rdf:resource="{$associationURI}"/>
            </xsl:with-param>
        </xsl:call-template>

        <!--Add a reference to the association object to the user object -->
        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="svfn:userURI($user)" />
            <xsl:with-param name="rdfNodes">
                <vivo:awardOrHonor rdf:resource="{$associationURI}"/>
            </xsl:with-param>
        </xsl:call-template>

        <!-- Output the Awarding Organization Object, if there is one -->
        <xsl:if test="$awardingOrganization!=''">
        <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="$awardingOrganizationURI" />
            <xsl:with-param name="rdfNodes">
                <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
                <rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Agent"/>
                <rdfs:label><xsl:value-of select="$awardingOrganization"/></rdfs:label>
                <vivo:awardConferred rdf:resource="{$honorAwardURI}"/>
            </xsl:with-param>
        </xsl:call-template>
        </xsl:if>


         <!-- Create a URI for the associated award date, and create the award date object (if there is one) -->
         <xsl:variable name="honorAwardBeginDateObjectURI" select="concat($associationURI, '-award-date')" />
         <xsl:variable name="honorAwardBeginDateObject" select="svfn:renderDateObject(.,$honorAwardBeginDateObjectURI,$honorAwardDate)" />

         <!-- Output the association object -->


            <xsl:call-template name="render_rdf_object">
            <xsl:with-param name="objectURI" select="$associationURI" />
            <xsl:with-param name="rdfNodes">
                <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
                <rdf:type rdf:resource="http://vivoweb.org/ontology/core#AwardReceipt"/>
                <vivo:receiptOf rdf:resource="{$honorAwardURI}"/>
                <vivo:awardOrHonorFor rdf:resource="{svfn:userURI($user)}"/>

                <!-- If the date object exists (check for child nodes), output a reference to it -->
                <xsl:if test="$honorAwardBeginDateObject/*" >
                    <vivo:dateTimeValue rdf:resource="{$honorAwardBeginDateObjectURI}"/>
                </xsl:if>

                <!-- Grab the User and Activity Object values to Label the Relationship Object Appropriately. -->
                <rdfs:label>
                    <xsl:value-of select="$fullActivityObj/symp:entry/api:object/api:records/api:record/api:native/api:field[@name='c-certification-honor-award-name']/api:text"/>
                    <xsl:text> (</xsl:text>
                    <xsl:value-of select="$fullUserObj/symp:entry/api:object/api:first-name"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="$fullUserObj/symp:entry/api:object/api:last-name"/>
                    <xsl:text>)</xsl:text>

                    <!-- Add an award date to the Relationship Label, if there is one. -->
                    <xsl:if test="$honorAwardBeginDateObject/*" >
                    <xsl:text> - </xsl:text>
                    <xsl:value-of select="$honorAwardDate"/>
                    </xsl:if>
                </rdfs:label>

            </xsl:with-param>
        </xsl:call-template>

        <xsl:copy-of select="$honorAwardBeginDateObject" />

        </xsl:if>

        <!-- Match relationship of type activity-to-user association -->

        <!--
            Apply templates on the activity object, in "processRelationship" mode
            This allows the activity to apply it's statements to the user object, if necessary
        -->
        <xsl:apply-templates select="api:related/api:object[@category='activity']" mode="processRelationship"> <!-- api:related[@direction='from']/api:object mode="professionalActivityRelationship" -->
            <!-- Supply the URI of the user object that is related as a parameter -->
            <xsl:with-param name="userURI" select="svfn:userURI(api:related/api:object[@category='user'])" />
        </xsl:apply-templates>
    </xsl:template>
</xsl:stylesheet>