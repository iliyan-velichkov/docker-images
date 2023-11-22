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
package org.eclipse.dirigible.components.data.management.format;

import java.util.List;

/**
 * The Interface HeaderFormatter.
 *
 */
public interface HeaderFormatter {

    /**
     * Write the header for the provided ResultSet.
     *
     * @param columns the columns
     * @return the result
     */
    String write(List<ColumnDescriptor> columns);
}
