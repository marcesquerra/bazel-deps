package io.iohk.bazel.deps.templates
package runner

import io.iohk.bazel.deps.model.plain._
import io.iohk.bazel.deps.model.maven.Coordinates
import io.iohk.bazel.deps.model.maven.Version
import io.iohk.bazel.deps.yaml._
import io.iohk.bazel.deps.tools._
import io.iohk.bazel.deps.repo._

object Runner extends App {
  println()
  //println(scala_rules("external"))

  val f = "/Development/iohk/bazel-deps/foo.yaml"
  val r = readFile(f)
  val yaml = r.parsedYaml
  yaml.as[Dependencies] match {
    case Right(deps) =>
      val allTransitiveDeps = getAllTransitiveDependencies(deps.mavenCoordinates, deps.scalaVersionSufix)
      val allDeps = getAllDependencies(deps.mavenCoordinates, deps.scalaVersionSufix)
      //println(workspace(allDeps))
      //println(dependencies(allTransitiveDeps.toMap))
      println(pom(deps.mavenCoordinates))
    case Left(err) =>
      println(err)
  }
}
