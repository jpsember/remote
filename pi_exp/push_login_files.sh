#!/usr/bin/env bash
set -eu

HOME=/home/pi
remote user select pi
dev push bash_profile.txt ${HOME}/.bash_profile
dev push inputrc.txt ${HOME}/.inputrc
dev push hush_login.txt ${HOME}/.hushlogin
