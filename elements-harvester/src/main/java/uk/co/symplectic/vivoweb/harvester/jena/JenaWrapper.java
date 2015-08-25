/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package uk.co.symplectic.vivoweb.harvester.jena;

import com.hp.hpl.jena.rdf.model.Model;
import org.vivoweb.harvester.util.repo.JenaConnect;

public class JenaWrapper {
    private JenaConnect jenaConnect;

    public JenaWrapper(JenaConnect jenaConnect) {
        this.jenaConnect = jenaConnect;
    }

    public synchronized void remove(Model model) {
        jenaConnect.getJenaModel().remove(model);
    }

    public synchronized void add(Model model) {
        jenaConnect.getJenaModel().add(model);
    }
}
