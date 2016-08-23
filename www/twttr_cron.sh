#!/bin/bash
exists=$(crontab -l | grep -v "^(#|$)" | grep -qw "${1}"; echo $?)
if [ ${exists} = 1 ]
then
  job="*/10 * * * * ${1}"
  echo "$(crontab -l; echo "${job}")" | crontab -
fi