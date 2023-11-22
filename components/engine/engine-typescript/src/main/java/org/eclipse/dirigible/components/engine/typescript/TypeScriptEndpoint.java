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
package org.eclipse.dirigible.components.engine.typescript;

import org.eclipse.dirigible.components.base.endpoint.BaseEndpoint;
import org.eclipse.dirigible.components.engine.javascript.endpoint.JavascriptEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * The Class TypeScriptEndpoint.
 */
@RestController
@RequestMapping({BaseEndpoint.PREFIX_ENDPOINT_SECURED + "ts", BaseEndpoint.PREFIX_ENDPOINT_PUBLIC + "ts"})
public class TypeScriptEndpoint extends BaseEndpoint {

    /** The Constant HTTP_PATH_MATCHER. */
    private static final String HTTP_PATH_MATCHER = "/{projectName}/{*projectFilePath}";

    /** The javascript endpoint. */
    private final JavascriptEndpoint javascriptEndpoint;

    /**
     * Instantiates a new type script endpoint.
     *
     * @param javascriptEndpoint the javascript endpoint
     */
    @Autowired
    public TypeScriptEndpoint(JavascriptEndpoint javascriptEndpoint) {
        this.javascriptEndpoint = javascriptEndpoint;
    }

    /**
     * Gets the.
     *
     * @param projectName the project name
     * @param projectFilePath the project file path
     * @param params the params
     * @return the response entity
     */
    @GetMapping(HTTP_PATH_MATCHER)
    public ResponseEntity<?> get(@PathVariable("projectName") String projectName, @PathVariable("projectFilePath") String projectFilePath,
            @Nullable @RequestParam(required = false) MultiValueMap<String, String> params) {
        return javascriptEndpoint.get(projectName, replaceTSWithMJSExtension(projectFilePath), params);
    }

    /**
     * Post.
     *
     * @param projectName the project name
     * @param projectFilePath the project file path
     * @param params the params
     * @return the response entity
     */
    @PostMapping(HTTP_PATH_MATCHER)
    public ResponseEntity<?> post(@PathVariable("projectName") String projectName, @PathVariable("projectFilePath") String projectFilePath,
            @Nullable @RequestParam(required = false) MultiValueMap<String, String> params) {
        return javascriptEndpoint.post(projectName, replaceTSWithMJSExtension(projectFilePath), params);
    }

    /**
     * Post file.
     *
     * @param projectName the project name
     * @param projectFilePath the project file path
     * @param params the params
     * @param file the file
     * @return the response entity
     */
    @PostMapping(value = HTTP_PATH_MATCHER, consumes = "multipart/form-data")
    public ResponseEntity<?> postFile(@PathVariable("projectName") String projectName,
            @PathVariable("projectFilePath") String projectFilePath,
            @Nullable @RequestParam(required = false) MultiValueMap<String, String> params,
            @Validated @RequestParam("file") MultipartFile file) {
        return javascriptEndpoint.postFile(projectName, replaceTSWithMJSExtension(projectFilePath), params, file);
    }

    /**
     * Put.
     *
     * @param projectName the project name
     * @param projectFilePath the project file path
     * @param params the params
     * @return the response entity
     */
    @PutMapping(HTTP_PATH_MATCHER)
    public ResponseEntity<?> put(@PathVariable("projectName") String projectName, @PathVariable("projectFilePath") String projectFilePath,
            @Nullable @RequestParam(required = false) MultiValueMap<String, String> params) {
        return javascriptEndpoint.put(projectName, replaceTSWithMJSExtension(projectFilePath), params);
    }

    /**
     * Put file.
     *
     * @param projectName the project name
     * @param projectFilePath the project file path
     * @param params the params
     * @param file the file
     * @return the response entity
     */
    @PutMapping(value = HTTP_PATH_MATCHER, consumes = "multipart/form-data")
    public ResponseEntity<?> putFile(@PathVariable("projectName") String projectName,
            @PathVariable("projectFilePath") String projectFilePath,
            @Nullable @RequestParam(required = false) MultiValueMap<String, String> params,
            @Validated @RequestParam("file") MultipartFile file) {
        return javascriptEndpoint.putFile(projectName, replaceTSWithMJSExtension(projectFilePath), params, file);
    }

    /**
     * Patch.
     *
     * @param projectName the project name
     * @param projectFilePath the project file path
     * @param params the params
     * @return the response entity
     */
    @PatchMapping(HTTP_PATH_MATCHER)
    public ResponseEntity<?> patch(@PathVariable("projectName") String projectName, @PathVariable("projectFilePath") String projectFilePath,
            @Nullable @RequestParam(required = false) MultiValueMap<String, String> params) {
        return javascriptEndpoint.patch(projectName, replaceTSWithMJSExtension(projectFilePath), params);
    }

    /**
     * Delete.
     *
     * @param projectName the project name
     * @param projectFilePath the project file path
     * @param params the params
     * @return the response entity
     */
    @DeleteMapping(HTTP_PATH_MATCHER)
    public ResponseEntity<?> delete(@PathVariable("projectName") String projectName,
            @PathVariable("projectFilePath") String projectFilePath,
            @Nullable @RequestParam(required = false) MultiValueMap<String, String> params) {
        return javascriptEndpoint.delete(projectName, replaceTSWithMJSExtension(projectFilePath), params);
    }

    /**
     * Replace TS with MJS extension.
     *
     * @param projectFilePath the project file path
     * @return the string
     */
    private String replaceTSWithMJSExtension(String projectFilePath) {
        int indexOfExtensionStart = projectFilePath.lastIndexOf(".ts");
        if (indexOfExtensionStart == -1) {
            throw new RuntimeException("Could not find .ts extension");
        }

        String projectFilePathWithoutExtension = projectFilePath.substring(0, indexOfExtensionStart);
        String maybePathParameters = projectFilePath.substring(indexOfExtensionStart)
                                                    .replace(".ts", ""); // for decorators and rs api
        return projectFilePathWithoutExtension + ".mjs" + maybePathParameters;
    }
}
