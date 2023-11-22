/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2023 SAP SE or an SAP affiliate company and Eclipse Dirigible
 * contributors SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.dirigible.components.engine.camel.domain;

import com.google.gson.annotations.Expose;
import org.eclipse.dirigible.components.base.artefact.Artefact;

import javax.persistence.*;

@Entity
@Table(name = "DIRIGIBLE_CAMEL")
public class Camel extends Artefact {
    public static final String ARTEFACT_TYPE = "camel";

    /** The id. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CAMEL_ID", nullable = false)
    private Long id;

    /** The content. */
    @Column(name = "CAMEL_CONTENT", columnDefinition = "CLOB", nullable = true)
    @Expose
    private byte[] content;

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the content.
     *
     * @return the content
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Sets the content.
     *
     * @param content the content to set
     */
    public void setContent(byte[] content) {
        this.content = content;
    }
}
