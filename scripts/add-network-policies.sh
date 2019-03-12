#!/usr/bin/env bash
cf add-network-policy gateway --destination-app time
cf add-network-policy gateway --destination-app echo
cf add-network-policy gateway --destination-app greeting
cf add-network-policy gateway --destination-app whoami
