package io.iohk.bazel.deps.model.maven

import java.security.MessageDigest


sealed trait Coordinates {
  val group: Group
  val artifactId: ArtifactId

  def asString: String
  def unversioned: Coordinates.Unversioned
  def skipJari: Boolean = artifactId.scalaVersion.isDefined
}

object Coordinates {

  case class Versioned(
    override val group: Group,
    override val artifactId: ArtifactId,
                 version: Version,
             val scope: Option[String]) extends Coordinates {
    override def unversioned: Coordinates.Unversioned = Coordinates.Unversioned(group, artifactId)
    override def asString: String = s"${group.asString}:${artifactId.asString}:${version.asString}"

    override def equals(that: Any): Boolean = {
      that match {
        case a: Versioned => this.asString == a.asString
        case _ => false
      }
    }

    override def hashCode: Int = asString.hashCode
    def url: String =
      s"https://repo1.maven.org/maven2/${group.asString.replaceAllLiterally(".", "/")}/${artifactId.asString}/${version.asString}/${artifactId.asString}-${version.asString}.jar"


    private lazy val jarContent: Array[Byte] = {
      import java.io.BufferedInputStream
      import java.io.InputStream
      import java.net.URL

      def inputStreamToByteArray(is: InputStream): Array[Byte] =
        Iterator continually is.read takeWhile (-1 !=) map (_.toByte) toArray

      val u = new URL(url)
      val uc = u.openConnection()
      val raw = uc.getInputStream()
      val in = new BufferedInputStream(raw)
      val r = inputStreamToByteArray(in)
      in.close()
      raw.close()
      r
    }

    lazy val sha256: String =
      MessageDigest.getInstance("SHA-256")
        .digest(jarContent)
        .map("%02x".format(_)).mkString
  }

  case class Unversioned(override val group: Group, override val artifactId: ArtifactId) extends Coordinates {
    override def asString: String = s"${group.asString}:${artifactId.asString}"
    def asCompactString: String = s"${group.asString}:${artifactId.asCompactString}"
    override def unversioned: Coordinates.Unversioned = this

    override def equals(that: Any): Boolean = {
      that match {
        case a: Unversioned => this.asString == a.asString
        case _ => false
      }
    }

    override def hashCode: Int = asString.hashCode

    def asBazelWorkspaceName(neverlink: Boolean): String =
      asBazelWorkspaceName(neverlink, true)

    def asBazelLabel(neverlink: Boolean, externalFolder: String): String =
      if(skipJari)
        s"//$externalFolder/jvm:${asBazelWorkspaceName(neverlink, false)}"
      else
        s"@${asBazelWorkspaceName(neverlink, false)}"

    def asBazelWorkspaceName(neverlink: Boolean, checkForExternal: Boolean): String =
      asString.flatMap {
        case '_' => "__"
        case '.' => "___"
        case ':' => "_and_"
        case '-' => "_ds_"
        case c if c.isLetter || c.isDigit => c.toString
        case otherwise => s"_0x${"%04x".format(otherwise.toInt)}"
      } + (if (checkForExternal && skipJari) "__EXTERNAL" else "") + (if (neverlink) "__NEVERLINK" else "")

  }
}

