package io.iohk.bazel.deps.model

case class MavenArtifactId(
  artifactId: String,
  packaging: String,
  classifier: Option[String]) {

  def asString: String = classifier match {
    case Some(c) => s"$artifactId:$packaging:$c"
    case None => if (packaging == MavenArtifactId.defaultPackaging) {
      artifactId
    } else {
      s"$artifactId:$packaging"
    }
  }

  def addSuffix(s: String): MavenArtifactId = MavenArtifactId(s"$artifactId$s", packaging, classifier)
}

object MavenArtifactId {
  val defaultPackaging = "jar"

  // def apply(a: ArtifactOrProject): MavenArtifactId = MavenArtifactId(a.asString)
  // def apply(a: ArtifactOrProject, s: Subproject): MavenArtifactId = MavenArtifactId(a.toArtifact(s))

  // convenience: empty string classifier converted to None
  def apply(artifact: String, packaging: String, classifier: String): MavenArtifactId = {
    assert(packaging != "")
    MavenArtifactId(
      artifact,
      packaging,
      classifier match {
        case "" => None
        case c => Some(c)
      }
    )
  }

  def apply(str: String): MavenArtifactId =
    str.split(":") match {
      case Array(a, p, c) => MavenArtifactId(a, p, Some(c))
      case Array(a, p) => MavenArtifactId(a, p, None)
      case Array(a) => MavenArtifactId(a, defaultPackaging, None)
      case _ => sys.error(s"$str did not match expected format <artifactId>[:<packaging>[:<classifier>]]")
    }
}
