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
package org.eclipse.dirigible.components.odata.config;

import org.apache.olingo.odata2.core.servlet.ODataServlet;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The Class ODataConfig.
 */
@Configuration
@EnableAutoConfiguration(exclude = LiquibaseAutoConfiguration.class)
public class ODataConfig {

    /**
     * Olingo servlet.
     *
     * @return the servlet registration bean
     */
    @Bean
    public ServletRegistrationBean<ODataServlet> olingoServlet() {
        ServletRegistrationBean<ODataServlet> bean = new ServletRegistrationBean<ODataServlet>(new ODataServlet(), "/odata/v2/*");
        bean.addInitParameter("javax.ws.rs.Application", "org.apache.olingo.odata2.core.rest.app.ODataApplication");
        bean.addInitParameter("org.apache.olingo.odata2.service.factory",
                "org.eclipse.dirigible.components.odata.factory.DirigibleODataServiceFactory");
        bean.setLoadOnStartup(1);
        return bean;
    }

}
