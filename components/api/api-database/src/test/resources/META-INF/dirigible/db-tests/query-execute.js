/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2022 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 * SPDX-License-Identifier: EPL-2.0
 */
var query = require('db/query');
var update = require('db/update');
var assertTrue = require('test/assert').assertTrue;

update.execute("CREATE TABLE Q (A INT, B VARCHAR(10))");
update.execute("INSERT INTO Q VALUES (1, 'ABC')");
update.execute("INSERT INTO Q VALUES (2, 'DEF')");

var sql = "SELECT * FROM Q WHERE A = ?";
var resultset = query.execute(sql, [1]);

console.log(JSON.stringify(resultset));

update.execute("DROP TABLE Q");

assertTrue((resultset !== null) && (resultset !== undefined));