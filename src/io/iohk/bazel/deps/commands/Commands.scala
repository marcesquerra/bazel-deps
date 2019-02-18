package io.iohk.bazel.deps.commands

import cats.data.{ NonEmptyList, Validated, ValidatedNel }
import cats.implicits._
import com.monovore.decline.{ Argument, Command => DCommand, _ }
import java.io.File
import java.nio.file.Path

sealed abstract class Verbosity(val repr: String, val level: Int)

object Verbosity {
  case object Error extends Verbosity("error", 1)
  case object Warn extends Verbosity("warn", 2)
  case object Info extends Verbosity("info", 3)
  case object Debug extends Verbosity("debug", 4)
  case object Trace extends Verbosity("trace", 5)

  val levels: Map[String, Verbosity] =
    Map(
      "error" -> Error,
      "warn" -> Warn,
      "info" -> Info,
      "debug" -> Debug,
      "trace" -> Trace)

  private[this] val names: String =
    levels.values.toList.sortBy(_.level).map(_.repr).mkString(", ")

  val helpMessage: String =
    s"How verbose to log at (one of: $names, default: warn)."

  def errorMessage(s: String): String =
    s"Invalid verbosity level '$s'." + "\n" + s"Valid verbosity levels are: $names."

  implicit object VerbosityArgument extends Argument[Verbosity] {
    def defaultMetavar: String = "LEVEL"
    def read(s: String): ValidatedNel[String, Verbosity] =
      levels.get(s.toLowerCase) match {
        case Some(v) => Validated.valid(v)
        case None => Validated.invalidNel(errorMessage(s))
      }
  }

  val opt = Opts.option[Verbosity](
    "verbosity",
    short = "v",
    help = Verbosity.helpMessage
  ).orElse(Opts(Verbosity.Warn))
}

sealed abstract class Command {
  def verbosity: Verbosity
}

object Command {
  case class Generate(
    repoRoot: Path,
    depsFile: String,
    externalFolder: String,
    verbosity: Verbosity
  ) extends Command {
    def absDepsFile: File =
      new File(repoRoot.toFile, depsFile)
  }

  val generate = DCommand("generate", "generate transitive bazel targets") {
    val repoRoot = Opts.option[Path](
      "repo-root",
      short = "r",
      metavar = "reporoot",
      help = "the ABSOLUTE path to the root of the bazel repo")

    val depsFile = Opts.option[String](
      "deps",
      short = "d",
      metavar = "deps",
      help = "relative path to the dependencies yaml file")

    val external = Opts.option[String](
      "external",
      short = "e",
      metavar = "external",
      help = "relative path, from the root of the bazel repo, of the folder to hold the external dependencies").withDefault("3rdparty")

    (repoRoot |@| depsFile |@| external |@| Verbosity.opt).map(Generate(_, _, _, _))
  }

  val command: DCommand[Command] =
    DCommand(name = "bazel-deps", header = "a tool to manage transitive external Maven dependencies for bazel") {
      (Opts.help :: (List(generate).map(Opts.subcommand(_))))
        .reduce(_.orElse(_))
    }
}
