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
package org.eclipse.dirigible.components.extensions.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.dirigible.components.base.artefact.Artefact;

/**
 * The Class ExtensionPoint.
 */
@Entity
@Table(name = "DIRIGIBLE_EXTENSION_POINTS")
public class ExtensionPoint extends Artefact {

    /** The Constant ARTEFACT_TYPE. */
    public static final String ARTEFACT_TYPE = "extensionpoint";

    /** The id. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EXTENSIONPOINT_ID", nullable = false)
    private Long id;

    @Transient
    private transient Set<Extension> extensions = new HashSet<Extension>();

    /**
     * Instantiates a new extension point.
     *
     * @param location the location
     * @param name the name
     * @param description the description
     */
    public ExtensionPoint(String location, String name, String description) {
        super(location, name, ARTEFACT_TYPE, description, null);
    }

    /**
     * Instantiates a new extension point.
     */
    public ExtensionPoint() {
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
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the extensions.
     *
     * @return the extensions
     */
    public Set<Extension> getExtensions() {
        return extensions;
    }

    /**
     * Sets the extensions.
     *
     * @param extensions the extensions to set
     */
    public void setExtensions(Set<Extension> extensions) {
        this.extensions = extensions;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "ExtensionPoint [id=" + id + ", location=" + location + ", name=" + name + ", description=" + description + ", type=" + type
                + ", key=" + key + ", dependencies=" + dependencies + ", state=" + createdBy + ", createdAt=" + createdAt + ", updatedBy="
                + updatedBy + ", updatedAt=" + updatedAt + "]";
    }

}
