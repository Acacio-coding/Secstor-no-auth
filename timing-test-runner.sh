#!/bin/bash

while true
do
  echo "Enter the N number of keys:"
  read n

  if [ $n -gt 2 ]
  then
    break
  else
    echo -e "\nN must be bigger than 2...\n"
  fi
done


while true
do
  echo -e "\nEnter the K number of keys:"
  read k

  if [ $k -le $n ] && [ $k -gt 2 ]
  then
    break
  else
    echo -e "\nK must be lesser or equal N and bigger than 2...\n"
  fi
done

declare -a keystoreconstruct

while true
do
  echo -e "\nEnter the number of keys to use in reconstruction (must be from biggest to lowest), to stop enter 0:"
  read keynumber

  isbigger=false

  if [ ${#keystoreconstruct[@]} -gt 0 ]
  then
    for ((i = 0; i < ${#keystoreconstruct[@]}; i++))
    do
      if (( keynumber >= keystoreconstruct[i] ))
      then
        echo -e "\nThe entered number is bigger from a previous entered number...\n"
        isbigger=true
        i=${#keystoreconstruct[@]}
      fi
    done
  fi

  if ! $isbigger
  then
    if [ $keynumber -eq 0 ] && [ ${#keystoreconstruct[@]} -gt 0 ]
    then
      break
    elif [ $keynumber -le $n ] && [ $keynumber -ge $k ]
    then
      keystoreconstruct+=($keynumber)
    else
      echo -e "\nThe number of keys to use in reconstruction must be lesser or equal than N and bigger or equal K and you must enter at least one valid number...\n"
    fi
  fi
done

while true
do
  echo -e "\nEnter the dataset size in kB (1, 3, 5 or 10):"
  read size

  if [ $size -eq 1 ] || [ $size -eq 3 ] || [ $size -eq 5 ] || [ $size -eq 10 ]
  then
    break
  else
    echo -e "\nThe dataset size must be either 1, 3, 5 or 10...\n"
  fi
done

while true
do
  echo -e "\nEnter the number of objects of the dataset (100 or 1000):"
  read objects

  if [ $objects -eq 100 ] || [ $objects -eq 1000 ]
  then
    break
  else
    echo -e "\nThe number of objects must be either 100 or 1000...\n"
  fi
done

dataset="${size}kB_${objects}objects_dataset_to_split.json"

while true
do
  echo -e "\nEnter the number of threads to use in the test:"
  read threads

  if [ $threads -ge 1 ]
  then
    break
  else
    echo -e "\nThe number of threads must be bigger or equal than 1...\n"
  fi
done

echo -e "\nStarting the tests using the following parameters:"
echo "N: $n"
echo "K: $k"
echo "Number of keys to use in reconstruction: ${keystoreconstruct[*]}"
echo "Dataset: $dataset"
echo -e "Number of threads: $threads\n"

mvn exec:java -Dexec.classpathScope=test -Dexec.mainClass=com.ifsc.secstor.OneByOne -Dexec.args="$n $k ${keystoreconstruct[*]} $dataset $threads"
