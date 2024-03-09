#!/usr/bin/env bash
set -eu

echo "Attempting to install some files on the rpi."
echo "If prompted, enter the password 'password'."

# The remote's project_dir is set to the remote's user directory, so we can
# push relative to that.

remote user select pi
dev push authorized_keys .ssh/authorized_keys
dev push bash_profile.txt .bash_profile
dev push inputrc.txt .inputrc
dev push hush_login.txt .hushlogin
