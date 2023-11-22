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
package org.eclipse.dirigible.components.openapi.domain;

import org.eclipse.dirigible.components.base.artefact.Artefact;

import javax.persistence.*;

/**
 * The Class OpenAPI.
 */
@Entity
@Table(name = "DIRIGIBLE_OPENAPI")
public class OpenAPI extends Artefact {

    /**
     * The Constant ARTEFACT_TYPE.
     */
    public static final String ARTEFACT_TYPE = "openapi";

    /**
     * The id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OPENAPI_ID", nullable = false)
    private Long id;

    /**
     * Instantiates a new open API.
     *
     * @param location the location
     * @param name the name
     * @param description the description
     */
    public OpenAPI(String location, String name, String description) {
        super(location, name, ARTEFACT_TYPE, description, null);
    }

    /**
     * Instantiates a new open API.
     */
    public OpenAPI() {
        super();
    }

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
     * @param id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "OpenAPI{" + "id=" + id + ", location='" + location + '\'' + ", name='" + name + '\'' + ", type='" + type + '\''
                + ", description='" + description + '\'' + ", key='" + key + '\'' + ", dependencies='" + dependencies + '\''
                + ", createdBy=" + createdBy + ", createdAt=" + createdAt + ", updatedBy=" + updatedBy + ", updatedAt=" + updatedAt + '}';
    }
}
