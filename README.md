[![Circle CI](https://circleci.com/gh/legdba/servicebox.svg?style=svg)](https://circleci.com/gh/legdba/servicebox)
# Overview
Toolbox of HTTP services for infra and containers testing:
* HTTP echo, some with intensive CPU usage and some with delays
* HTTP service causing Java heap leak

# Usage
Run with
‘‘‘
./gradlew run
‘‘‘

Then connect to http://localhost:8080/ and see the exposed services
