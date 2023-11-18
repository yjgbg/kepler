package com.github.yjgbg.kepler.dsl

import scala.collection.Iterable

object core:
  enum Scope(private[core] val value:collection.mutable.HashMap[String,Any]):
    case Root(private[core] override val value:collection.mutable.HashMap[String,Any]) extends Scope(value)
    case >>[+A <: Scope,+B](private[core] override val value:collection.mutable.HashMap[String,Any]) extends Scope(value)
  export Scope.*
  extension [S <:Scope](a:S) 
    def toHashMap:collection.immutable.HashMap[String,Any] = a.value.view.mapValues(_.asInstanceOf[Matchable]).mapValues{
      case x:Scope => x.toHashMap
      case iterable:Iterable[?] => iterable.map(_.asInstanceOf[Matchable]).map{
        case x:Scope => x.toHashMap
        case other => other
      }
      case other => other
    }.to(collection.immutable.HashMap)
    def getNode[Key <: String & Singleton,V](key:Key)(using NodeKey[Key,S,V]):Seq[S >> V] = 
      a.value.get(key) match
        case None => Nil
        case Some(value) => value.asInstanceOf[collection.mutable.Buffer[S >> V]].toSeq
    def getSingleValue[Key <: String&Singleton,V](key:Key)(using SingleValueKey[Key,S,V]):Option[V] = 
      a.value.get(key) match
        case None => None
        case Some(value) => Some(value.asInstanceOf[V])
    def getMultiValue[Key <: String & Singleton,V](key:Key)(using MultiValueKey[Key,S,V]):Seq[V] = 
      a.value.get(key) match
        case None => Nil
        case Some(value) => value.asInstanceOf[collection.mutable.Buffer[V]].toSeq
  opaque type SingleValueKey[Key,S <: Scope,V] = Key
  opaque type MultiValueKey[Key,S <: Scope,V] = Key
  opaque type NodeKey[Key,S <:Scope,V] = Key
  object Key:
    inline def singleValueKey[Key <: String & Singleton,S <: Scope,V]:SingleValueKey[Key,S,V] = compiletime.constValue[Key].asInstanceOf
    inline def multiValueKey[Key <: String & Singleton,S <: Scope,V]:MultiValueKey[Key,S,V] = compiletime.constValue[Key].asInstanceOf
    inline def nodeKey[Key <: String & Singleton,S <: Scope,V]:NodeKey[Key,S,V] = compiletime.constValue[Key].asInstanceOf
  import Scope.*
  def obj(closure: Scope.Root ?=> Unit): Scope.Root = 
    val root:Scope.Root = Scope.Root(collection.mutable.HashMap.empty)
    closure(using root)
    root
  extension [S <: Scope, K <: Singleton & String, V](key: K)
    def :=(using SingleValueKey[K,S,V],S)(value:V):Unit = 
      summon[S].value.put(key,value)
    def :=(using MultiValueKey[K,S,V],S)(value:Iterable[V]):Unit =
      summon[S].value.put(key,value.toBuffer)
    def +=(using MultiValueKey[K,S,V],S)(value:V):Unit = 
      summon[S].value.get(key) match
        case None => summon[S].value.put(key,collection.mutable.Buffer(value))
        case Some(seq) => seq.asInstanceOf[collection.mutable.Buffer[V]] += value

    def ++=(using MultiValueKey[K,S,V],S)(value:Iterable[V]):Unit =
      summon[S].value.get(key) match
        case None => summon[S].value.put(key,value.toBuffer)
        case Some(seq) => seq.asInstanceOf[collection.mutable.Buffer[V]] ++= value
    
    def apply(using NodeKey[K,S,V],S)(closure0: S >> V ?=> Unit)(closure1: S >> V ?=> Unit) :Unit =
      val x:S >> V = Scope.>>[S,V](collection.mutable.HashMap.empty)
      closure0(using x)
      closure1(using x)
      summon[S].value.get(key) match
        case None => summon[S].value.put(key,collection.mutable.Buffer(x))
        case Some(value) => summon[S].value.put(key,value.asInstanceOf[collection.mutable.Buffer[S >> V]] += x)