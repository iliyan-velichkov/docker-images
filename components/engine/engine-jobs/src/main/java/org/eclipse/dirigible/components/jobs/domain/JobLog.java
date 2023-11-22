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
package org.eclipse.dirigible.components.jobs.domain;

import java.sql.Timestamp;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.eclipse.dirigible.components.base.artefact.Artefact;

import com.google.gson.annotations.Expose;

/**
 * The JobLogDefinition serialization object.
 */
@Entity
@Table(name = "DIRIGIBLE_JOB_LOGS")
public class JobLog extends Artefact {

    /** The Constant JOB_LOG_STATUS_TRIGGRED. */
    public static final short JOB_LOG_STATUS_TRIGGRED = 0;

    /** The Constant JOB_LOG_STATUS_FINISHED. */
    public static final short JOB_LOG_STATUS_FINISHED = 1;

    /** The Constant JOB_LOG_STATUS_FAILED. */
    public static final short JOB_LOG_STATUS_FAILED = -1;

    /** The Constant JOB_LOG_STATUS_LOGGED. */
    public static final short JOB_LOG_STATUS_LOGGED = 2;

    /** The Constant JOB_LOG_STATUS_ERROR. */
    public static final short JOB_LOG_STATUS_ERROR = 3;

    /** The Constant JOB_LOG_STATUS_WARN. */
    public static final short JOB_LOG_STATUS_WARN = 4;

    /** The Constant JOB_LOG_STATUS_INFO. */
    public static final short JOB_LOG_STATUS_INFO = 5;

    /** The Constant JOB_LOG_STATUS_UNKNOWN. */
    public static final short JOB_LOG_STATUS_UNKNOWN = 99;

    /** The Constant ARTEFACT_TYPE. */
    public static final String ARTEFACT_TYPE = "job-log";

    /** The id. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "JOBLOG_ID", nullable = false)
    private Long id;

    /** The job name. */
    @Column(name = "JOBLOG_JOB_NAME", columnDefinition = "VARCHAR", nullable = false, length = 255)
    @Expose
    private String jobName;

    /** The handler. */
    @Column(name = "JOBLOG_HANDLER", columnDefinition = "VARCHAR", nullable = false, length = 255)
    @Expose
    private String handler;

    /** The started at. */
    @Column(name = "JOBLOG_TRIGGERED_AT", columnDefinition = "TIMESTAMP", nullable = true)
    @Expose
    private Timestamp triggeredAt;

    /** The triggered id. */
    @Column(name = "JOBLOG_TRIGGERED_ID", columnDefinition = "BIGINT", nullable = true)
    @Expose
    private Long triggeredId;

    /** The finished at. */
    @Column(name = "JOBLOG_FINISHED_AT", columnDefinition = "TIMESTAMP", nullable = true)
    @Expose
    private Timestamp finishedAt;

    /** The status. */
    @Column(name = "JOBLOG_STATUS", columnDefinition = "SMALLINT", nullable = false)
    @Expose
    private Short status;

    /** The message. */
    @Column(name = "JOBLOG_MESSAGE", columnDefinition = "VARCHAR", nullable = true, length = 2000)
    @Expose
    private String message;

    /**
     * Instantiates a new job log.
     */
    public JobLog() {
        super();
        this.type = ARTEFACT_TYPE;
    }

    /**
     * Instantiates a new job log.
     *
     * @param location the location
     * @param name the name
     * @param description the description
     * @param dependencies the dependencies
     * @param jobName the job id
     * @param handler the handler
     * @param triggeredAt the triggered at
     * @param triggeredId the triggered id
     * @param finishedAt the finished at
     * @param status the status
     * @param message the message
     */
    public JobLog(String location, String name, String description, Set<String> dependencies, String jobName, String handler,
            Timestamp triggeredAt, Long triggeredId, Timestamp finishedAt, Short status, String message) {
        super(location, name, ARTEFACT_TYPE, description, dependencies);
        this.jobName = jobName;
        this.handler = handler;
        this.triggeredAt = triggeredAt;
        this.triggeredId = triggeredId;
        this.finishedAt = finishedAt;
        this.status = status;
        this.message = message;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the job name.
     *
     * @return the job name
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * Sets the job name.
     *
     * @param jobName the new job id
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * Gets the handler.
     *
     * @return the handler
     */
    public String getHandler() {
        return handler;
    }

    /**
     * Sets the handler.
     *
     * @param handler the new handler
     */
    public void setHandler(String handler) {
        this.handler = handler;
    }

    /**
     * Gets the triggered at.
     *
     * @return the triggered at
     */
    public Timestamp getTriggeredAt() {
        return triggeredAt;
    }

    /**
     * Sets the triggered at.
     *
     * @param triggeredAt the new triggered at
     */
    public void setTriggeredAt(Timestamp triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    /**
     * Gets the triggered id.
     *
     * @return the triggered id
     */
    public Long getTriggeredId() {
        return triggeredId;
    }

    /**
     * Sets the triggered id.
     *
     * @param triggeredId the new triggered id
     */
    public void setTriggeredId(Long triggeredId) {
        this.triggeredId = triggeredId;
    }

    /**
     * Gets the finished at.
     *
     * @return the finished at
     */
    public Timestamp getFinishedAt() {
        return finishedAt;
    }

    /**
     * Sets the finished at.
     *
     * @param finishedAt the new finished at
     */
    public void setFinishedAt(Timestamp finishedAt) {
        this.finishedAt = finishedAt;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public Short getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the new status
     */
    public void setStatus(Short status) {
        this.status = status;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     *
     * @param message the new message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "JobLog{" + "id=" + id + ", jobName=" + jobName + ", handler=" + handler + ", triggeredAt=" + triggeredAt + ", triggeredId="
                + triggeredId + ", finishedAt=" + finishedAt + ", status=" + status + ", message=" + message + ", location=" + location
                + ", name=" + name + ", type=" + type + ", description=" + description + ", key=" + key + ", dependencies=" + dependencies
                + ", createdBy=" + createdBy + ", createdAt=" + createdAt + ", updatedBy=" + updatedBy + ", updatedAt=" + updatedAt + '}';
    }
}
