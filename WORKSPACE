workspace(name = "protopoet")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "bazel_common",
    sha256 = "3656cb5b1a49be1077f13ba9e9cf79e690675870dc7edec8943aebaf7e94dd44",
    strip_prefix = "bazel-common-c0a6655a70fb389dbb6473989450df0c86447ec3",
    urls = ["https://github.com/google/bazel-common/archive/c0a6655a70fb389dbb6473989450df0c86447ec3.zip"],
)

load("@bazel_common//:workspace_defs.bzl", "google_common_workspace_rules")
google_common_workspace_rules()
