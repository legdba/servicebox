#!/bin/bash
###############################################################################
# Build Docker from current fatJar and plublish it to the registry with
# proper version and name.
###############################################################################

# Ensure that when using pipes $? is non-zero if any of the pipe commands fail
# in order to allow || exit 1 to catch it and exit.
set -o pipefail

###############################################################################
# Handle dry-run mode
###############################################################################
export DRYRUN=$1
if [ "$DRYRUN" != "" ]
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
    if [ "$DRYRUN" == "" ]
    then
        eval "$@" || exit 1
    fi
}
# run without printing (convenient for commands with secrets)
run() {
    if [ "$DRYRUN" == "" ]
    then
        eval "$@" || exit 1
    fi
}

###############################################################################
# Check required variable
###############################################################################

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
echo "=== Generating Dockerfile ==="
exe "cat Dockerfile.in | sed \"s/__APP__/$(echo $APP | sed -e 's/[\/&]/\\&/g')/g\" | sed \"s/__FATJAR__/$(echo $ARTIFACT | sed -e 's/[\/&]/\\&/g')/g\" > $ARTIFACTS_PATH/Dockerfile"

###############################################################################
# Generate Docker Image
###############################################################################
echo
echo "=== generating Docker Image(s) ==="
exe "cd $ARTIFACTS_PATH"
exe "tar cvzf $ARTIFACT.tgz $ARTIFACT $ARTIFACT.sha1 Dockerfile"
echo "\$ docker login --username=$DOCKER_REPO_USER --password=\$DOCKER_REPO_TOKEN --email=$DOCKER_REPO_EMAIL $DOCKER_REPO"
run "docker login --username=$DOCKER_REPO_USER --password=$DOCKER_REPO_TOKEN --email=$DOCKER_REPO_EMAIL $DOCKER_REPO"
export DOCKER_IMAGE_NAME="$DOCKER_REPO/legdba/$APP"
exe "docker build --tag=\"$DOCKER_IMAGE_NAME\" ."
exe "docker run -ti -P $DOCKER_IMAGE_NAME --help"
exe "cd -"

###############################################################################
# Push Docker images
###############################################################################
echo
echo "=== pushing Docker Image(s) ==="
export DOCKER_IMAGE_LABEL="$DOCKER_IMAGE_NAME:$VERSION"
exe "docker tag $DOCKER_IMAGE_NAME $DOCKER_IMAGE_LABEL"
exe "docker push $DOCKER_IMAGE_LABEL"

if [ "$BRANCH" != "master" ]
then
    export DOCKER_IMAGE_LABEL="$DOCKER_IMAGE_NAME:$BRANCH"
    exe "docker tag $DOCKER_IMAGE_NAME $DOCKER_IMAGE_LABEL"
    exe "docker push $DOCKER_IMAGE_LABEL"
fi

export DOCKER_IMAGE_LABEL="$DOCKER_IMAGE_NAME:latest"
exe "docker tag $DOCKER_IMAGE_NAME $DOCKER_IMAGE_LABEL"
exe "docker push $DOCKER_IMAGE_LABEL"
