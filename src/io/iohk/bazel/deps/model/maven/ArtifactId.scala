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

  def asCompactString: String =
    List(
      Some(projectId),
      module.map(m => s"-$m"),
      packagingDescription.map(p => s":$p"),
      classifier.map(c => s":$c"),
      scalaVersion.map(_ => s"%")).flatten.mkString

  override def equals(that: Any): Boolean = {
    that match {
      case a: ArtifactId => this.asString == a.asString
      case _ => false
    }
  }

  override def hashCode: Int = asString.hashCode

}

object ArtifactId {
  def apply(candidate: String, scalaVersionSufix: String): ArtifactId = {
    ProjectDescriptor(candidate) match {
      case ProjectDescriptor(projectId, packagingDescription, classifier) =>
        val (withoutScalaVersion, scalaVersion) = {
          val fullScalaVersionSufix = s"_$scalaVersionSufix"
          if(candidate.endsWith(fullScalaVersionSufix))
            (projectId.reverse.drop(fullScalaVersionSufix.length).reverse, Some(scalaVersionSufix))
          else
            (projectId, None)
        }
        val (projectDescriptorId, module) =
          withoutScalaVersion.indexOf('-') match {
            case i if i < 0 =>
              (withoutScalaVersion, None)
            case i =>
              val (h, t) = withoutScalaVersion.splitAt(i)
              (h, Some(t.tail))
          }
        ArtifactId(
          ProjectDescriptor(projectDescriptorId, packagingDescription, classifier),
          module,
          scalaVersion)
    }
  }
}
