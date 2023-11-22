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
/**
 * API Producer
 */

exports.queue = function(destination) {
	const queue = new Queue();
	queue.destination = destination;
	return queue;
};

exports.topic = function(destination) {
	const topic = new Topic();
	topic.destination = destination;
	return topic;
};

function Queue() {
	this.send = function(message) {
		org.eclipse.dirigible.components.api.messaging.MessagingFacade.sendToQueue(this.destination, message);
	};
}

function Topic() {
	this.send = function(message) {
		org.eclipse.dirigible.components.api.messaging.MessagingFacade.sendToTopic(this.destination, message);
	};
}
