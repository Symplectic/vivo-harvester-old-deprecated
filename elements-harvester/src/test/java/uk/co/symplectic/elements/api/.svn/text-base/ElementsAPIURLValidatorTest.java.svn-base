/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.elements.api;

import org.junit.Assert;
import org.junit.Test;

public class ElementsAPIURLValidatorTest {
    @Test
    public void testIsValid() throws Exception {
        assertFailure(new ElementsAPIURLValidator(null, false), "URL must not be null");
        assertFailure(new ElementsAPIURLValidator("this is not a url", false), "Unsecured endpoint does not begin with http://");
        assertFailure(new ElementsAPIURLValidator("https://touch.symplectic.co.uk/", false), "Unsecured endpoint does not begin with http://");
        assertFailure(new ElementsAPIURLValidator("http://touch.symplectic.co.uk/", true), "Secured endpoint does not begin with https://");

        assertFailure(new ElementsAPIURLValidator("http://not:a:valid:url", false), "Endpoint is not a valid URL");

        assertSuccess(new ElementsAPIURLValidator("https://touch.symplectic.co.uk/", true));
        assertSuccess(new ElementsAPIURLValidator("http://touch.symplectic.co.uk/", false));
    }

    private void assertFailure(ElementsAPIURLValidator validator, String expectedMsg) {
        Assert.assertFalse(validator.isValid());
        Assert.assertTrue(expectedMsg.equals(validator.getLastValidationMessage()));
    }

    private void assertSuccess(ElementsAPIURLValidator validator) {
        Assert.assertTrue(validator.isValid());
    }
}
