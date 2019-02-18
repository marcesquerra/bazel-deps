load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
scala_import(
    name = "json4s_ast_EXT",
    jars = [
        "//external:jar/org/json4s/json4s_ast_2_12"
    ],
    neverlink = 1,
    visibility = [
        "//visibility:public"
    ]
)

scala_import(
    name = "json4s_ast",
    jars = [
        "//external:jar/org/json4s/json4s_ast_2_12"
    ],
    runtime_deps = [
        "//3rdparty/jvm/org/scala_lang:scala_library"
    ],
    visibility = [
        "//visibility:public"
    ]
)



scala_import(
    name = "json4s_core_EXT",
    jars = [
        "//external:jar/org/json4s/json4s_core_2_12"
    ],
    neverlink = 1,
    visibility = [
        "//visibility:public"
    ]
)

scala_import(
    name = "json4s_core",
    jars = [
        "//external:jar/org/json4s/json4s_core_2_12"
    ],
    runtime_deps = [
        "//3rdparty/jvm/com/thoughtworks/paranamer:paranamer",
        "//3rdparty/jvm/org/scala_lang:scala_library",
        ":json4s_ast",
        ":json4s_scalap"
    ],
    visibility = [
        "//visibility:public"
    ]
)



scala_import(
    name = "json4s_jackson_EXT",
    jars = [
        "//external:jar/org/json4s/json4s_jackson_2_12"
    ],
    neverlink = 1,
    visibility = [
        "//visibility:public"
    ]
)

scala_import(
    name = "json4s_jackson",
    jars = [
        "//external:jar/org/json4s/json4s_jackson_2_12"
    ],
    runtime_deps = [
        "//3rdparty/jvm/com/fasterxml/jackson/core:jackson_databind",
        "//3rdparty/jvm/org/scala_lang:scala_library",
        ":json4s_core"
    ],
    visibility = [
        "//visibility:public"
    ]
)



scala_import(
    name = "json4s_scalap_EXT",
    jars = [
        "//external:jar/org/json4s/json4s_scalap_2_12"
    ],
    neverlink = 1,
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)

scala_import(
    name = "json4s_scalap",
    jars = [
        "//external:jar/org/json4s/json4s_scalap_2_12"
    ],
    runtime_deps = [
        "//3rdparty/jvm/org/scala_lang:scala_library"
    ],
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)


