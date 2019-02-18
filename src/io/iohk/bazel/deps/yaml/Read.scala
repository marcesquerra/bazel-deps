package io.iohk.bazel.deps.yaml


import org.json4s._
import org.json4s.jackson._
import scala.util.control.NonFatal

trait Read[T] {

  def read(v: JValue): Either[String, T]

  def map[U](f: T => U): Read[U] = Read[U](v => read(v).map(f))


  def flatMap[U](f: T => Read[U]): Read[U] = Read[U] { v =>
    read(v) match {
      case Right(u) => f(u).read(v)
      case Left(e) => Left(e)
    }
  }

}

case class PathReader(path: List[String]) {
  def \ (field: String) : PathReader = PathReader(field :: path)
  def readField[T: Read]: Read[T] = Read[T] {v =>
    def impl(p: List[String], j: JValue): Either[String, T] = p match {
      case Nil => implicitly[Read[T]].read(j)
      case h :: t => impl(t, j \\ h)
    }
    try {
      impl(path.reverse, v)
    }
    catch {
      case NonFatal(_) =>
        Left("Error extracting field")
    }
  }
  def readFieldOpt[T: Read]: Read[Option[T]] = Read[Option[T]] {v =>
    def impl(p: List[String], j: JValue): Either[String, Option[T]] = p match {
      case Nil =>
        j match {
          case JNothing => Right(None)
          case JNull => Right(None)
          case _ =>
            implicitly[Read[T]].read(j).map(t => Some(t))
        }
      case h :: t =>
        j match {
          case JObject(fields) =>
            fields.find(_._1 == h) match {
              case Some((_, n)) => impl(t, n)
              case None => Right(None)
            }
          case otherwise =>
            Left(
            s"""|A field can only be extracted out of a JSON Object
                |Instead, we are trying to do it out of this json:
                |${ JsonMethods.pretty(otherwise) }
                |""".stripMargin)
        }
      // impl(t, j \\ h)
    }
    try {
      impl(path.reverse, v)
    }
    catch {
      case NonFatal(_) =>
        Left("Error extracting field")
    }
  }
}

object Read extends DefaultReadImplementations {
  def apply[T](f: JValue => Either[String, T]): Read[T] = new Read[T]{override def read(v: JValue): Either[String, T] = f(v)}

  def \ (field: String) : PathReader = PathReader(field :: Nil)
  def read[T: Read](v: JValue): Either[String, T] = implicitly[Read[T]].read(v)
}

sealed trait DefaultReadImplementations extends LowPriorityReadImplementations {

  implicit val StringRead: Read[String] = Read[String] {
    case JString(s) => Right(s)
    case otherwise =>
      Left(s"String can only be read from JSON Strings, but got a '$otherwise' instead")
  }

  implicit def ListFieldRead[V: Read]: Read[List[(String, V)]] = Read[List[(String, V)]] {
    case JObject(l) =>
      l.foldRight(Right(Nil) : Either[String, List[(String, V)]]){
        case ((k : String, j: JValue), Right(l)) =>
          implicitly[Read[V]].read(j).map(v => (k, v) :: l)
        case (_, Left(e)) => Left(e)
      }
    case JArray(l) =>
      val tupRead: Read[(String, V)] = Read[(String, V)] {
        case JArray(JString(k) :: j :: Nil) => implicitly[Read[V]].read(j).map(v => (k, v))
        case _ => Left("A field tuple can only be read from an array of two elements where the first is a String")
      }
      l.foldRight(Right(Nil) : Either[String, List[(String, V)]]){
        case (j, Right(l)) => tupRead.read(j).map(t => t :: l)
        case (_, Left(e)) => Left(e)
      }
    case otherwise =>
      Left(
      s"""|A list of fields can only be read from a JSObject or a JSArray
          |Instead, we are trying to convert this json into a collection of fields:
          |${ JsonMethods.pretty(otherwise) }
          |""".stripMargin)
  }

}

sealed trait LowPriorityReadImplementations {
  import scala.collection.generic.CanBuildFrom
  import scala.collection.mutable.Builder

  implicit def ListRead[T, C[_]](implicit rT: Read[T], cbf: CanBuildFrom[List[_], T, C[T]]): Read[C[T]] = Read[C[T]] {
    case JArray(elems) =>
      var builder: Either[String, Builder[T, C[T]]] = Right(cbf(elems))
      var remaining = elems
      while(remaining.nonEmpty && builder.isRight) {
        val e = remaining.head
        remaining = remaining.tail
        builder = builder.flatMap{b =>
          rT.read(e).map{t =>
            b += t
            b
          }
        }
      }
      builder.map(_.result)
    case otherwise =>
      Left(
      s"""|An scala collection can only be recovered out of a JSArray
          |Instead, we are trying to recover it out of this json:
          |${ JsonMethods.pretty(otherwise) }
          |""".stripMargin)
  }
}

object Foo {
  val tmp: Read[List[String]] = implicitly[Read[List[String]]] // Read.ListRead
}


