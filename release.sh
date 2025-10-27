#!/usr/bin/env bash
echo "Building and releasing, setup GITHUB_TOKEN!"
sbt "release with-defaults"