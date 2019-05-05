workspace(name = "protopoet")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "bazel_common",
    sha256 = "ccdd09559b49c7efd9e4b0b617b18e2a4bbdb2142fc30dfd3501eb5fa1294dcc",
    strip_prefix = "bazel-common-f3dc1a775d21f74fc6f4bbcf076b8af2f6261a69",
    urls = ["https://github.com/google/bazel-common/archive/f3dc1a775d21f74fc6f4bbcf076b8af2f6261a69.zip"],
)

load("@bazel_common//:workspace_defs.bzl", "google_common_workspace_rules")

google_common_workspace_rules()
