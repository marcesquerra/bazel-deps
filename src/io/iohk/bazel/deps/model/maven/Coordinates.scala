package io.iohk.bazel.deps.model.maven

sealed trait Coordinates {
  val group: Group
  val artifactId: ArtifactId

  def asString: String
  def unversioned: Coordinates.Unversioned
}

object Coordinates {

  case class Versioned(override group: Group, override artifactId: ArtifactId, version: Version) extends Coordinates {
    override def unversioned: Coordinates.Unversioned = Coordinates.Unversioned(group, artifactId)
    override def asString: String = s"${group.asString}:${artifactId.asString}:${version.asString}"

//    def toDependencies(l: Language): Dependencies =
//      Dependencies(Map(group ->
//        Map(ArtifactOrProject(artifact.asString) ->
//          ProjectRecord(l, Some(version), None, None, None, None, None))))
  }

  case class Unversioned(override group: Group, artifactId: ArtifactId) extends Coordinates {
    override def asString: String = s"${group.asString}:${artifactId.asString}"
    override def unversioned: Coordinates.Unversioned = this

    /** This is a bazel-safe name to use as a remote repo name */
    def toBazelRepoName(namePrefix: NamePrefix): String =
      s"${namePrefix.asString}$asString".map {
        case '.' => "_"  // todo, we should have something such that if a != b this can't be equal, but this can
        case '-' => "_"
        case ':' => "_"
        case other => other
      }
      .mkString

    /**
      * The bazel-safe target name
      */
    def toTargetName: String =
      artifact.asString.map {
        case ':' => '_'
        case o => o
      }

    def toBindingName(namePrefix: NamePrefix): String = {
      val g = group.asString.map {
        case '.' => '/'
        case o => o
      }
      s"jar/${namePrefix.asString}$g/${toTargetName}".map {
        case '.' | '-' => '_'
        case o => o
      }
    }
    def bindTarget(namePrefix: NamePrefix): String = s"//external:${toBindingName(namePrefix)}"
  }
}

//
//object MavenCoordinate {
//  def apply(s: String): MavenCoordinate =
//    parse(s) match {
//      case Validated.Valid(m) => m
//      case Validated.Invalid(NonEmptyList(msg, Nil)) => sys.error(msg)
//      case _ => sys.error("unreachable (we have only a single error)")
//    }
//
//  def parse(s: String): ValidatedNel[String, MavenCoordinate] =
//    s.split(":") match {
//      case Array(g, a, v) => Validated.valid(MavenCoordinate(MavenGroup(g), MavenArtifactId(a), Version(v)))
//      case other => Validated.invalidNel(s"expected exactly three :, got $s")
//    }
//
//  def apply(u: UnversionedCoordinate, v: Version): MavenCoordinate =
//    MavenCoordinate(u.group, u.artifact, v)
//
//  implicit def mvnCoordOrd: Ordering[MavenCoordinate] = Ordering.by { m: MavenCoordinate =>
//    (m.group.asString, m.artifact.asString, m.version)
//  }
//}


//
