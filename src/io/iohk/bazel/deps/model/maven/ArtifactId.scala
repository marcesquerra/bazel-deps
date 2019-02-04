package io.iohk.bazel.deps.model.maven

case class ArtifactId(
  projectDescriptor: ProjectDescriptor,
  module: Option[String],
  scalaVersion: Option[String]) {

  def projectId: String = projectDescriptor.projectId
  def packagingDescription: Option[String] = projectDescriptor.packagingDescription
  def classifier: Option[String] = projectDescriptor.classifier
  def packaging: String = projectDescriptor.packaging

  def asString: String =
    List(
      Some(projectId),
      module.map(m => s"-$m"),
      scalaVersion.map(s => s"_$s"),
      packagingDescription.map(p => s":$p"),
      classifier.map(c => s":$c")).flatten.mkString

}

