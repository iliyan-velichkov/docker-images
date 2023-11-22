/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2023 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 * SPDX-License-Identifier: EPL-2.0
 */
let documentUtils = require("ide-documents/utils/cmis/document");
let imageUtils = require("ide-documents/utils/cmis/image");

exports.resize = function (path, documents, width, height) {
    let result = [];
    for (let i = 0; i < documents.size(); i++) {
        let folder = folderUtils.getFolder(path);
        let name = documents.get(i).getName();
        if (width && height && name) {
            result.push(imageUtils.uploadImageWithResize(folder, name, documents.get(i), parseInt(width), parseInt(height)));
        } else {
            result.push(documentUtils.uploadDocument(folder, documents.get(i)));
        }
    }
    return result;
};