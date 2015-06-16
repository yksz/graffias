#!/bin/sh
cp src/graffias.groovy test

cd test
echo "Running the server."
groovy server.groovy &
pid=$!
sleep 5

echo "Test has been started."
groovy client.groovy
echo "Test has been completed."

echo "Shutting down the server."
kill $pid
