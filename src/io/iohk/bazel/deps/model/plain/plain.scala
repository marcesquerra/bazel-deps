package io.iohk.bazel.deps.model

import io.iohk.bazel.deps.yaml._
import io.iohk.bazel.deps.model.maven._

package object plain {
  type ProjectId = String
  type GroupId = String
  type Module = String
  type Group = (GroupId, List[(ProjectId, Project)])
}

package plain {
  case class Project(version: Version, lang: String, modules: Option[List[String]], scope: Option[String])
  object Project {
    implicit val ProjectRead: Read[Project] =
      for {
        version <- (Read \ "version").readField[Version]
        lang    <- (Read \ "lang").readField[String]
        modules <- (Read \ "modules").readFieldOpt[List[String]]
        scope   <- (Read \ "scope").readFieldOpt[String]
      } yield Project(version, lang, modules, scope)
  }

  case class Dependencies(dependencies: List[Group], scalaVersionSufix: String) {
    def mavenCoordinates: List[Coordinates.Versioned] =
      for {
        (groupId, projects)                             <- dependencies
        (projectId,
         Project(version, lang, modulesOpt, scope))     <- projects
        module                                          <- modulesOpt match {
                                                             case None          => List(None)
                                                             case Some(Nil)     => List(None)
                                                             case Some(modules) =>
                                                               modules.map{
                                                                 case s if s.trim.isEmpty => None
                                                                 case s => Some(s)
                                                               }
                                                           }
      } yield {
        val projectDescriptor: ProjectDescriptor = ProjectDescriptor(projectId)
        val scalaVersion = Option(scalaVersionSufix).filter(_ => lang.toLowerCase == "scala")
        val artifactId = ArtifactId(projectDescriptor, module, scalaVersion)
        Coordinates.Versioned(
          Group(groupId), artifactId, version, scope
        )
      }
  }
  object Dependencies {
    implicit val DependenciesRead: Read[Dependencies] =
      for {
        dependencies <- (Read \ "dependencies").readField[List[Group]]
        scalaVersionSufix <- (Read \ "scalaVersionSufix").readField[String]
      } yield Dependencies(dependencies, scalaVersionSufix)
  }

}
