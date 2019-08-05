
load("//javatests:junit.bzl", "junit_tests")

junit_tests(
    name = "proto_writer_tests",
    srcs = ["ProtoWriterTest.java"],
    deps = [
        "//java/protopoet:proto_writer",
        "@google_bazel_common//third_party/java/guava",
        "@google_bazel_common//third_party/java/junit",
        "@google_bazel_common//third_party/java/truth",
    ],
)

junit_tests(
    name = "usage_monitor_tests",
    srcs = [
        "UsedFieldMonitorTest.java",
        "UsedNameMonitorTest.java",
    ],
    deps = [
        "//java/protopoet:usage_monitor",
        "@google_bazel_common//third_party/java/guava",
        "@google_bazel_common//third_party/java/junit",
        "@google_bazel_common//third_party/java/truth",
    ],
)

junit_tests(
    name = "protopoet_tests",
    resources = ["//javatests:testdata"],
    srcs = [
        "EnumFieldSpecTest.java",
        "EnumSpecTest.java",
        "ExtensionSpecTest.java",
        "ImportSpecTest.java",
        "MapFieldSpecTest.java",
        "MessageFieldSpecTest.java",
        "MessageSpecTest.java",
        "OneofFieldSpecTest.java",
        "OptionSpecTest.java",
        "ProtoFileTest.java",
        "ReservationSpecTest.java",
        "RpcFieldSpecTest.java",
        "ServiceSpecTest.java",
    ],
    deps = [
        ":expected_output",
        "//java/protopoet",
        "//java/protopoet:proto_writer",
        "@google_bazel_common//third_party/java/guava",
        "@google_bazel_common//third_party/java/junit",
        "@google_bazel_common//third_party/java/truth",
    ],
)

java_library(
    name = "expected_output",
    srcs = ["ExpectedOutput.java"],
    deps = [
        "//java/protopoet:proto_writer",
        "@google_bazel_common//third_party/java/guava",
        "@google_bazel_common//third_party/java/junit",
        "@google_bazel_common//third_party/java/truth",
    ],
)
