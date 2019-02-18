scala_library(name = "repo",
              srcs = glob(["**/*.scala"], exclude = ["test/**/*"]),
              deps = [
                "//src/io/iohk/bazel/deps/model",
                "//src/io/iohk/bazel/deps/yaml",
                "//3rdparty/jvm/io/get_coursier:coursier",
                "//3rdparty/jvm/io/get_coursier:coursier_cache",
                "//3rdparty/jvm/io/get_coursier:coursier_core",
              ],
              visibility = ["//visibility:public"])

