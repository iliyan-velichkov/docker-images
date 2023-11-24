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
exports.getCache = function() {
	var native = org.eclipse.dirigible.commons.config.ResourcesCache.getThemeCache();
	var cache = new Cache();
	cache.native = native;
  	return cache;
};

function Cache() {

	this.getTag = function(tag) {
		return this.native.getTag(tag);
	};

	this.setTag = function(id, tag) {
		return this.native.setTag(id, tag);
	};

	this.generateTag = function() {
		return this.native.generateTag();
	};

	this.clear = function() {
		return this.native.clear();
	};
}
