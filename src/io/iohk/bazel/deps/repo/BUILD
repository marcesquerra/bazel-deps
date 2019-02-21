load("//scala:rules.bzl", "scala_library", "scala_test")

scala_library(
  name = "repo",
  deps = [
    "//src/io/iohk/bazel/deps/model",
    "//src/io/iohk/bazel/deps/yaml",
  ],
  external = [
    "io.get-coursier:coursier%",
    "io.get-coursier:coursier-cache%",
    "io.get-coursier:coursier-core%",
  ],
)

