[![Circle CI](https://circleci.com/gh/legdba/servicebox.svg?style=shield)](https://circleci.com/gh/legdba/servicebox)
[![Docker Repository on Quay.io](https://quay.io/repository/legdba/servicebox/status "Docker Repository on Quay.io")](https://quay.io/repository/legdba/servicebox)
# Overview
Toolbox of HTTP services for infra and containers testing:
* HTTP echo, some with intensive CPU usage and some with delays
* HTTP service causing Java heap leak

# Usage
The application runs either as a fat Jar or as a Docker container.

## Fat Jar
Get from GIT and build:
```
git clone https://github.com/legdba/servicebox.git && cd servicebox && ./gradlew check fatJar
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
docker run -ti -p :8080:8080 --rm=true quay.io/legdba/servicebox
```
Help available the usual way:
```
docker run -ti -p :8080:8080 --rm=true quay.io/legdba/servicebox --help
```
