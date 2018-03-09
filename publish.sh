#!/bin/bash


# constants

DOC_BADGE="docs/docs-status-badge.svg"
VERSION_DATA_FILE="_data/versions.csv"
# parse command args

# these are examples of what args there should be

DOC_FOLDER="docs/unknown"
DOC_STATUS="unknown"
DOC_VERSION="unknown"

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


exit 0


# update status

command cp "images/docs-$DOC_STATUS.svg" "$DOC_BADGE"

# add new docs and image

git add "$DOC_FOLDER" "$DOC_BADGE"
git commit -m "autopublish javadoc 
