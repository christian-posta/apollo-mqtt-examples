## Overview

Apache Apollo is a multi-protocol, high-performance, messaging server which the
    MQTT protocol which is a binary, wire-level protocol optimized for limited-resource devices on unreliable
    networks. These examples show some basic useage scenarios with the Java-based client lib,
    mqtt-client which can be found at: [https://github.com/fusesource/mqtt-client](https://github.com/fusesource/mqtt-client)

## Prereqs

- Install Java SDK
- Install [Maven](http://maven.apache.org/download.html)

## Building

Run:

    mvn install

## Running the Examples

All of these examples require an externally running Apollo broker with a tcp connector
listening on port 61613