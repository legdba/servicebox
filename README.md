[![License Apache](https://www.brimarx.com/pub/apache2.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Circle CI](https://circleci.com/gh/legdba/servicebox-jaxrs.svg?style=shield)](https://circleci.com/gh/legdba/servicebox-jaxrs)
[![Docker Repository on Quay.io](https://quay.io/repository/legdba/servicebox-jaxrs/status "Docker Repository on Quay.io")](https://quay.io/repository/legdba/servicebox-jaxrs)
# Overview
Toolbox of HTTP services for infra and containers testing:
* HTTP echo, some with intensive CPU usage and some with delays
* HTTP service causing Java heap leak
* HTTP services with high-CPU usage, latency, and doing a sum on a counter using a backend (Redis or Cassandra)
* HTTP services to return container information (env variables, hostname)

# Usage
The application runs either as a Java app or as a Docker container.

## Java App
Get from GIT and build:
```
git clone https://github.com/legdba/servicebox-jaxrs.git && cd servicebox-jaxrs && ./gradlew check
```
Then run the app:
```
./gradlew run
```
Then connect to http://localhost:8080/ and see the description of the exposed services.

Display help for more details (requires to pass CLI arguments which is not supported by gradle):
```
./gradlew distTar
[untar the build/distributions/ tar anywhere convenient; move to the bin/ directory expendedt]
./servicebox-jaxrs --help
```

## Docker App
Latest version is always available in Quai.io and can be used as a docker application:
```
docker run -ti -p :8080:8080 --rm=true quay.io/legdba/servicebox-jaxrs:latest
```
Help available the usual way:
```
docker run -ti -p :8080:8080 --rm=true quay.io/legdba/servicebox-jaxrs:latest --help
```
JAVA_OPTS can be set from docker this way (it will erase the defaults):
```
docker run -ti -p :8080:8080 -e "JAVA_OPTS=-Xmx100m" --rm=true quay.io/legdba/servicebox-jaxrs:latest --help
```

Note that in the docker registry each image is tagged with the git revision, commit and branch name of the code
used to generate the image. If you run quay.io/legdba/servicebox-jaxrs:r28-bbb4196-master this is the revision 'r28'
and commit 'bbb4196'. The associated code can be seen at https://github.com/legdba/servicebox-jaxrs/commit/bbb4196
or with a 'git bbb4196' command when in the servicebox-jaxrs repo.

# Logs
Servicebox-jaxrs writes all logs to stdout as one-line logstash default JSON documents
(see https://github.com/logstash/logstash-logback-encoder). While this is super convenient for serious deployements
where a log collector (logstash or another) and ElasticSearch+Kibana is used, this is not human-friendly for basic
debugging where Kibana is not available or used.

To get human readable logs simply pipe the startup command line with "es2txt" utility (requires python 2.7+).

```
./gradlew run | ./ls2txt
```

Transaction logs are sent to stdout as JSON documents as well:
- Pure JSON transaction log set with logger 'transaction-logs.json' and JSON attributes for each field (all prefixed with 'txn.')
- NCSA logs in the message field of the JSON logs and set with logger 'transaction-logs.ncsa'

## Using a Backend

### Memory
This is the default. No configuration needed.

### Cassandra
To use cassandra as a backend add the following options:
```
--be-type=cassandra --be-opts='{"contactPoints":["46.101.16.49","178.62.87.192"]}'
```
Plain-text credentials can be set this way (no other credentials supported so far):
```
--be-type cassandra --be-opts '{"contactPoints":["52.88.93.64","52.89.85.132","52.89.133.153"], "authProvider":{"type":"PlainTextAuthProvider", "username":"username", "password":"p@ssword"}}'
```
Set load balancing policies:
```
--be-type cassandra --be-opts '{"contactPoints":["52.88.93.64","52.89.85.132","52.89.133.153"], "loadBalancingPolicy":{"type":"DCAwareRoundRobinPolicy","localDC":"DC_name_"}}'
```

### Redis-sentinel
To use a redis sentinel as a backend add the following options listing at least one of the sentinel nodes (ideally 2
minimum, could be more):
```
--be-type redis-sentinel --be-opts redis-sentinel://sentinel1,sentinel2,sentinel3#mymaster
```
Plain-text credentials can be set this way:
```
--be-type redis-sentinel --be-opts redis-sentinel://password@sentinel1,sentinel2,sentinel3#mymaster
```
Port default to 26379 (default sentinel port) but can be defined manually like this:
```
--be-type redis-sentinel --be-opts redis-sentinel://sentinel1:5000,sentinel2:6000,sentinel3:7000#mymaster
```

This is using the Lettuce RedisURI syntax.
For a full list of URI syntax check http://redis.paluch.biz/apidocs/index.html?com/lambdaworks/redis/RedisConnection.html

### Redis-cluster
To use a redis cluster as a backend add the following options listing at least one of the cluster nodes (ideally 2
minimum, could be more):
```
--be-type=redis-cluster --be-opts=redis://redis1,redis2,redis3
```
Plain-text credentials can be set this way:
```
--be-type=redis-cluster --be-opts=redis://password@redis1,redis2,redis3
```
Port default to 6379 (default redis port) but can be defined manually like this:
```
--be-type=redis-cluster --be-opts=redis://redis1:5000,redis2:6000,redis3:7000
```

This is using the Lettuce RedisURI syntax.
For a full list of URI syntax check http://redis.paluch.biz/apidocs/index.html?com/lambdaworks/redis/RedisConnection.html

# Exposed services

See Swagger definition and sample CURL commands at http://yourhost:8080/api/v2/swagger.yaml
See Swagger-UI at http://yourhost:8080/docs/ (mind the final '/').

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
