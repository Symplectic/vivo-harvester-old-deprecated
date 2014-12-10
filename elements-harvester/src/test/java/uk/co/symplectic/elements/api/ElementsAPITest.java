/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.elements.api;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ElementsAPITest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testGetAPIValid() throws Exception {
        Assert.assertNotNull(ElementsAPI.getAPI("3.7"));
        Assert.assertNotNull(ElementsAPI.getAPI("3.7.16"));
        Assert.assertNotNull(ElementsAPI.getAPI("4.6"));
        Assert.assertNotNull(ElementsAPI.getAPI("4.9"));

        Assert.assertNotNull(ElementsAPI.getAPI("v3.7.16"));
        Assert.assertNotNull(ElementsAPI.getAPI("v 3.7.16"));
        Assert.assertNotNull(ElementsAPI.getAPI("v4.6"));
        Assert.assertNotNull(ElementsAPI.getAPI("v 4.6"));
        Assert.assertNotNull(ElementsAPI.getAPI("v4.9"));
        Assert.assertNotNull(ElementsAPI.getAPI("v 4.9"));

        Assert.assertNotNull(ElementsAPI.getAPI("version 3.7.16"));
        Assert.assertNotNull(ElementsAPI.getAPI("Version 3.7.16"));
        Assert.assertNotNull(ElementsAPI.getAPI("version 4.6"));
        Assert.assertNotNull(ElementsAPI.getAPI("Version 4.6"));
        Assert.assertNotNull(ElementsAPI.getAPI("version 4.9"));
        Assert.assertNotNull(ElementsAPI.getAPI("Version 4.9"));
    }

    @Test
    public void testGetAPIInvalidVersion() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Unsupported version");
        ElementsAPI.getAPI("3.7.1");
        ElementsAPI.getAPI("value 3.7.1");
        ElementsAPI.getAPI("4.8");
        ElementsAPI.getAPI("ver 4.9");

        ElementsAPI.getAPI(null);
    }

    @Test
    public void testGetAPINull() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("No version supplied");
        ElementsAPI.getAPI(null);
    }

/*
    @Test
    public void testExecute() throws Exception {
        File test = new File("data");
        if (test.exists()) {
            FileUtils.deleteDirectory(test);
        }

        ElementsAPI api = ElementsAPI.getAPI(ElementsAPI.VERSION_3_7, "http://touch.symplectic.co.uk/bu/insecure", false);
        ElementsAPIFeedObjectQuery feedQuery = new ElementsAPIFeedObjectQuery();
        feedQuery.setFullDetails(true);
        feedQuery.setProcessAllPages(true);
        feedQuery.setModifiedSince("2012-07-01T00:00:00Z");

        feedQuery.setCategory("users");
        api.execute(feedQuery, new ElementsAPIFeedObjectHandler() {

            @Override
            public void handle(ElementsRelationship relationship) {
            }
        });

        feedQuery.setCategory("publications");
        api.execute(feedQuery, new ElementsAPIFeedObjectHandler() {

            @Override
            public void handle(ElementsRelationship relationship) {
            }
        });

        ElementsAPIFeedRelationshipQuery relationshipFeedQuery = new ElementsAPIFeedRelationshipQuery();
        relationshipFeedQuery.setProcessAllPages(true);
        api.execute(relationshipFeedQuery, new ElementsAPIFeedObjectHandler() {

            @Override
            public void handle(ElementsRelationship relationship) {
            }
        });
    }
*/
}
