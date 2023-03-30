#!/bin/bash

bazel build //java/protopoet:docs
rm -rf ./docs
mv ./bazel-bin/java/protopoet/docs ./