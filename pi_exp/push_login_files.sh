#!/usr/bin/env bash
set -eu

HOME=/home/pi
remote user select pi
dev push bash_profile.txt .bash_profile
dev push inputrc.txt .inputrc
dev push hush_login.txt .hushlogin
