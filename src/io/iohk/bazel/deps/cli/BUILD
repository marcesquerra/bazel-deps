load("//scala:rules.bzl", "scala_binary", "scala_test")

scala_binary(
  name = "cli",
  deps = [
    "//src/io/iohk/bazel/deps/commands",
    "//src/io/iohk/bazel/deps/repo",
    "//src/io/iohk/bazel/deps/model",
    "//src/io/iohk/bazel/deps/yaml",
    "//src/io/iohk/bazel/deps/tools",
    "//src/io/iohk/bazel/deps/templates",
  ],
  external = [ "org.slf4j:slf4j-simple" ],
  main_class = "io.iohk.bazel.deps.cli.BazelDepsCli",
)

