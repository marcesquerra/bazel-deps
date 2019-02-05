package io.iohk.bazel.deps.model.maven


//sealed abstract class Language {
//  def asString: String
//  def asOptionsString: String
//  def mavenCoord(g: MavenGroup, a: ArtifactOrProject, v: Version): MavenCoordinate
//  def mavenCoord(g: MavenGroup, a: ArtifactOrProject, sp: Subproject, v: Version): MavenCoordinate
//  def unversioned(g: MavenGroup, a: ArtifactOrProject): UnversionedCoordinate
//  def unversioned(g: MavenGroup, a: ArtifactOrProject, sp: Subproject): UnversionedCoordinate
//
//  def unmangle(m: MavenCoordinate): MavenCoordinate
//}
//
//object Language {
//  sealed trait JavaLike extends Language {
//    def asString: String
//    def asOptionsString = asString
//    def mavenCoord(g: MavenGroup, a: ArtifactOrProject, v: Version): MavenCoordinate =
//      MavenCoordinate(g, MavenArtifactId(a), v)
//
//    def mavenCoord(g: MavenGroup, a: ArtifactOrProject, sp: Subproject, v: Version): MavenCoordinate =
//      MavenCoordinate(g, MavenArtifactId(a, sp), v)
//
//    def unversioned(g: MavenGroup, a: ArtifactOrProject): UnversionedCoordinate =
//      UnversionedCoordinate(g, MavenArtifactId(a))
//
//    def unversioned(g: MavenGroup, a: ArtifactOrProject, sp: Subproject): UnversionedCoordinate =
//      UnversionedCoordinate(g, MavenArtifactId(a, sp))
//
//    def unmangle(m: MavenCoordinate) = m
//  }
//
//  case object Java extends JavaLike {
//    def asString = "java"
//  }
//
//  case object Kotlin extends JavaLike {
//    def asString = "kotlin"
//
//  }
//
//  case class Scala(v: Version, mangle: Boolean) extends Language {
//    def asString = if (mangle) "scala" else "scala/unmangled"
//    def asOptionsString: String = s"scala:${v.asString}"
//
//    val major = v.asString.split('.') match {
//      case Array("2", x) if (x.toInt >= 10) => s"2.$x"
//      case Array("2", x, _) if (x.toInt >= 10) => s"2.$x"
//      case _ => sys.error(s"unsupported scala version: ${v.asString}")
//    }
//    private val suffix = s"_$major"
//    private def add(a: MavenArtifactId): MavenArtifactId =
//      if (mangle) a.addSuffix(suffix)
//      else a
//
//    def unversioned(g: MavenGroup, a: ArtifactOrProject): UnversionedCoordinate =
//      UnversionedCoordinate(g, add(MavenArtifactId(a)))
//
//    def unversioned(g: MavenGroup, a: ArtifactOrProject, sp: Subproject): UnversionedCoordinate =
//      UnversionedCoordinate(g, add(MavenArtifactId(a, sp)))
//
//    def mavenCoord(g: MavenGroup, a: ArtifactOrProject, v: Version): MavenCoordinate =
//      MavenCoordinate(g, add(MavenArtifactId(a)), v)
//
//    def mavenCoord(g: MavenGroup, a: ArtifactOrProject, sp: Subproject, v: Version): MavenCoordinate =
//      MavenCoordinate(g, add(MavenArtifactId(a, sp)), v)
//
//    def removeSuffix(s: String): Option[String] =
//      if (s.endsWith(suffix)) Some(s.dropRight(suffix.size))
//      else None
//
//    def removeSuffix(uv: UnversionedCoordinate) : UnversionedCoordinate = {
//      val aid = uv.artifact
//      removeSuffix(aid.artifactId) match {
//        case None => uv
//        case Some(a) => UnversionedCoordinate(uv.group, MavenArtifactId(a, aid.packaging, aid.classifier))
//      }
//    }
//
//    def endsWithScalaVersion(uv: UnversionedCoordinate): Boolean =
//      uv.artifact.artifactId.endsWith(suffix)
//
//    def unmangle(m: MavenCoordinate) = {
//      val uv = m.unversioned
//      val uvWithRemoved = removeSuffix(uv)
//      if (uv == uvWithRemoved) {
//        m
//      } else {
//        MavenCoordinate(uvWithRemoved, v)
//      }
//    }
//  }
//
//  object Scala {
//    val default: Scala = Scala(Version("2.11.11"), true)
//  }
//
//  implicit val ordering: Ordering[Language] = Ordering.by(_.asString)
//}
//

//case class ProjectRecord(
//  lang: Language,
//  version: Option[Version],
//  modules: Option[Set[Subproject]],
//  exports: Option[Set[(MavenGroup, ArtifactOrProject)]],
//  exclude: Option[Set[(MavenGroup, ArtifactOrProject)]],
//  generatesApi: Option[Boolean],
//  processorClasses: Option[Set[ProcessorClass]]) {
//
//
//  // Cache this
//  override lazy val hashCode: Int =
//    (lang, version, modules, exports, exclude, processorClasses).hashCode
//
//  def flatten(ap: ArtifactOrProject): List[(ArtifactOrProject, ProjectRecord)] =
//    getModules match {
//      case Nil => List((ap, copy(modules = None)))
//      case mods => mods.map { sp =>
//        (ap.toArtifact(sp), copy(modules = None))
//      }
//    }
//
//  def normalizeEmptyModule: ProjectRecord =
//    getModules match {
//      case Subproject("") :: Nil => copy(modules = None)
//      case _ => this
//    }
//
//  def withModule(m: Subproject): ProjectRecord = modules match {
//    case None =>
//      copy(modules = Some(Set(m)))
//    case Some(subs) =>
//      // otherwise add this subproject
//      copy(modules = Some(subs + m))
//  }
//
//  def combineModules(that: ProjectRecord): Option[ProjectRecord] =
//    if ((lang == that.lang) &&
//        (version.flatMap { v => that.version.map(_ == v) }.forall(_ == true)) &&
//        (exports == that.exports) &&
//        (exclude == that.exclude)) {
//      val mods = (modules, that.modules) match {
//        case (Some(a), Some(b)) => Some(a | b)
//        case (None, s) => s.map(_ + Subproject(""))
//        case (s, None) => s.map(_ + Subproject(""))
//      }
//
//      Some(copy(modules = mods))
//    } else None
//
//  def getModules: List[Subproject] = modules.getOrElse(Set.empty).toList.sortBy(_.asString)
//
//  def versionedDependencies(g: MavenGroup,
//    ap: ArtifactOrProject): List[MavenCoordinate] =
//    version.fold(List.empty[MavenCoordinate]) { v =>
//      getModules match {
//        case Nil => List(lang.mavenCoord(g, ap, v))
//        case mods => mods.map { m => lang.mavenCoord(g, ap, m, v) }
//      }
//    }
//
//  def allDependencies(g: MavenGroup, ap: ArtifactOrProject): List[UnversionedCoordinate] =
//    getModules match {
//      case Nil => List(lang.unversioned(g, ap))
//      case mods => mods.map { m => lang.unversioned(g, ap, m) }
//    }
//
//  private def toList(s: Set[(MavenGroup, ArtifactOrProject)]): List[(MavenGroup, ArtifactOrProject)] =
//    s.toList.sortBy { case (a, b) => (a.asString, b.asString) }
//
//  def toDoc: Doc = {
//    def colonPair(a: MavenGroup, b: ArtifactOrProject): Doc =
//      quoteDoc(s"${a.asString}:${b.asString}")
//
//    def exportsDoc(e: Set[(MavenGroup, ArtifactOrProject)]): Doc =
//      if (e.isEmpty) Doc.text("[]")
//      else (Doc.line + vlist(toList(e).map { case (a, b) => colonPair(a, b) })).nested(2)
//
//    def quoteEmpty(s: String): Doc =
//      if (s.isEmpty) quoteDoc("") else Doc.text(s)
//
//    val record = List(List(("lang", Doc.text(lang.asString))),
//      version.toList.map { v => ("version", quoteDoc(v.asString)) },
//      modules.toList.map { ms =>
//        ("modules", list(ms.map(_.asString).toList.sorted)(quoteDoc)) },
//      exports.toList.map { ms =>
//        ("exports", exportsDoc(ms)) },
//      exclude.toList.map { ms =>
//        ("exclude", exportsDoc(ms)) },
//      processorClasses.toList.map { pcs =>
//        ("processorClasses", list(pcs.map(_.asString).toList.sorted)(quoteDoc)) }
//      )
//      .flatten
//      .sortBy(_._1)
//    packedYamlMap(record)
//  }
//}
//
//object ProjectRecord {
//  implicit val ordering: Ordering[ProjectRecord] = {
//    implicit def ordList[T: Ordering]: Ordering[List[T]] =
//      Ordering.by { l => (l: Iterable[T]) }
//
//    Ordering.by { pr =>
//      (pr.lang,
//        pr.version,
//        (pr.modules.fold(0)(_.size), pr.modules.map(_.map(_.asString).toList.sorted)),
//        (pr.exports.fold(0)(_.size), pr.exports.map(_.map { case (m, a) => (m.asString, a.asString) }.toList.sorted)),
//        (pr.exclude.fold(0)(_.size), pr.exclude.map(_.map { case (m, a) => (m.asString, a.asString) }.toList.sorted)))
//    }
//  }
//}
//
//case class Dependencies(toMap: Map[MavenGroup, Map[ArtifactOrProject, ProjectRecord]]) {
//
//  def flatten: Dependencies =
//    Dependencies(toMap.mapValues { map =>
//      map.toList.flatMap { case (a, p) => p.flatten(a) }.toMap
//    })
//
//  def toDoc: Doc = {
//    implicit val ordDoc: Ordering[Doc] = Ordering.by { d: Doc => d.renderWideStream.mkString }
//    val allDepDoc = toMap.toList
//      .map { case (g, map) =>
//        val parts = Dependencies.normalize(map.toList).sorted
//
//        // This is invariant should be true at the end
//        //assert(parts.flatMap { case (a, p) => p.flatten(a) }.sorted == allProj.sorted)
//
//        val groupMap = yamlMap(parts.map { case (a, p) => (a.asString, p.toDoc) })
//
//        (g.asString, groupMap)
//      }
//      .sorted
//
//    yamlMap(allDepDoc, 2)
//  }
//
//  // Returns 1 if there is exactly one candidate that matches.
//  def unversionedCoordinatesOf(g: MavenGroup, a: ArtifactOrProject): Option[UnversionedCoordinate] =
//    toMap.get(g).flatMap { ap =>
//      a.splitSubprojects match {
//        case Nil =>
//          ap.get(a).map(_.allDependencies(g, a)) match {
//            case Some(h :: Nil) => Some(h)
//            case other => None // 0 or more than one
//          }
//        case parts =>
//          // This can be split, but may not be:
//          val unsplit = ap.get(a).map(_.lang.unversioned(g, a)).toSet
//          val uvcs = unsplit.union(parts.flatMap { case (proj, subproj) =>
//            ap.get(proj)
//              .map { pr => pr.getModules.filter(_ == subproj).map((_, pr.lang)) }
//              .getOrElse(Nil)
//              .map { case (m, lang) => lang.unversioned(g, proj, m) }
//          }
//          .toSet)
//        if (uvcs.size == 1) Some(uvcs.head) else None
//      }
//    }
//
//  def exportedUnversioned(u: UnversionedCoordinate,
//    r: Replacements): Either[List[(MavenGroup, ArtifactOrProject)], List[UnversionedCoordinate]] =
//
//    recordOf(u).flatMap(_.exports) match {
//      case None => Right(Nil)
//      case Some(l) =>
//        def uv(g: MavenGroup, a: ArtifactOrProject): Option[UnversionedCoordinate] =
//          unversionedCoordinatesOf(g, a).orElse(r.unversionedCoordinatesOf(g, a))
//
//        val errs = l.filter { case (g, a) => uv(g, a).isEmpty }
//        if (errs.nonEmpty) Left(l.toList)
//        else Right(l.toList.flatMap { case (g, a) => uv(g, a) })
//    }
//
//  private val coordToProj: Map[MavenCoordinate, ProjectRecord] =
//    (for {
//      (g, m) <- toMap.iterator
//      (a, p) <- m.iterator
//      mcoord <- p.versionedDependencies(g, a)
//    } yield (mcoord -> p)).toMap
//
//  private val unversionedToProj: Map[UnversionedCoordinate, ProjectRecord] =
//    (for {
//      (g, m) <- toMap.iterator
//      (a, p) <- m.iterator
//      uv <- p.allDependencies(g, a)
//    } yield (uv -> p)).toMap
//
//  val roots: Set[MavenCoordinate] = coordToProj.keySet
//  val unversionedRoots: Set[UnversionedCoordinate] =
//    unversionedToProj.iterator
//      .collect { case (uv, pr) if pr.version.isEmpty => uv }
//      .toSet
//  /**
//   * Note, if we implement this method with an unversioned coordinate,
//   * we need to potentially remove the scala version to check the
//   * ArtifactOrProject key
//   */
//  private def recordOf(m: UnversionedCoordinate): Option[ProjectRecord] =
//    unversionedToProj.get(m)
//
//  def languageOf(m: UnversionedCoordinate): Option[Language] =
//    recordOf(m).map(_.lang)
//
//  def excludes(m: UnversionedCoordinate): Set[UnversionedCoordinate] =
//    recordOf(m).flatMap(_.exclude) match {
//      case None => Set.empty
//      case Some(uvs) =>
//        uvs.map { case (g, a) =>
//          unversionedCoordinatesOf(g, a)
//            .getOrElse(UnversionedCoordinate(g, MavenArtifactId(a)))
//        }.toSet
//    }
//}
//
//object Dependencies {
//  def empty: Dependencies = Dependencies(Map.empty[MavenGroup, Map[ArtifactOrProject, ProjectRecord]])
//
//  /**
//   * Combine as many ProjectRecords as possible into a result
//   */
//  def normalize(candidates0: List[(ArtifactOrProject, ProjectRecord)]): List[(ArtifactOrProject, ProjectRecord)] = {
//    type AP = (ArtifactOrProject, ProjectRecord)
//
//    def group[A, B](abs: List[(A, B)]): List[(A, List[B])] =
//      abs.groupBy(_._1).map { case (k, vs) => k -> vs.map(_._2) }.toList
//
//    def flatten(lp: List[AP]): List[AP] = lp.flatMap { case (a, p) => p.flatten(a) }
//
//    type CandidateGraph = List[(ArtifactOrProject, List[(ProjectRecord, List[(Subproject, AP)])])]
//
//    def apsIn(cs: CandidateGraph): Set[AP] =
//      (for {
//        (a, ps) <- cs
//        (p, saps) <- ps
//        (s, ap) <- saps
//      } yield ap).toSet
//
//    // each Artifact-project record pair is either in the final result, or it isn't. We
//    // just build all the cases now:
//    def manyWorlds(candidates: CandidateGraph, acc: List[AP]): List[List[AP]] = {
//      candidates match {
//        case Nil => List(acc)
//        case (art, Nil) :: tail => manyWorlds(tail, acc)
//        case (art, (_, Nil) :: rest) :: tail => manyWorlds((art, rest) :: tail, acc)
//        case (art, (pr, subs) :: rest) :: tail =>
//          // we consider taking (art, pr) and putting it in the result:
//          val newPR = subs.foldLeft(pr) { case (pr, (sub, _)) => pr.withModule(sub) }.normalizeEmptyModule
//
//          val finished = subs.map(_._2).toSet
//          // this ArtifactOrProject has been used, so nothing in rest is legitimate
//          // but we also need to filter to not consider items we have already added
//          val newCand =
//            tail
//              .map { case (a, ps) =>
//                val newPS =
//                  ps.map { case (pr, subs) =>
//                    (pr, subs.filterNot { case (_, ap) => finished(ap) })
//                  }
//                (a, newPS)
//              }
//
//          // this AP can't appear in others:
//          val case1 = manyWorlds(newCand, (art, newPR) :: acc)
//
//          // If we can still skip this (pr, subs) item and still
//          // find homes for all the AP pairs in subs, then try
//          def maybeRecurse(g: CandidateGraph): List[List[AP]] = {
//            val aps = subs.iterator.map(_._2)
//            val stillPaths = aps.filterNot(apsIn(g)).isEmpty
//            if (stillPaths) manyWorlds(g, acc)
//            else Nil
//          }
//
//          // or we could have not used this (art, pr) pair at all if there is
//          // something else to use it in rest:
//          // if any APs in subs don't appear in the rest this can't be successful
//          // check that before we recurse
//          val case2 = maybeRecurse((art, rest) :: tail)
//
//          // or we could have just skipped using this art entirely
//          // so that we don't exclude APs associated with it from
//          // better matches
//          // if any APs in subs don't appear in the rest this can't be successful
//          // check that before we recurse
//          val case3 = maybeRecurse(tail)
//
//          case1 reverse_::: case2 reverse_::: case3
//      }
//    }
//
//    def select(cs: CandidateGraph, inputs: Set[AP]): List[AP] = {
//      def hasAllInputs(lp: List[AP]): Boolean =
//        inputs == flatten(lp).toSet
//
//      // We want the result that has all inputs and is smallest
//      manyWorlds(cs, Nil) match {
//        case Nil => Nil
//        case nonEmpty =>
//          val minimal = nonEmpty
//            .filter(hasAllInputs _)
//            .groupBy(_.size) // there can be several variants with the same count
//            .toList
//            .minBy(_._1)
//            ._2
//          // after first taking the minimal number, we want
//          // the lowest versions first, then we want the longest
//          // prefixes
//          implicit def ordList[T: Ordering]: Ordering[List[T]] =
//            Ordering.Iterable[T].on[List[T]] { l => l }
//
//          implicit val orderingArtP: Ordering[ArtifactOrProject] =
//            Ordering.by { a: ArtifactOrProject =>
//              val str = a.asString
//              (-str.size, str)
//            }
//          // max of the sorted list gets us the longest strings, from last to front.
//          minimal.map(_.sortBy(_.swap)).min
//      }
//    }
//    // Each artifact or project has a minimal prefix
//    // they can't conflict if that minimal prefix does not conflict
//    val candidates = flatten(candidates0)
//    val splitToOrig: List[(String, CandidateGraph)] = {
//      val g0 = candidates.flatMap { case ap@(a, p) =>
//        require(p.modules == None) // this is an invariant true of candidates
//
//        // Note that previously we allowed these splits to happen at any hyphen
//        // in the artifact name, which looked nice, but resulted in terrible
//        // runtime performance (literally hours to format the file) in cases
//        // with lots of subprojects. The `take(2)` here restricts the split to
//        // happening at the first hyphen (if at all).
//        val subs = a.splitSubprojects1.toList.take(2)
//        val prefix = subs.map { case (ArtifactOrProject(MavenArtifactId(artifact, _, _)), _) => artifact }.min
//        subs.map { case (a, sp) =>
//          (prefix, (a, (p, (sp, ap))))
//        }
//      }
//      group(g0).map { case (p, as) =>
//        p -> (group(as).map { case (a, prsub) => a -> group(prsub) }.sortBy { case (_, prs) => -prs.size })
//      }
//    }
//
//    // For each prefix apply the algo
//    splitToOrig.flatMap { case (_, cg) =>
//      select(cg, apsIn(cg))
//    }
//  }
//
//  private[bazel_deps] def joinWith[F[_]: Applicative, K, A, B, C](m1: Map[K, A], m2: Map[K, B])(fn: Ior[A, B] => F[C]): F[Map[K, C]] = {
//    val allKeys = (m1.keySet | m2.keySet).toList
//    def travFn(k: K): F[(K, C)] = {
//
//      def withKey(f: F[C]): F[(K, C)] = f.map((k, _))
//
//      (m1.get(k), m2.get(k)) match {
//        case (Some(a), None) => withKey(fn(Ior.left(a)))
//        case (None, Some(b)) => withKey(fn(Ior.right(b)))
//        case (Some(a), Some(b)) => withKey(fn(Ior.both(a, b)))
//        case (None, None) => sys.error(s"somehow $k has no values in either")
//      }
//    }
//
//    val fl: F[List[(K, C)]] = allKeys.traverse(travFn)
//    fl.map(_.toMap)
//  }
//
//  private[bazel_deps] def onBoth[F[_]: Applicative, A](fn: (A, A) => F[A]): Ior[A, A] => F[A] = {
//    case Ior.Right(a) => Applicative[F].pure(a)
//    case Ior.Left(a) => Applicative[F].pure(a)
//    case Ior.Both(a1, a2) => fn(a1, a2)
//  }
//
//  def combine(vcp: VersionConflictPolicy, a: Dependencies, b: Dependencies): ValidatedNel[String, Dependencies] = {
//
//    type M1[T] = Map[MavenGroup, T]
//
//    val functor1 = Functor[M1]
//    def flatten(d: Dependencies): Dependencies = {
//      val m: Map[MavenGroup, Map[ArtifactOrProject, ProjectRecord]] =
//        functor1.map(d.toMap) { m: Map[ArtifactOrProject, ProjectRecord] =>
//          m.iterator.flatMap { case (ap, pr) => pr.flatten(ap) }.toMap
//        }
//      Dependencies(m)
//    }
//
//
//    def mergeArtifact(p1: ProjectRecord, p2: ProjectRecord): ValidatedNel[String, ProjectRecord] = {
//      (p1.version, p2.version) match {
//        case (None, None) => Validated.valid(p2) // right wins
//        case (Some(v1), Some(v2)) if v1 == v2 => Validated.valid(p2) // right wins
//        case (Some(v1), Some(v2)) =>
//          vcp.resolve(None, Set(v1, v2)).map { v =>
//            if (v == v1) p1
//            else p2
//          }
//        case (Some(v1), None) => Validated.valid(p1)
//        case (None, Some(v2)) => Validated.valid(p2)
//      }
//    }
//
//    type Artifacts = Map[ArtifactOrProject, ProjectRecord]
//    type AE[T] = ValidatedNel[String, T]
//
//    val mergeGroup: Ior[Artifacts, Artifacts] => AE[Artifacts] = {
//      val fn1: Ior[ProjectRecord, ProjectRecord] => AE[ProjectRecord] =
//        onBoth[AE, ProjectRecord](mergeArtifact(_, _))
//
//      onBoth[AE, Artifacts](joinWith[AE, ArtifactOrProject, ProjectRecord, ProjectRecord, ProjectRecord](_, _)(fn1))
//    }
//
//    val flatA = flatten(a).toMap
//    val flatB = flatten(b).toMap
//
//    joinWith[AE, MavenGroup, Artifacts, Artifacts, Artifacts](flatA, flatB)(mergeGroup)
//      .map { map => Dependencies(map.toList: _*) }
//  }
//
//  def apply(items: (MavenGroup, Map[ArtifactOrProject, ProjectRecord])*): Dependencies =
//    Dependencies(items.groupBy(_._1)
//      .map { case (g, pairs) =>
//        val finalMap = pairs.map(_._2).reduce(_ ++ _)
//        (g, finalMap)
//      }
//      .toMap)
//}

