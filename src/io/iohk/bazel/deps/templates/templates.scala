package io.iohk.bazel.deps

import io.iohk.bazel.deps.model.maven.Coordinates

/*
    "-Xmx1536M",
    "-Xms1536M",
    "-Xss5M",
    "-XX:MaxMetaspaceSize=512m",
*/

package object templates {

  val defaultDefaultScalacJvmFlags: List[String] = List(
    "-Xmx1536M",
    "-Xms1536M",
    "-Xss5M",
    "-XX:MaxMetaspaceSize=512m")

  def scala_rules(target: String, defaultScalacJvmFlags: List[String] = defaultDefaultScalacJvmFlags): String =
    s"""|${notice("# ")}
        |
        |load("@io_bazel_rules_scala//scala:scala.bzl", "scala_repl", native_scala_binary = "scala_binary", native_scala_library = "scala_library", native_scala_test = "scala_test")
        |load("//$target:deps.bzl", "dependencies")
        |
        |_default_scalac_jvm_flags = [
        |${defaultScalacJvmFlags.map(f => s"""    "${f.trim}",\n""").mkString}]
        |
        |def _asNeverlink(label):
        |  if ":" in label:
        |    idx = label.rindex(":")
        |    name = label[(idx+1):]
        |    prefix = label[:idx]
        |    return "%s:%s_EXT" % (prefix,name)
        |  else:
        |    if "/" in label:
        |      idx = label.rindex("/") + 1
        |      name = label[idx:]
        |      return "%s:%s_EXT" % (label,name)
        |    else:
        |      return "%s_EXT" % (label)
        |
        |def _asNeverlinks(labels):
        |  return [_asNeverlink(label) for label in labels]
        |
        |def _scala_library_impl(name, srcs, deps, external, scalac_jvm_flags, visibility, neverlink, **kwargs):
        |    allexternal = [dep
        |             for ext in external
        |             for dep in dependencies(ext, neverlink)]
        |    realname = ("%s_EXT" % name) if neverlink else name
        |    realdeps = _asNeverlinks(deps) if neverlink else deps
        |    native_scala_library(
        |        name = realname,
        |        deps = realdeps + allexternal,
        |        exports = allexternal,
        |        srcs = native.glob(["**/*.scala"], exclude = ["test/**/*"]) if srcs == None else srcs,
        |        scalac_jvm_flags = scalac_jvm_flags,
        |        visibility = visibility,
        |        **kwargs
        |    )
        |
        |def scala_library(name, srcs = None, deps = [], external = [], scalac_jvm_flags = _default_scalac_jvm_flags, visibility = ["//visibility:public"], **kwargs):
        |    _scala_library_impl(
        |        name             = name,                   deps       = deps,
        |        external         = external,               srcs       = srcs,
        |        scalac_jvm_flags = scalac_jvm_flags,       visibility = visibility,
        |        neverlink        = False,                  **kwargs
        |    )
        |    _scala_library_impl(
        |        name             = name,                   deps       = deps,
        |        external         = external,               srcs       = srcs,
        |        scalac_jvm_flags = scalac_jvm_flags,       visibility = visibility,
        |        neverlink        = True,                   **kwargs
        |    )
        |    scala_repl(
        |        name             = "%s_repl" % name,
        |        deps             = deps + [name],
        |        scalac_jvm_flags = scalac_jvm_flags,
        |    )
        |
        |def _scala_binary_impl(name, main_class, srcs, deps, external, scalac_jvm_flags, neverlink, **kwargs):
        |    allexternal = [dep
        |             for ext in external
        |             for dep in dependencies(ext, neverlink)]
        |    realname= ("%s_EXT" % name) if neverlink else name
        |    native_scala_binary(
        |        name = realname,
        |        deps = deps + allexternal,
        |        srcs = native.glob(["**/*.scala"], exclude = ["test/**/*"]) if srcs == None else srcs,
        |        scalac_jvm_flags = scalac_jvm_flags,
        |        main_class = main_class,
        |        **kwargs
        |    )
        |
        |def scala_binary(name, main_class, srcs = None, deps = [], external = [], scalac_jvm_flags = _default_scalac_jvm_flags, **kwargs):
        |    _scala_binary_impl(
        |        name             = name,                   deps       = deps,
        |        external         = external,               srcs       = srcs,
        |        scalac_jvm_flags = scalac_jvm_flags,       main_class = main_class,
        |        neverlink        = False,                  **kwargs
        |    )
        |    _scala_binary_impl(
        |        name             = name,                   deps       = deps,
        |        external         = external,               srcs       = srcs,
        |        scalac_jvm_flags = scalac_jvm_flags,       main_class = main_class,
        |        neverlink        = True,                   **kwargs
        |    )
        |    _scala_library_impl(
        |        name = "__%s_binary_lib" % name,
        |        deps = deps,
        |        external = external,
        |        srcs = srcs,
        |        scalac_jvm_flags = scalac_jvm_flags,
        |        neverlink = False,
        |        **kwargs
        |    )
        |    scala_repl(
        |        name = "%s_repl" % name,
        |        deps = deps + ["__%s_binary_lib" % name],
        |        scalac_jvm_flags = scalac_jvm_flags,
        |    )
        |
        |def scala_test(name, srcs = None, deps = [], resources = None, scalac_jvm_flags = _default_scalac_jvm_flags, **kwargs):
        |    native_scala_test(
        |        name = name,
        |        deps = deps,
        |        resources = native.glob(["test-resources/**/*"]) if resources == None else resources,
        |        srcs = native.glob(["test/**/*.scala"]) if srcs == None else srcs,
        |        scalac_jvm_flags = scalac_jvm_flags,
        |        **kwargs
        |    )
        |""".stripMargin

  def workspace(mavenCoordinates: Set[Coordinates.Versioned]): String = {
    def toImport(v: Coordinates.Versioned, neverlink: Boolean): String =
      s"""|  java_import_external(
          |      name = "${v.unversioned.asBazelWorkspaceName(neverlink)}",
          |      licenses = ["notice"],
          |      jar_urls = [
          |          "${v.url}"
          |      ],
          |      jar_sha256 = "${v.sha256}",
          |      neverlink=${if(neverlink) "1" else "0"}
          |  )
          |""".stripMargin
    def toImports(v: Coordinates.Versioned): String = toImport(v, false) + toImport(v, true)
    s"""|${notice("# ")}
        |
        |load("@bazel_tools//tools/build_defs/repo:java.bzl", "java_import_external")
        |
        |def maven_dependencies():
        |${mavenCoordinates.map(toImports).mkString("\n")}
        |""".stripMargin
  }

  def dependencies(mavenCoordinates: Map[Coordinates.Versioned, Set[Coordinates.Versioned]]): String = {
    def toEntry(k: Coordinates.Versioned, vs: Set[Coordinates.Versioned], neverlink: Boolean): String =
      s"""  "${k.unversioned.asCompactString}": [${vs.map(_.unversioned.asBazelWorkspaceName(neverlink)).map{v => s""""@$v""""}.mkString(", ")}],"""
    s"""|${notice("# ")}
        |
        |_lookup = {
        |${mavenCoordinates.map{case (k, vs) => toEntry(k, vs, false)}.mkString("\n")}
        |}
        |
        |_lookup__NEVERLINK = {
        |${mavenCoordinates.map{case (k, vs) => toEntry(k, vs, true)}.mkString("\n")}
        |}
        |
        |def dependencies(of, neverlink):
        |  return _lookup__NEVERLINK[of] if neverlink else _lookup[of]
        |
        |""".stripMargin
  }

  def pom(mavenCoordinates: List[Coordinates.Versioned]): String = {
    def dep(v: Coordinates.Versioned): String =
      s"""|    <dependency>
          |      <groupId>${v.group.asString}</groupId>
          |      <artifactId>${v.artifactId.asString}</artifactId>
          |      <version>${v.version.asString}</version>${v.scope.map{s => s"\n      <scope>$s</scope>"}.getOrElse("")}
          |    </dependency>
          |""".stripMargin
    s"""|<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
        |  <modelVersion>4.0.0</modelVersion>
        |  <groupId>groupId</groupId>
        |  <artifactId>artifactId</artifactId>
        |  <version>1.0-SNAPSHOT</version>
        |  <name>$${project.artifactId}</name>
        |  <description>My wonderfull scala app</description>
        |  <inceptionYear>2018</inceptionYear>
        |  <licenses>
        |    <license>
        |      <name>My License</name>
        |      <url>http://....</url>
        |      <distribution>repo</distribution>
        |    </license>
        |  </licenses>
        |
        |  <properties>
        |    <maven.compiler.source>1.8</maven.compiler.source>
        |    <maven.compiler.target>1.8</maven.compiler.target>
        |    <encoding>UTF-8</encoding>
        |    <scala.version>2.12.6</scala.version>
        |    <scala.compat.version>2.12</scala.compat.version>
        |    <spec2.version>4.2.0</spec2.version>
        |  </properties>
        |
        |  <dependencies>
        |${mavenCoordinates.map(dep).mkString}
        |  </dependencies>
        |
        |  <build>
        |    <sourceDirectory>src/main/scala</sourceDirectory>
        |    <testSourceDirectory>src/test/scala</testSourceDirectory>
        |    <plugins>
        |      <plugin>
        |        <!-- see http://davidb.github.com/scala-maven-plugin -->
        |        <groupId>net.alchim31.maven</groupId>
        |        <artifactId>scala-maven-plugin</artifactId>
        |        <version>3.3.2</version>
        |        <executions>
        |          <execution>
        |            <goals>
        |              <goal>compile</goal>
        |              <goal>testCompile</goal>
        |            </goals>
        |            <configuration>
        |              <args>
        |                <arg>-dependencyfile</arg>
        |                <arg>$${project.build.directory}/.scala_dependencies</arg>
        |              </args>
        |            </configuration>
        |          </execution>
        |        </executions>
        |      </plugin>
        |      <plugin>
        |        <groupId>org.apache.maven.plugins</groupId>
        |        <artifactId>maven-surefire-plugin</artifactId>
        |        <version>2.21.0</version>
        |        <configuration>
        |          <!-- Tests will be run with scalatest-maven-plugin instead -->
        |          <skipTests>true</skipTests>
        |        </configuration>
        |      </plugin>
        |      <plugin>
        |        <groupId>org.scalatest</groupId>
        |        <artifactId>scalatest-maven-plugin</artifactId>
        |        <version>2.0.0</version>
        |        <configuration>
        |          <reportsDirectory>$${project.build.directory}/surefire-reports</reportsDirectory>
        |          <junitxml>.</junitxml>
        |          <filereports>TestSuiteReport.txt</filereports>
        |          <!-- Comma separated list of JUnit test class names to execute -->
        |          <jUnitClasses>samples.AppTest</jUnitClasses>
        |        </configuration>
        |        <executions>
        |          <execution>
        |            <id>test</id>
        |            <goals>
        |              <goal>test</goal>
        |            </goals>
        |          </execution>
        |        </executions>
        |      </plugin>
        |    </plugins>
        |  </build>
        |</project>
        |""".stripMargin
  }

  private val NOTICE =
    """|This file has been autogenerated by bazel-deps.
       |Please, DO NOT EDIT it by hand.""".stripMargin

  private def notice(comment: String): String =
    NOTICE.split("\n").map(l => comment + l).mkString("\n")

}
