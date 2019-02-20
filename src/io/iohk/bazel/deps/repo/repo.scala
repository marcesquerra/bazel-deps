package io.iohk.bazel.deps

import io.iohk.bazel.deps.model.maven._
import coursier._
import coursier.core.Organization
import coursier.core.ModuleName
import coursier.util.Task
import scala.concurrent.ExecutionContext.Implicits.global

package object repo {

  def getAllDependencies(of: List[Coordinates.Versioned], scalaVersionSufix: String): Set[Coordinates.Versioned] =
    getAllTransitiveDependencies(of, scalaVersionSufix).flatMap{case (a, bs) => bs + a}

  private def deps(of: List[Coordinates.Versioned], scalaVersionSufix: String): Set[Coordinates.Versioned] = {
    val start = Resolution(of.map(_.asDependency))
    val repositories = Seq(
      MavenRepository("https://repo1.maven.org/maven2")
    )
    val fetch = ResolutionProcess.fetch(repositories, Cache.fetch[Task]())
    val resolution: core.Resolution = start.process.run(fetch).unsafeRun()
    val errors: Seq[((Module, String), Seq[String])] = resolution.errors
    val conflicts: Set[Dependency] = resolution.conflicts
    resolution.dependencies.map(_.asVersionedCoordinates(scalaVersionSufix)).groupBy(_.unversioned).map{case (_, ds) => ds.toList.sortBy(_.version).head}.toSet
  }

  def getTransitiveDependencies(of: Coordinates.Versioned, scalaVersionSufix: String, upgrades: Map[Coordinates.Unversioned, Coordinates.Versioned]): Set[Coordinates.Versioned] =
    deps(List(of), scalaVersionSufix).filterNot(_ == of).map{d => upgrades.get(d.unversioned).getOrElse(d) }

  def getAllTransitiveDependencies(of: List[Coordinates.Versioned], scalaVersionSufix: String): Set[(Coordinates.Versioned, Set[Coordinates.Versioned])] = {
    val off = of.maxVersions
    val upgrades = deps(off, scalaVersionSufix).map{d => d.unversioned -> d}.toMap
    off.map{v =>
      (upgrades(v.unversioned), getTransitiveDependencies(upgrades(v.unversioned), scalaVersionSufix, upgrades))
    }.toSet
  }

  implicit class VersionedOps(val v: Coordinates.Versioned) extends AnyVal {
    def asDependency: Dependency =
      Dependency(
        Module(Organization(v.group.asString), ModuleName(v.artifactId.asString)),
        v.version.asString
      )
  }

  implicit class DependencyOps(val d: Dependency) extends AnyVal {
    def asVersionedCoordinates(scalaVersionSufix: String): Coordinates.Versioned = {
      Coordinates.Versioned(
        Group(d.module.organization.value),
        ArtifactId(d.module.name.value, scalaVersionSufix),
        Version(d.version),
        None
      )
    }
  }

  implicit class VerionedListOps(val vs: List[Coordinates.Versioned]) extends AnyVal {
    def maxVersions: List[Coordinates.Versioned] =
      vs.groupBy(_.unversioned).map{case (_, ds) => ds.sortBy(_.version).last}.toList
  }
}
