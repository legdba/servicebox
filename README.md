[![License Apache](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Circle CI](https://circleci.com/gh/legdba/servicebox-jaxrs.svg?style=shield)](https://circleci.com/gh/legdba/servicebox-jaxrs)
[![Docker Repository on Quay.io](https://quay.io/repository/legdba/servicebox-jaxrs/status "Docker Repository on Quay.io")](https://quay.io/repository/legdba/servicebox-jaxrs)
# Overview
Toolbox of HTTP services for infra and containers testing:
* HTTP echo, some with intensive CPU usage and some with delays
* HTTP service causing Java heap leak

# Usage
The application runs either as a fat Jar or as a Docker container.

## Fat Jar
Get from GIT and build:
```
git clone https://github.com/legdba/servicebox-jaxrs.git && cd servicebox-jaxrs && ./gradlew check fatJar
```
Then run the app as a fat jar:
```
java -jar build/libs/*.jar
```
Then connect to http://localhost:8080/ and see the description of the exposed services.

Display help for more details:
```
java -jar build/libs/*.jar --help
```

## Docker
Latest version is always available in Quai.io and can be used as a docker application:
```
docker run -ti -p :8080:8080 --rm=true quay.io/legdba/servicebox-jaxrs:latest
```
Help available the usual way:
```
docker run -ti -p :8080:8080 --rm=true quay.io/legdba/servicebox-jaxrs:latest --help
```

Note that in the docker registry each image is tagged with the git revision and commit of the code
used to generate the image. If you run quay.io/legdba/servicebox-jaxrs:r28-bbb4196 this is the revision 'r28'
and commit 'bbb4196'. The associated code can be seen at https://github.com/legdba/servicebox-jaxrs/commit/bbb4196
or with a 'git bbb4196' command when in the servicebox-jaxrs repo.

# Logs
Servicebox-jaxrs writes all logs to stdout as one-line logstash default JSON documents
(see https://github.com/logstash/logstash-logback-encoder). While this is super convenient for serious deployement
where a log collector (logstash or another) and ElasticSearch+Kibana is used, this is not human-friendly for basic
debugging where Kibana is not available or used.

To get human readable logs simply pipe the startup command line with "es2txt" utility (requires python 2.7+).

```
java -jar build/libs/*.jar | ./ls2txt
```

# License
This software is under Apache 2.0 license.

```
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
```
