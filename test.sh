#!/bin/sh
cp src/graffias.groovy test

cd test
echo "Running the server."
groovy testServer.groovy &
pid=$!
sleep ${1:-5}

echo "Test has been started."
groovy testClient.groovy
echo "Test has been completed."

echo "Shutting down the server."
kill $pid
