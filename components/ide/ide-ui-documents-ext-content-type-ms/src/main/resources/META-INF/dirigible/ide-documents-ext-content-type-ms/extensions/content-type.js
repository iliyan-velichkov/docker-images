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
let config = dirigibleRequire("core/configurations");

let DIRIGIBLE_DOCUMENTS_EXT_CONTENT_TYPE_MS_ENABLED = "DIRIGIBLE_DOCUMENTS_EXT_CONTENT_TYPE_MS_ENABLED";

function getContentType(fileName, contentType) {
	let enabled = JSON.parse(config.get(DIRIGIBLE_DOCUMENTS_EXT_CONTENT_TYPE_MS_ENABLED, "false"));
	if (enabled && typeof enabled === "boolean") {
		if (fileName.endsWith(".pptx")) {
			return "application/vnd.ms-powerpoint";
		} else if (fileName.endsWith(".docx")) {
			return "application/msword";
		} else if (fileName.endsWith(".xlsx")) {
			return "application/vnd.ms-excel";
		}
	}
	return contentType;
}

exports.getContentTypeBeforeUpload = function (fileName, contentType) {
	return getContentType(fileName, contentType);
};

exports.getContentTypeBeforeDownload = function (fileName, contentType) {
	return getContentType(fileName, contentType);
};