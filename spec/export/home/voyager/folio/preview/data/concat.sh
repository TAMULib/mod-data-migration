#!/bin/bash

find $1 -mindepth 1 -maxdepth 3 -type d | while read -r dir
do
  pushd "$dir"
  # echo $dir

  files=(*.json)
  pos=$(( ${#files[*]} - 1 ))
  first=${files[0]}
  last=${files[$pos]}

  for i in "${files[@]}"
  do
    if [ "$i" != "*.json" ]
    then
      if [[ "$i" == "$first" ]]
      then
        echo "["
      fi
      cat "$i"
      if [[ "$i" != "$last" ]]
      then
        echo ","
      else
        echo "]"
      fi
    fi
  done > 000.all.jsonc

  popd
done
