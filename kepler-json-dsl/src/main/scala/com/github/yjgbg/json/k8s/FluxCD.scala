package com.github.yjgbg.json
package k8s

trait FluxCD:
  self:KubernetesDsl =>
  given Version["fluxcd.GitRepository"] = "source.toolkit.fluxcd.io/v1".asInstanceOf
  type FluxCDGitRepositoryScope = Scope
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

