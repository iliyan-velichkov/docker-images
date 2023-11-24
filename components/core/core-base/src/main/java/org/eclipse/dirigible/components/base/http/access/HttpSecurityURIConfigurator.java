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
package org.eclipse.dirigible.components.base.http.access;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * The Class HttpSecurityURIConfigurator.
 */
public class HttpSecurityURIConfigurator {

    /**
     * Configure.
     *
     * @param http the http
     * @throws Exception the exception
     */
    public static void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/")
            .permitAll()
            .antMatchers("/home")
            .permitAll()
            .antMatchers("/logout")
            .permitAll()
            .antMatchers("/index-busy.html")
            .permitAll()

            .antMatchers("/stomp")
            .permitAll()

            .antMatchers("/error/**")
            .permitAll()
            .antMatchers("/error.html")
            .permitAll()

            // Public
            .antMatchers("/favicon.ico")
            .permitAll()
            .antMatchers("/public/**")
            .permitAll()
            .antMatchers("/webjars/**")
            .permitAll()

            .antMatchers("/services/core/theme/**")
            .permitAll()
            .antMatchers("/services/core/version/**")
            .permitAll()
            .antMatchers("/services/core/healthcheck/**")
            .permitAll()
            .antMatchers("/services/web/resources/**")
            .permitAll()
            .antMatchers("/services/web/resources-core/**")
            .permitAll()
            .antMatchers("/services/js/resources-core/**")
            .permitAll()
            .antMatchers("/services/integrations/**")
            .permitAll()

            .antMatchers("/actuator/**")
            .permitAll()

            // Authenticated
            .antMatchers("/services/**")
            .authenticated()
            .antMatchers("/websockets/**")
            .authenticated()
            .antMatchers("/odata/**")
            .authenticated()

            // Swagger UI
            .antMatchers("/swagger-ui/**")
            .authenticated()
            .antMatchers("/v3/api-docs/swagger-config")
            .authenticated()
            .antMatchers("/v3/api-docs/**")
            .authenticated()

            // "Developer" role required
            .antMatchers("/services/ide/**")
            .hasRole("Developer")
            .antMatchers("/websockets/ide/**")
            .hasRole("Developer")

            // Spring Boot Admin
            .antMatchers("/spring-admin/**")
            .hasRole(DirigibleRole.OPERATOR.getRoleName())

            // Deny all other requests
            .anyRequest()
            .denyAll();
    }

}
