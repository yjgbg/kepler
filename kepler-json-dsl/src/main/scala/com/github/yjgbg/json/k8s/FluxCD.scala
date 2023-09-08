package com.github.yjgbg.json
package k8s
object FluxCDDsl extends KubernetesDsl,FluxCD
trait FluxCD:
  self:KubernetesDsl =>
  given FluxCDGitRepositoryVersion:Version["fluxcd.GitRepository"] = "source.toolkit.fluxcd.io/v1".asInstanceOf
  given FluxCDHelmRepositoryVersion:Version["fluxcd.HelmRepository"] = "source.toolkit.fluxcd.io/v1beta2".asInstanceOf
  // given Version["fluxcd.GitRepository"] = "source.toolkit.fluxcd.io/v1".asInstanceOf
  // given Version["fluxcd.GitRepository"] = "source.toolkit.fluxcd.io/v1".asInstanceOf
  opaque type FluxCDGitRepositoryScope = Scope
  def gitRepository(using NamespaceScope,Version["fluxcd.GitRepository"])
  (name:String)(closure:(FluxCDGitRepositoryScope,ResourceProvince[FluxCDGitRepositoryScope]) ?=> Unit):Unit = 
    summon[NamespaceScope].resourceSeq = summon[NamespaceScope].resourceSeq :+ Resource(s"$name-fluxcd-git-repository",{
      "apiVersion" := summon[Version["fluxcd.GitRepository"]].asInstanceOf
      "metadata"::= {
        "name" := name
        "namespace" := summon[NamespaceScope].name
      }
      given ResourceProvince[FluxCDGitRepositoryScope] = null.asInstanceOf
      closure.apply
    })
  opaque type FluxCDHelmRepositoryScope = Scope
  def helmRepository(using NamespaceScope,Version["fluxcd.HelmRepository"])
  (name:String)(closure:(FluxCDHelmRepositoryScope,ResourceProvince[FluxCDHelmRepositoryScope]) ?=> Unit) =
    summon[NamespaceScope].resourceSeq = summon[NamespaceScope].resourceSeq :+ Resource(s"$name-fluxcd-helm-repository",{
      "apiVersion" := summon[Version["fluxcd.HelmRepository"]].asInstanceOf
      "metadata"::= {
        "name" := name
        "namespace" := summon[NamespaceScope].name
      }
      given ResourceProvince[FluxCDHelmRepositoryScope] = null.asInstanceOf
      closure.apply
    })
  def interval[A<:FluxCDGitRepositoryScope|FluxCDHelmRepositoryScope]
  (using A >> SpecScope)(seconds:Int):Unit = {
    given Scope = summon[A >> SpecScope].asInstanceOf
    "interval" := s"${seconds}s"
  }
  def timeout[A<:FluxCDGitRepositoryScope|FluxCDHelmRepositoryScope]
  (using A >> SpecScope)(seconds:Int):Unit = {
    given Scope = summon[A >> SpecScope].asInstanceOf
    "timeout" := s"${seconds}s"
  }
  def url[A<:FluxCDGitRepositoryScope|FluxCDHelmRepositoryScope]
  (using A >> SpecScope)(url:String):Unit = {
    given Scope = summon[A >> SpecScope].asInstanceOf
    "url" := url
  }
  def `type`(using FluxCDHelmRepositoryScope>>SpecScope)(`type`:"default"|"oci") = {
    given Scope = summon[FluxCDGitRepositoryScope >> SpecScope].asInstanceOf
    "type" := `type`
  }
  opaque type RefScope = Scope
  def ref(using FluxCDGitRepositoryScope >> SpecScope)(closure:RefScope ?=> Unit):Unit = {
    given Scope = summon[FluxCDGitRepositoryScope >> SpecScope].asInstanceOf
    "ref" ::= closure
  }
  def branch(using RefScope)(branch:String):Unit = "branch" := branch
  def tag(using RefScope)(tag:String):Unit = "tag" := tag
  /**
    * @param semver reference: https://github.com/Masterminds/semver#checking-version-constraints
    */
  def semver(using RefScope)(semver:String):Unit = "semver" := semver
  /**
    * @param name reference: https://git-scm.com/docs/git-check-ref-format#_description
    */
  def name(using RefScope)(name:String):Unit = "name" := name
  def commit(using RefScope)(commit:String):Unit = "commit" := commit
  opaque type VerifyScope = Scope
  // def verify(using FluxCDGitRepositoryScope >> SpecScope)(closure:VerifyScope ?=> Unit) = {
  //   given Scope = summon[FluxCDGitRepositoryScope >> SpecScope].asInstanceOf
  //   "verify" ::= closure
  // }
  // to specify what Git commit object should be verified. Only supports head at present.
  def mode(using VerifyScope)(mode:String):Unit = "mode" := mode
  def secretRefName[A<:FluxCDGitRepositoryScope|FluxCDHelmRepositoryScope]
  (using A >> SpecScope)
  (secretRefName:String):Unit = {
    given Scope = summon[A >> SpecScope].asInstanceOf
    "secretRef" ::= { "name" := secretRefName}
  }
  @scala.annotation.targetName("fluxSuspend")
  def suspend[A<:FluxCDGitRepositoryScope|FluxCDHelmRepositoryScope]
  (using A >> SpecScope)(suspend:Boolean):Unit = {
    given Scope = summon[A >> SpecScope].asInstanceOf
    "suspend" := suspend
  }
  def recurseSubmodules(using FluxCDGitRepositoryScope >> SpecScope)(recurseSubmodules: Boolean):Unit = {
    given Scope = summon[FluxCDGitRepositoryScope >> SpecScope].asInstanceOf
    "recurseSubmodules" := recurseSubmodules
  }
  def include(using FluxCDGitRepositoryScope >> SpecScope)(name:String,fromPath:String,toPath:String) = {
    given Scope = summon[FluxCDGitRepositoryScope >> SpecScope].asInstanceOf
    "include" ++= {
      "repository" ::= {"name" := name}
      "fromPath" := fromPath
      "toPath" := toPath
    }
  }
  def ignore(using FluxCDGitRepositoryScope >> SpecScope)(ignore:String):Unit = {
    given Scope = summon[FluxCDGitRepositoryScope >> SpecScope].asInstanceOf
    "ignore" := ignore
  }
