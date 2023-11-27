package com.github.yjgbg.kepler.dsl

object core:
  given [A](using a: A): Left[A, Nothing] = Left(a)
  given [A](using a: A): Right[Nothing, A] = Right(a)
  given [A](using a: A): Some[A] = Some(a)
  enum Scope(private[core] val value: collection.mutable.HashMap[String, Any]):
    case Root(private[core] override val value: collection.mutable.HashMap[String, Any]) extends Scope(value)
    case >>[+A <: Scope, +B](
        private[core] override val value: collection.mutable.HashMap[String, Any]
    ) extends Scope(value)
  export Scope.*
  extension [S <: Scope](a: S)
    def toHashMap: collection.immutable.HashMap[String, Any] = a.value.view
      .mapValues(_.asInstanceOf[Matchable])
      .mapValues {
        case x: Scope => x.toHashMap
        case iterable: scala.collection.Iterable[?] =>
          iterable.map(_.asInstanceOf[Matchable]).map {
            case x: Scope => x.toHashMap
            case other    => other
          }
        case other => other
      }
      .to(collection.immutable.HashMap)
    def getSingleNode[Key <: String & Singleton, V](key: Key)(using
        SingleNodeKey[Key, S, V]
    ): Option[S >> V] =
      a.value.get(key).asInstanceOf
    def getMultiNode[Key <: String & Singleton, V](key: Key)(using
        MultiNodeKey[Key, S, V]
    ): Seq[S >> V] =
      a.value.get(key) match
        case None        => Nil
        case Some(value) => value.asInstanceOf[collection.mutable.Buffer[S >> V]].toSeq
    def getSingleValue[Key <: String & Singleton, V](key: Key)(using
        SingleValueKey[Key, S, V]
    ): Option[V] =
      a.value.get(key).asInstanceOf
    def getMultiValue[Key <: String & Singleton, V](key: Key)(using
        MultiValueKey[Key, S, V]
    ): Seq[V] =
      a.value.get(key) match
        case None        => Nil
        case Some(value) => value.asInstanceOf[collection.mutable.Buffer[V]].toSeq
  opaque type SingleValueKey[Key, S <: Scope, V] = Key
  opaque type MultiValueKey[Key, S <: Scope, V] = Key
  opaque type SingleNodeKey[Key, S <: Scope, V] = Key
  opaque type MultiNodeKey[Key, S <: Scope, V] = Key
  object Key:
    inline def singleValueKey[Key <: String & Singleton, S <: Scope, V]: SingleValueKey[Key, S, V] =
      compiletime.constValue[Key].asInstanceOf
    inline def multiValueKey[Key <: String & Singleton, S <: Scope, V]: MultiValueKey[Key, S, V] =
      compiletime.constValue[Key].asInstanceOf
    inline def singleNodeKey[Key <: String & Singleton, S <: Scope, V]: SingleNodeKey[Key, S, V] =
      compiletime.constValue[Key].asInstanceOf
    inline def multiNodeKey[Key <: String & Singleton, S <: Scope, V]: MultiNodeKey[Key, S, V] =
      compiletime.constValue[Key].asInstanceOf
  import Scope.*
  def obj(closure: Scope.Root ?=> Unit): Scope.Root =
    val root: Scope.Root = Scope.Root(collection.mutable.HashMap.empty)
    closure(using root)
    root
  extension [S <: Scope, K <: Singleton & String, V](key: K)
    def :=(using S,SingleValueKey[K, S, V])(value: V): Unit =
      summon[S].value.put(key, value)
    def ::=(using S,MultiValueKey[K, S, V])(value: scala.collection.Iterable[V]): Unit =
      summon[S].value.put(key, value.toBuffer)
    def +=(using S,MultiValueKey[K, S, V])(value: V): Unit =
      summon[S].value.get(key) match
        case None      => summon[S].value.put(key, collection.mutable.Buffer(value))
        case Some(seq) => seq.asInstanceOf[collection.mutable.Buffer[V]] += value
    def ++=(using S,MultiValueKey[K, S, V])(value: scala.collection.Iterable[V]): Unit =
      summon[S].value.get(key) match
        case None      => summon[S].value.put(key, value.toBuffer)
        case Some(seq) => seq.asInstanceOf[collection.mutable.Buffer[V]] ++= value
    def apply(using s:S,either: Either[SingleNodeKey[K, S, V], MultiNodeKey[K, S, V]])(closure: S >> V ?=> Unit): Unit =
      val x: S >> V = Scope.>>[S, V](collection.mutable.HashMap.empty)
      closure(using x)
      either match
        case Left(value) => s.value.put(key, x)
        case Right(value) =>
          s.value.get(key) match
            case None        => s.value.put(key, collection.mutable.Buffer(x))
            case Some(value) => value.asInstanceOf[collection.mutable.Buffer[Any]] += x
