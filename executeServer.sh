#!/bin/bash
javac -g -cp jackson/jackson-core-2.9.7.jar:jackson/jackson-databind-2.9.7.jar:jackson/jackson-annotations-2.9.7.jar:. -d bin \
    winsome/server/ServerMain.java \
    && java -cp jackson/jackson-core-2.9.7.jar:jackson/jackson-databind-2.9.7.jar:jackson/jackson-annotations-2.9.7.jar:bin \
    winsome.server.ServerMain config/serverConfig.json