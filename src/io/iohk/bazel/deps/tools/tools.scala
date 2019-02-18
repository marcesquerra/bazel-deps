package io.iohk.bazel.deps

import java.io.{ BufferedReader, File, FileReader }
import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

package object tools {

  private def tryToReadFile(f: File): Try[String] = Try {
    val fr = new FileReader(f)
    try {
      val buf = new BufferedReader(fr)
      val bldr = new java.lang.StringBuilder
      val cbuf = new Array[Char](1024)
      var read = 0
      while(read >= 0) {
        read = buf.read(cbuf, 0, 1024)
        if (read > 0) bldr.append(cbuf, 0, read)
      }
      Success(bldr.toString)
    }
    catch {
      case NonFatal(err) => Failure(err)
    }
    finally {
      fr.close
    }
  }.flatten

  def readFile(f: File): String =
    tryToReadFile(f) match {
      case Success(s) => s
      case Failure(_) =>
        System.err.println("FATAL: Source file could not be read")
        scala.sys.exit(1)
    }

  def readFile(f: String): String =
    readFile(new File(f))
}
