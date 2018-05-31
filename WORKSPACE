workspace(name = "protopoet")

http_archive(
    name = "guava",
    url = "https://github.com/perezd/guava/archive/f8d89fb5f7720e20680cc8bd1fe16bb27072c5c6.zip",
    strip_prefix = "guava-f8d89fb5f7720e20680cc8bd1fe16bb27072c5c6",
    sha256 = "666ecd6f3522f85f83be6ef75083f199942f2e3f06b61a42e08b8fa69ed2f1e6",
)

load("@guava//:defs.bzl", "guava_src_dependencies")
guava_src_dependencies()

maven_jar(
    name = "junit_jar",
    artifact = "junit:junit:4.12",
)

bind(
    name = "junit",
    actual = "@junit_jar//jar",
)

maven_jar(
    name = "truth_jar",
    artifact = "com.google.truth:truth:0.39",
)

bind(
    name = "truth",
    actual = "@truth_jar//jar",
)
