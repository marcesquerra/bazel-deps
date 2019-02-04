package io.iohk.bazel.deps.model.maven

case class Version(asString: String)

object Version {
  private def isNum(c: Char): Boolean =
    ('0' <= c) && (c <= '9')
  /**
   * break a string into alternating runs of Longs and Strings
   */
  private def tokenize(s: String): List[Either[String, Long]] = {
    def append(a: List[Either[String, Long]], b: Either[List[Char], List[Char]]): List[Either[String, Long]] =
      b match {
        case Right(thisAcc) =>
          Right(thisAcc.reverse.mkString.toLong) :: a
        case Left(thisAcc) =>
          Left(thisAcc.reverse.mkString) :: a
      }

    val (acc, toAdd) =
      s.foldLeft((List.empty[Either[String, Long]], Option.empty[Either[List[Char], List[Char]]])) {
        // Here are the first characters
        case ((acc, None), c) if isNum(c) =>
          (acc, Some(Right(c :: Nil)))
        case ((acc, None), c) if !isNum(c) =>
          (acc, Some(Left(c :: Nil)))
        // Here we continue with the same type
        case ((acc, Some(Right(thisAcc))), c) if isNum(c) =>
          (acc, Some(Right(c :: thisAcc)))
        case ((acc, Some(Left(thisAcc))), c) if !isNum(c)=>
          (acc, Some(Left(c :: thisAcc)))
        // Here we switch type and add to the acc
        case ((acc, Some(r@Right(thisAcc))), c) if !isNum(c)=>
          (append(acc, r), Some(Left(c :: Nil)))
        case ((acc, Some(l@Left(thisAcc))), c) if isNum(c) =>
          (append(acc, l), Some(Right(c :: Nil)))
      }
    toAdd.fold(acc)(append(acc, _)).reverse
  }

  implicit def versionOrdering: Ordering[Version] = {
    implicit val strNumOrd: Ordering[Either[String, Long]] = new Ordering[Either[String, Long]] {
      def compare(left: Either[String, Long], right: Either[String, Long]): Int = {
        (left, right) match {
          case (Right(a), Right(b)) => java.lang.Long.compare(a, b)
          case (Right(_), Left(_)) => 1 // put non number before number (eg, "-RC" comes before 2)
          case (Left(_), Right(_)) => -1
          case (Left(a), Left(b)) => a.compareTo(b)
            val commonTokens = Set("alpha", "beta", "pre", "rc", "m")
            val al = a.toLowerCase
            val bl = b.toLowerCase
            if (commonTokens(al) && commonTokens(bl)) {
              al.compareTo(bl)
            } else a.compareTo(b)
        }
      }
    }
    // In versions, if one is a prefix of the other, and the next item is
    // not a number, it is bigger.
    @annotation.tailrec
    def prefixCompare[T: Ordering](a: List[T], b: List[T])(fn: T => Int): Int = (a, b) match {
      case (Nil, h :: tail) => fn(h)
      case (h :: tail, Nil) => -fn(h)
      case (Nil, Nil) => 0
      case (ha :: taila, hb :: tailb) =>
        val c = Ordering[T].compare(ha, hb)
        if (c == 0) prefixCompare(taila, tailb)(fn)
        else c
    }
    Ordering.by { v: Version =>
      v.asString.split("\\.|\\-") // note this is a regex
        .flatMap(tokenize)
        .toList
    }(new Ordering[List[Either[String, Long]]] {
      def compare(a: List[Either[String, Long]], b: List[Either[String, Long]]) =
        prefixCompare(a, b) {
          case Left(_) => 1 // if see a string, the shorter one is larger
          case Right(_) => -1 // if we see a number, the shorter is smaller
        }
    })
  }
}

