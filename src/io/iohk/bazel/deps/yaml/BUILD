scala_library(
  name = "yaml",
  srcs = glob(["**/*.scala"], exclude = ["test/**/*"]),
  deps = [
    "//3rdparty/jvm/org/json4s:json4s_jackson",
    "//3rdparty/jvm/org/json4s:json4s_ast",
    "//3rdparty/jvm/org/json4s:json4s_core",
    "//3rdparty/jvm/com/fasterxml/jackson/core:jackson_core",
    "//3rdparty/jvm/com/fasterxml/jackson/core:jackson_databind",
    "//3rdparty/jvm/com/fasterxml/jackson/dataformat:jackson_dataformat_yaml"
  ],
  visibility = ["//visibility:public"])

