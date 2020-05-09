workspace(name = "protopoet")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "google_bazel_common",
    sha256 = "18f266d921db1daa2ee9837343938e37fa21e0a8b6a0e43a67eda4c30f62b812",
    strip_prefix = "bazel-common-eb5c7e5d6d2c724fe410792c8be9f59130437e4a",
    urls = ["https://github.com/google/bazel-common/archive/eb5c7e5d6d2c724fe410792c8be9f59130437e4a.zip"],
)

load("@google_bazel_common//:workspace_defs.bzl", "google_common_workspace_rules")

google_common_workspace_rules()
