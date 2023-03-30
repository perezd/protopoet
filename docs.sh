#!/bin/bash

bazel build //java/protopoet:docs
mv ./bazel-bin/java/protopoet/html ./docs