package io.iohk.bazel.deps.model.maven

case class ProjectDescriptor(projectId: String, packagingDescription: Option[String], classifier: Option[String]) {
  def packaging: String = packagingDescription.getOrElse(ProjectDescriptor.defaultPackaging)
}

object ProjectDescriptor {
  val defaultPackaging = "jar"

  def apply(str: String): ProjectDescriptor =
    str.split(":") match {
      case Array(pid, p, c) => ProjectDescriptor(pid, Some(p), Some(c))
      case Array(pid, p) => ProjectDescriptor(pid, Some(p), None)
      case Array(pid) => ProjectDescriptor(pid, None, None)
      case _ => sys.error(s"$str did not match expected format <artifactId>[:<packaging>[:<classifier>]]")
    }
}
