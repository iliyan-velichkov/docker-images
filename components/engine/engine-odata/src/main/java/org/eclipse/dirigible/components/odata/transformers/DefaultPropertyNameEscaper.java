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
package org.eclipse.dirigible.components.odata.transformers;

/**
 * The Class DefaultPropertyNameEscaper.
 */
class DefaultPropertyNameEscaper implements ODataPropertyNameEscaper {

    /**
     * Replace unsupported olingo symbols with underscore symbol The olingo do not allow dot symbol to
     * be part of the property name.
     *
     * @param propertyName entity property name
     * @return replaced string
     * @see org.apache.olingo.odata2.core.edm.provider.EdmNamedImplProv
     */
    @Override
    public String escape(String propertyName) {
        return propertyName.replace('.', '_');
    }
}
