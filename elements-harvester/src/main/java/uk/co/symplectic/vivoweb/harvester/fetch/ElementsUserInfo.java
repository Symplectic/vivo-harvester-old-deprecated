/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.fetch;

public class ElementsUserInfo {
    private boolean isCurrentStaff = true;
    private String photoUrl = null;

    public ElementsUserInfo() {}

    public boolean getIsCurrentStaff() {
        return isCurrentStaff;
    }

    public void setIsCurrentStaff(boolean isCurrentStaff) {
        this.isCurrentStaff = isCurrentStaff;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
