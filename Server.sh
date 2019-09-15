#!/bin/bash
cd ../Server
javac TCPServer.java
echo Please enter the mode ?
echo mode is 1 for unencrypted chat
echo mode is 2 for encrypted chat without digest
echo mode is 3 for encrypted chat with digest
read var
java TCPServer $var