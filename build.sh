#!/bin/bash

bundle exec jekyll serve

read -p "Save local site? [yN]" yn

if [[ "$yn" = [yY]* ]]; then
    echo "Saving local _site directory!"
else
    command rm -r _site
fi
