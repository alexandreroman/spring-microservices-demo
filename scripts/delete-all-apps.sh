#!/usr/bin/env bash
cf a | grep -e "^name" -A 1000 |awk 'NR>1'| awk ' { system("cf d -r -f "$1) } '
