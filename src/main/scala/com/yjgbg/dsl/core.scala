package com.github.yjgbg.kepler.dsl

object core:
  type Closure[A] = A ?=> Unit
  given [A](using A):Left[A,Nothing] = Left(summon)
  given [A](using A):Right[Nothing,A] = Right(summon)
  sealed trait QEither[+A,+B,+C,+D]
  case class LL[A](a:A) extends QEither[A,Nothing,Nothing,Nothing]
  case class LR[A](a:A) extends QEither[Nothing,A,Nothing,Nothing]
  case class RL[A](a:A) extends QEither[Nothing,Nothing,A,Nothing]
  case class RR[A](a:A) extends QEither[Nothing,Nothing,Nothing,A]
  given [A](using A):LL[A] = LL(summon)
  given [A](using A):LR[A] = LR(summon)
  given [A](using A):RL[A] = RL(summon)
  given [A](using A):RR[A] = RR(summon)
  enum Scope(val value: collection.mutable.HashMap[String, Any]):
    case Root(override val value: collection.mutable.HashMap[String, Any]) extends Scope(value)
    case >>[+A <: Scope, +B](
        override val value: collection.mutable.HashMap[String, Any]
    ) extends Scope(value)
  opaque type SingleValueKey[K, S <: Scope, V] = K
  opaque type MultiValueKey[K, S <: Scope, V] = K
  opaque type SingleNodeKey[K, S <: Scope, V] = K
  opaque type MultiNodeKey[K, S <: Scope, V] = K
  object Key:
    inline def singleValueKey[K <: String & Singleton, S <: Scope, V]: SingleValueKey[K, S, V] =
      compiletime.constValue[K].asInstanceOf
    inline def multiValueKey[K <: String & Singleton, S <: Scope, V]: MultiValueKey[K, S, V] =
      compiletime.constValue[K].asInstanceOf
    inline def singleNodeKey[K <: String & Singleton, S <: Scope, V]: SingleNodeKey[K, S, V] =
      compiletime.constValue[K].asInstanceOf
    inline def multiNodeKey[K <: String & Singleton, S <: Scope, V]: MultiNodeKey[K, S, V] =
      compiletime.constValue[K].asInstanceOf
  export Scope.*
  type GetResult[A,S,V] = A match
    case LL[?] => Option[S >> V]
    case LR[?] => Seq[S >> V]
    case RL[?] => Option[V]
    case RR[?] => Seq[V]
  extension [S <: Scope](a: S)
    def toHashMap: collection.immutable.HashMap[String, Any] = a.value.view
      .mapValues(_.asInstanceOf[Matchable])
      .mapValues {
        case x: Scope => x.toHashMap
        case iterable: collection.Iterable[?] =>
          iterable.map(_.asInstanceOf[Matchable]).map {
            case x: Scope => x.toHashMap
            case other    => other
          }
        case other => other
      }
      .to(collection.immutable.HashMap)
    def get[K <: String & Singleton,V](key:K)
    (using either:QEither[SingleNodeKey[K,S,V],MultiNodeKey[K,S,V],SingleValueKey[K,S,V],MultiValueKey[K,S,V]])
    :GetResult[either.type,S,V] = 
      (either match
        case LL(value) => a.value.get(key)
        case LR(value) => a.value.get(key) match
          case None        => Nil
          case Some(value) => value.asInstanceOf[collection.mutable.Buffer[S >> V]].toSeq
        case RL(value) => a.value.get(key)
        case RR(value) => a.value.get(key) match
          case None        => Nil
          case Some(value) => value.asInstanceOf[collection.mutable.Buffer[V]].toSeq
      ).asInstanceOf
  extension [K <: String & Singleton](key:K)
    def get[V,S <: Scope]
      (using scope:S,either:QEither[SingleNodeKey[K,S,V],MultiNodeKey[K,S,V],SingleValueKey[K,S,V],MultiValueKey[K,S,V]])
      :GetResult[either.type,S,V] =scope.get(key)(using either)
  import Scope.*
  def obj(closure: Closure[Root]): Scope.Root =
    val root: Scope.Root = Scope.Root(collection.mutable.HashMap.empty)
    closure(using root)
    root
  extension [S <: Scope, K <: Singleton & String, V](key: K)
    def :=(using s:S,e:Either[SingleValueKey[K,S,V],MultiValueKey[K,S,V]])(value: e.type match
      case Left[?,?] => V
      case Right[?,?] => collection.Iterable[V]
    ) = e match
      case Left(_) => summon[S].value.put(key,value)
      case Right(_) => summon[S].value.put(key,value.asInstanceOf[collection.Iterable[?]].toBuffer)
    def +=(using S,MultiValueKey[K, S, V])(value: V): Unit =
      summon[S].value.get(key) match
        case None      => summon[S].value.put(key, collection.mutable.Buffer(value))
        case Some(seq) => seq.asInstanceOf[collection.mutable.Buffer[V]] += value
    def ++=(using S,MultiValueKey[K, S, V])(value: collection.Iterable[V]): Unit =
      summon[S].value.get(key) match
        case None      => summon[S].value.put(key, value.toBuffer)
        case Some(seq) => seq.asInstanceOf[collection.mutable.Buffer[V]] ++= value
    def apply(using s:S,either: Either[SingleNodeKey[K, S, V], MultiNodeKey[K, S, V]])(closure: Closure[S >> V]): Closure[S >> V] => Unit =
      val x: S >> V = Scope.>>[S, V](collection.mutable.HashMap.empty)
      closure(using x)
      either match
        case Left(value) => s.value.put(key, x)
        case Right(value) =>
          s.value.get(key) match
            case None        => s.value.put(key, collection.mutable.Buffer(x))
            case Some(value) => value.asInstanceOf[collection.mutable.Buffer[Any]] += x
      closure1 => closure1(using x)
