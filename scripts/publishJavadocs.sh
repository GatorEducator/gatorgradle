#!/bin/bash

# constants

DOC_BADGE="docs/docs-status-badge.svg"
VERSION_DATA_FILE="_data/versions.csv"

# parse command args

# these are examples of what args there should be

DOC_FOLDER="docs/unknown"
DOC_STATUS="unknown"
DOC_VERSION="unknown"
DOC_DATE="unknown"

while [[ $# -gt 0 ]]; do
    arg="$1"
    shift;
    case $arg in
        -doc|--doc-folder )
            DOC_FOLDER="$1"
            shift;
            ;;
        -s|--status )
            DOC_STATUS="$1"
            shift;
            ;;
        -v|--version )
            DOC_VERSION="$1"
            shift;
            ;;
    esac
done


# separate semver and build
SEM_VERSION=${DOC_VERSION%-*}
BUILD_VERSION=${DOC_VERSION##*-}

echo "SEM_VERSION=$SEM_VERSION"
echo "BUILD_VERSION=$BUILD_VERSION"

# check for local changes and exit if there are any
FAIL=0
git update-index -q --ignore-submodules --refresh || ( echo "Failed to update git index" && FAIL=1 )
git diff-files --quiet --ignore-submodules || ( echo "Please commit your changes!" && FAIL=1 )
git diff-index --cached --quiet HEAD --ignore-submodules", "Please add your changes:", true, "git diff-index --cached --name-status -r --ignore-submodules HEAD")

# switch to gh-pages branch
currentBranch=`git rev-parse --abbrev-ref HEAD`

git checkout

# update status
command printf "\n\"$BUILD_VERSION\",\"$SEM_VERSION\",\"$DOC_DATE\",\"$DOC_FOLDER\"" >> $VERSION_DATA_FILE

command cp "images/docs-$DOC_STATUS.svg" "$DOC_BADGE"


# add new docs and image

git add "$DOC_FOLDER" "$DOC_BADGE"
git commit -m "autopublish javadoc for version $DOC_VERSION: $DOC_STATUS"
git push origin gh-pages

# switch back to old branch
