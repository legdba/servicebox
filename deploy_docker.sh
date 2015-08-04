#!/bin/bash
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
# Build Docker from current fatJar and plublish it to the registry with
# proper version and name.
##############################################################

# Ensure that when using pipes $? is non-zero if any of the pipe commands fail
# in order to allow || exit 1 to catch it and exit.
set -o pipefail

LOCAL=NO
DRYRUN=NO
# Parse arguments
for i in "$@"
do
case $i in
    --local)
    LOCAL=YES
    shift # past argument with no value
    ;;
    --dryrun)
    DRYRUN=YES
    shift # past argument with no value
    ;;
    *)
            echo "invalid argument"
            echo "usage: [--dryrun] [--local]"
            exit 1
    ;;
esac
done
echo
echo "=== read arguments ==="
echo "LOCAL           = ${LOCAL}"
echo "DRYRUN          = ${DRYRUN}"
if [[ -n $1 ]]; then
    echo "Last line of file specified as non-opt/last argument:"
    tail -1 $1
fi

###############################################################################
# Handle dry-run mode
###############################################################################
if [ $DRYRUN == YES ]
then
    echo
    echo "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
    echo "+ DRY RUN MODE ENABLED; commands will be echoed, but no applied +"
    echo "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
    echo
fi

###############################################################################
# Functions to run commands line either with and without display,
# also handling dry-run.
###############################################################################
# Print and run
exe() {
    echo "\$ $@"
    if [ $DRYRUN == NO ]
    then
        eval "$@" || exit 1
    fi
}
# run without printing (convenient for commands with secrets)
run() {
    if [ $DRYRUN == NO ]
    then
        eval "$@" || exit 1
    fi
}

###############################################################################
# Check required variable
###############################################################################
echo
echo "=== checking environment ==="

# Check the env contains the Docker repo to push Docker image to
# use "quai.io" for quay.io
# use "hub.docker.com" for default docker hub
if [ -z "$DOCKER_REPO" ]
then
    echo "please set DOCKER_REPO"
    exit 1
fi

# Check the env contains the Docker repo login to push Docker image to
if [ -z "$DOCKER_REPO_USER" ]
then
    echo "please set DOCKER_REPO_USER"
    exit 1
fi

# Check the env contains the Docker repo token to push Docker image to
if [ -z "$DOCKER_REPO_TOKEN" ]
then
    echo "please set DOCKER_REPO_TOKEN"
    exit 1
fi

# Check the env contains the Docker repo user email to push Docker image to
if [ -z "$DOCKER_REPO_EMAIL" ]
then
    echo "please set DOCKER_REPO_EMAIL"
    exit 1
fi

###############################################################################
# Set all variables
###############################################################################
echo
echo "=== collecting facts ==="
if [ -n "$CIRCLECI" ]
then
    export ARTIFACTS_PATH=./build/libs
    export BRANCH=$CIRCLE_BRANCH
elif [ -n "$DRONE" ]
then
    export ARTIFACTS_PATH=./build/libs
    # Don't set BRANCH since CircleCI set it up already
else
    export ARTIFACTS_PATH=$(pwd)/build/libs
    export BRANCH=$(git rev-parse --abbrev-ref HEAD)
fi

export APP=$(     ./gradlew buildInfo -q | grep 'appname' | awk '{print $3}')
export VERSION=$( ./gradlew buildInfo -q | grep 'version' | awk '{print $3}')
export ARTIFACT=$(./gradlew buildInfo -q | grep 'fatjar'  | awk '{print $3}')

echo "APP=$APP"
echo "BRANCH=$BRANCH"
echo "VERSION=$VERSION"
echo "ARTIFACT=$ARTIFACT"
echo "ARTIFACTS_PATH=$ARTIFACTS_PATH"

# Display build/libs since this is often usefull for troubleshooting
echo "$ARTIFACTS_PATH content:"
ls $ARTIFACTS_PATH

###############################################################################
# Check JAR File exists and is valid, plus generate sha1
###############################################################################
echo
echo "=== checking artifacts ==="
if [ ! -f $ARTIFACTS_PATH/$ARTIFACT ]
then
    echo file not found: $ARTIFACTS_PATH/$ARTIFACT
    exit 1
fi

echo
echo "=== checking artifact is loading and displaying help ==="
exe "java -jar $ARTIFACTS_PATH/$ARTIFACT --help"

echo
echo "=== generating artifacts checksum ==="
exe "cd $ARTIFACTS_PATH/"
exe "sha1sum $ARTIFACT > $ARTIFACT.sha1"
exe "cd -"

###############################################################################
# Generate Dockerfile
###############################################################################
echo
echo "=== Generating Dockerfile and docker resources archive ==="
exe "cat docker/Dockerfile.in | sed \"s/__APP__/$(echo $APP | sed -e 's/[\/&]/\\&/g')/g\" | sed \"s/__FATJAR__/$(echo $ARTIFACT | sed -e 's/[\/&]/\\&/g')/g\" > $ARTIFACTS_PATH/Dockerfile"
exe "cp docker/* $ARTIFACTS_PATH/"

###############################################################################
# Generate Docker Image
###############################################################################
echo
echo "=== generating Docker Image(s) ==="
exe "cd $ARTIFACTS_PATH"
exe "tar cvzf $ARTIFACT.tgz $ARTIFACT $ARTIFACT.sha1 Dockerfile"
if [ $LOCAL == NO ]
then
    echo "\$ docker login --username=$DOCKER_REPO_USER --password=\$DOCKER_REPO_TOKEN --email=$DOCKER_REPO_EMAIL $DOCKER_REPO"
    run "docker login --username=$DOCKER_REPO_USER --password=$DOCKER_REPO_TOKEN --email=$DOCKER_REPO_EMAIL $DOCKER_REPO"
fi
export DOCKER_IMAGE_NAME="$DOCKER_REPO/legdba/$APP"
exe "docker build --pull=true --tag=\"$DOCKER_IMAGE_NAME\" ."
exe "docker run -ti -P $DOCKER_IMAGE_NAME --help"
exe "cd -"

###############################################################################
# Push Docker images
###############################################################################
echo
if [ $LOCAL == NO ]
then

    echo "=== pushing Docker Image(s) ==="
    
    # Push an image with a label set to the binary version
    export DOCKER_IMAGE_LABEL="$DOCKER_IMAGE_NAME:$VERSION"
    exe "docker tag -f $DOCKER_IMAGE_NAME $DOCKER_IMAGE_LABEL"
    exe "docker push $DOCKER_IMAGE_LABEL"
    
    if [ "$BRANCH" == "master" ]
    then
        # If we are on master push and image with label 'latest'
        export DOCKER_IMAGE_LABEL="$DOCKER_IMAGE_NAME:latest"
        exe "docker tag -f $DOCKER_IMAGE_NAME $DOCKER_IMAGE_LABEL"
        exe "docker push $DOCKER_IMAGE_LABEL"
    else
        # If we are NOT master push and image with label set to the branch name
        export DOCKER_IMAGE_LABEL="$DOCKER_IMAGE_NAME:$BRANCH"
        exe "docker tag -f $DOCKER_IMAGE_NAME $DOCKER_IMAGE_LABEL"
        exe "docker push $DOCKER_IMAGE_LABEL"
    fi
fi
