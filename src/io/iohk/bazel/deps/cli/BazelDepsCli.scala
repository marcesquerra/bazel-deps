package io.iohk.bazel.deps.cli

import org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY
import io.iohk.bazel.deps.commands._
import io.iohk.bazel.deps.model.plain._
import io.iohk.bazel.deps.model.maven.Coordinates
import io.iohk.bazel.deps.model.maven.Version
import io.iohk.bazel.deps.yaml._
import io.iohk.bazel.deps.tools._
import io.iohk.bazel.deps.repo._
import io.iohk.bazel.deps.templates

import java.io.PrintWriter
import java.nio.file.Path

object BazelDepsCli {

  private def write(path: Path, content: String): Unit =
    new PrintWriter(path.toFile) { write(content); close }

  def main(args: Array[String]): Unit = {
    Command.command.parse(args) match {
      case Left(help) =>
        System.err.println(help)
        if (help.errors.isEmpty) System.exit(0)
        else System.exit(1)
      case Right(command) =>
        val level = command.verbosity.repr.toUpperCase
        System.setProperty(DEFAULT_LOG_LEVEL_KEY, level)
        command match {
          case gen: Command.Generate =>
            val r = readFile(gen.depsFile)
            val yaml = r.parsedYaml
            yaml.as[Dependencies] match {
              case Right(deps) =>
                val allTransitiveDeps = getAllTransitiveDependencies(deps.mavenCoordinates, deps.scalaVersionSufix)
                val allDeps = getAllDependencies(deps.mavenCoordinates, deps.scalaVersionSufix)
                val target = gen.repoRoot / gen.externalFolder
                val scalaFolder = gen.repoRoot / "scala"

                (target / "jvm").toFile.mkdirs()
                scalaFolder.toFile.mkdirs()

                println()
                println("Going to generate the 'workspace.bzl' file")
                write(target / "BUILD", "")
                write(target / "workspace.bzl", templates.workspace(allDeps))
                println("'workspace.bzl' file generated\n")
                println("Going to generate the 'deps.bzl' file")
                write(target / "deps.bzl", templates.dependencies(allTransitiveDeps.toMap, gen.externalFolder))
                println("'deps.bzl' file generated\n")
                println("Going to generate the 'pom.xml' file")
                write(target / "pom.xml", templates.pom(deps.mavenCoordinates))
                println("'pom.xml' file generated\n")
                println("Going to generate the 'scala/rules.bzl' file")
                write(scalaFolder / "rules.bzl", templates.scala_rules(gen.externalFolder))
                write(scalaFolder / "BUILD", "")
                println("'scala/rules.bzl' file generated\n")
                println("Going to generate the 'jvm/BUILD' file")
                write(target / "jvm" / "BUILD", templates.jvmBuild(allDeps))
                println("'jvm/BUILD' file generated\n")
              case Left(err) =>
                println(err)
            }
        }
    }
  }

  implicit class PathExtension(val p: Path) extends AnyVal {
    def / (dir: String): Path = p.resolve(dir)
  }
}
