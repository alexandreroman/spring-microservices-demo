#!/usr/bin/env bash
cf target |grep -e "^space"|awk '{ system("cf space --guid "$2)}'
