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
        "@guava//guava/src/com/google/common/base",
        "@guava//guava/src/com/google/common/collect",
    ],
)

java_library(
    name = "line_wrapper",
    srcs = ["LineWrapper.java"],
    deps = [
        "@guava//guava/src/com/google/common/base",
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
        "@guava//guava/src/com/google/common/collect",
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
        "@guava//guava/src/com/google/common/base",
    ],
)
