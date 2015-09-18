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

import com.brimarx.servicebox.model.Message;
import com.brimarx.servicebox.model.SumResult;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

public class DemoClient {
    public static void main(String[] args) {
        Client client = ClientBuilder.newClient().register(JacksonFeature.class);
        WebTarget api = client.target("http://localhost:8080/api/v2");
        WebTarget echo = api.path("/echo/{message}");
        WebTarget sum = api.path("/calc/sum/{id}/{value}");

        System.out.println(echo.resolveTemplate("message", "foo").request().get(Message.class));
        System.out.println(echo.resolveTemplate("message", "bar").request().get(Message.class));

        System.out.println(sum.resolveTemplate("id", "1").resolveTemplate("value", 1).request().get(SumResult.class));
        System.out.println(sum.resolveTemplate("id", "1").resolveTemplate("value", 1).request().get(SumResult.class));
    }
}
