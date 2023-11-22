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
package org.eclipse.dirigible.integration.tests.api.java.messaging;

import org.eclipse.dirigible.components.api.messaging.MessagingFacade;

/**
 * used by <b>messaging-test.ts</b>
 */
public class MessageProducer {

    public static void asyncSendMessageToTopic(String topic, String message) {
        new Thread(() -> MessagingFacade.sendToTopic(topic, message)).start();
    }

}
