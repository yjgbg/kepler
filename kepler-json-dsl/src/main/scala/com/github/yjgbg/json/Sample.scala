package com.github.yjgbg.json

object Sample:
  @main def main =
    import k8s.KubernetesEnhanceDel.*
    context("bbb"):
      namespace("default"):
        pod("xxx"):
          spec:
            proxy("mysql", 3306){}
        deployment("gateway"):
          labels("app" -> "gateway")
          spec:
            selectorMatchLabels("app" -> "gateway")
            template:
              labels("app" -> "gateway")
              spec:
                volumeCustom("www"):
                  fileImagePath("www","reg2.hypers.cc/has-frontend:latest","/usr/share/nginx/www","echo 'hello'")
                container("app", "nginx:alpine"):
                  volumeMounts("www" -> "/usr/share/nginx/www")
                  env("k0" -> "v0")
        tcpNodePort(8080,8080,"" -> "")
        service("gateway"):
          spec:
            selector("app" -> "gateway")
            tcpPorts(80 -> 80)
