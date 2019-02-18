load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
scala_import(
    name = "decline_EXT",
    jars = [
        "//external:jar/com/monovore/decline_2_12"
    ],
    neverlink = 1,
    visibility = [
        "//visibility:public"
    ]
)

scala_import(
    name = "decline",
    jars = [
        "//external:jar/com/monovore/decline_2_12"
    ],
    runtime_deps = [
        "//3rdparty/jvm/org/scala_lang:scala_library",
        "//3rdparty/jvm/org/typelevel:cats_core"
    ],
    visibility = [
        "//visibility:public"
    ]
)


