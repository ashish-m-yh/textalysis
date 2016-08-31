#!/bin/bash
exists=$(crontab -l | grep -v "^(#|$)" | grep -qw "${1}"; echo $?)
if [ ${exists} = 1 ]
then
  job="*/10 * * * * python ~/production/textalysis/www/twitter_srch.py ${1}"
  echo "$(crontab -l; echo "${job}")" | crontab -
fi
