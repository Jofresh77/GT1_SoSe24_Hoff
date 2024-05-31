#!/bin/bash

JAR_FILE="out/artifacts/Project1_GT1_SoSe241/Project1_GT1_SoSe24.jar"
JAVA_COMMAND="java -jar"

for ((iteration=1; iteration<=1; iteration++))
do
  echo "Iteration $iteration: Starting at $(date)"
  $JAVA_COMMAND "$JAR_FILE" > /dev/null 2>&1 &

  (sleep 10 && echo "Iteration $iteration: Finished at $(date)") &

  sleep_pid=$!

  wait $sleep_pid
done