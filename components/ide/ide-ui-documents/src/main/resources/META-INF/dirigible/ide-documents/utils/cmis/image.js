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
let streams = require("io/streams");
let imageIO = require("io/image");
let documentUtils = require("ide-documents/utils/cmis/document");

exports.uploadImageWithResize = function (folder, name, image, width, height) {
    let mimetype = image.getContentType();
    let originalInputStream = image.getInputStream();
    let inputStream = new streams.InputStream();
    inputStream.uuid = originalInputStream.uuid;

    let imageType = mimetype.split('/')[1];

    let resizedInputStream = imageIO.resize(inputStream, imageType, width, height);

    image.getInputStream = function () {
        return resizedInputStream;//new streams.InputStream(fis);
    }

    documentUtils.uploadDocument(folder, image);
}