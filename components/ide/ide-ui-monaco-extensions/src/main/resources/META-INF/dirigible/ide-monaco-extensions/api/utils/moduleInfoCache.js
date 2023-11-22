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
let repository = dirigibleRequire("platform/repository");
let config = dirigibleRequire("core/configurations");
let modulesParser = dirigibleRequire("ide-monaco-extensions/api/utils/modulesParser");
let suggestionsParser = dirigibleRequire("ide-monaco-extensions/api/utils/suggestionsParser");

const PATH_REGISTRY_PUBLIC = "/registry/public";
const MODULE_INFO_PREFIX = "MODULE_INFO_";
const PRIORITY_MODULES = [
    "http/response",
    "http/request",
    "db/dao",
    "db/sql",
    "db/query",
    "db/update",
    "db/database",
    "core/configurations",
    "security/user"
];

exports.get = function (moduleName) {
    let moduleInfo = loadModuleInfo(moduleName);
    try {
        let resource = repository.getResource(`${PATH_REGISTRY_PUBLIC}/${moduleName}.js`);
        let information = resource.getInformation();
        let lastModifiedAt = information.getModifiedAt().getTime();
        if (isEmptyObject(moduleInfo) || moduleInfo.lastModifiedAt < lastModifiedAt) {
            moduleInfo = {
                moduleName: moduleName,
                lastModifiedAt: lastModifiedAt,
                suggestions: suggestionsParser.parse(moduleName)
            }
            saveModuleInfo(moduleInfo);
        }
    } catch (e) {
        console.error(`Error occured ${e}`);
    }
    return moduleInfo;
};

exports.refresh = function () {
    modulesParser.getModules()
        .sort((a, b) => {
            let isPriorityModuleA = PRIORITY_MODULES.includes(a.name);
            let isPriorityModuleB = PRIORITY_MODULES.includes(b.name);
            if (isPriorityModuleA && !isPriorityModuleB) {
                return -1;
            } else if (!isPriorityModuleA && isPriorityModuleB) {
                return 1;
            }
            return 0;
        })
        .forEach(e => {
            console.debug(`Refreshing ModuleInfo: ${e.name}`);
            exports.get(e.name)
        });
    console.debug("Refreshing ModuleInfo Finished");
};

exports.clear = function () {
    let keys = config.getKeys().filter(e => e.startsWith(MODULE_INFO_PREFIX));
    keys.forEach(key => config.remove(key));
};

function loadModuleInfo(moduleName) {
    return JSON.parse(config.get(MODULE_INFO_PREFIX + moduleName, "{}"));
}

function saveModuleInfo(moduleInfo) {
    config.set(`${MODULE_INFO_PREFIX}${moduleInfo.moduleName}`, JSON.stringify(moduleInfo));
}

function isEmptyObject(obj) {
    return obj && Object.keys(obj).length === 0 && obj.constructor === Object
}
