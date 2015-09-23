/**
 ##############################################################
 # Licensed to the Apache Software Foundation (ASF) under one
 # or more contributor license agreements.  See the NOTICE file
 # distributed with this work for additional information
 # regarding copyright ownership.  The ASF licenses this file
 # to you under the Apache License, Version 2.0 (the
 # "License"); you may not use this file except in compliance
 # with the License.  You may obtain a copy of the License at
 #
 #   http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing,
 # software distributed under the License is distributed on an
 # "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 # KIND, either express or implied.  See the License for the
 # specific language governing permissions and limitations
 # under the License.
 ##############################################################
 */
package com.brimarx.servicebox;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jetty.util.component.LifeCycle.Listener;

import java.text.SimpleDateFormat;

public class RequestLogAdapter implements RequestLog {

    public RequestLogAdapter() {
    }

    private static Object def(Object o) {
        if (o == null) {
            return "-";
        } else {
            return o;
        }
    }

    public void log(Request request, Response response) {
        SimpleDateFormat sdf = new SimpleDateFormat("[dd/M/YYYY:HH:mm:ss Z]");
        StringBuilder sb = new StringBuilder();
        sb
                .append(def(request.getRemoteAddr()))             // remote client IP
                .append('\t')
                .append(def(request.getRemoteUser()))             // user-identifier; not sure how to get it
                .append('\t')
                .append(def(request.getUserIdentity()))           // userid
                .append('\t')
                .append(sdf.format(System.currentTimeMillis()))   // response timestamp (now)
                .append('\t')
                .append('"').append(def(request.getMethod())).append(' ').append(request.getRequestURI()).append(' ').append(request.getHttpVersion())
                .append('\t')
                .append(response.getStatus())
                .append('\t')
                .append(response.getContentCount())
                ;
        ncsaLogger.info(sb.toString());
    }

    public void start() throws Exception {
        isRunning = true;
    }

    public void stop() throws Exception {
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isStarted() {
        return isRunning;
    }

    public boolean isStarting() {
        return false;
    }

    public boolean isStopping() {
        return false;
    }

    public boolean isStopped() {
        return !isRunning;
    }

    public boolean isFailed() {
        return false;
    }

    public void addLifeCycleListener(Listener listener) {
    }

    public void removeLifeCycleListener(Listener listener) {
    }

    private boolean isRunning = false;
    private static final Logger ncsaLogger = LoggerFactory.getLogger("transaction-logs.http.NCSA");
}
