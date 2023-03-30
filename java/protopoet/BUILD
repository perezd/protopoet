load("@rules_jvm_external//:defs.bzl", "javadoc")

package(default_visibility = ["//javatests/protopoet:__pkg__"])

javadoc(
    name = "docs_jar",
    deps = [":protopoet"],
)

genrule(
    name = "docs",
    srcs = [":docs_jar"],
    outs = ["docs_html.zip"],
    cmd = "unzip -d $@ $<",
    visibility = ["//visibility:public"],
)

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
        "//:guava",
    ],
    visibility = ["//visibility:public"],
)

java_library(
    name = "line_wrapper",
    srcs = ["LineWrapper.java"],
    deps = [
        "//:guava",
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
        "//:guava",
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
        "//:guava",
    ],
)
