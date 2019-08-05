package(default_visibility = ["//visibility:public"])

java_library(
    name = "protopoet",
    srcs = [
        "Buildable.java",
        "Buildables.java",
        "EnumFieldSpec.java",
        "EnumSpec.java",
        "ExtensionSpec.java",
        "FieldRange.java",
        "FieldType.java",
        "FieldValue.java",
        "ImportSpec.java",
        "Importable.java",
        "MapFieldSpec.java",
        "MessageField.java",
        "MessageFieldSpec.java",
        "MessageSpec.java",
        "OneofFieldSpec.java",
        "OptionType.java",
        "OptionSpec.java",
        "ProtoFile.java",
        "ReservationSpec.java",
        "RpcFieldSpec.java",
        "ServiceSpec.java",
    ],
    deps = [
        ":proto_writer",
        ":usage_monitor",
        "@google_bazel_common//third_party/java/guava",
    ],
)

java_library(
    name = "line_wrapper",
    srcs = ["LineWrapper.java"],
    deps = [
        "@google_bazel_common//third_party/java/guava",
    ],
)

java_library(
    name = "usage_monitor",
    srcs = [
        "UsageException.java",
        "Useable.java",
        "UsedFieldMonitor.java",
        "UsedNameMonitor.java",
    ],
    deps = [
        "@google_bazel_common//third_party/java/guava",
    ],
)

java_library(
    name = "proto_writer",
    srcs = [
        "Emittable.java",
        "ProtoWriter.java",
    ],
    deps = [
        ":line_wrapper",
        "@google_bazel_common//third_party/java/guava",
    ],
)

# This target is used to create a combined jar of all the necessary
# subcomponents of protopoet.
java_library(
   name = "release",
   resource_jars = [
     ":libprotopoet.jar",
     ":libproto_writer.jar",
     ":libusage_monitor.jar",
     ":libline_wrapper.jar",
   ],
)
