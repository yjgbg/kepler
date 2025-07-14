package com.github.yjgbg.kepler.dsl

object GitlabCi:
    export core.{*,given}
    import scala.jdk.CollectionConverters.{MapHasAsJava,BufferHasAsJava}
    private def processObj(obj:Any): Any = obj match
        case scope : Scope => processObj(scope.value)
        case hashMap:scala.collection.mutable.HashMap[?,?] => hashMap.mapValues{v => processObj(v)}.to(collection.mutable.HashMap).asJava
        case arrayBuffer:scala.collection.mutable.ArrayBuffer[?] => arrayBuffer.map(it => processObj(it)).asJava
        case other:Any => other
    def GitlabCi(closure:Closure[Scope.Root]):Unit = 
        import org.yaml.snakeyaml.{Yaml,DumperOptions}
        val root = obj(closure)
        val any = processObj(root)
        val dumperOptions = DumperOptions()
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        val string = Yaml(dumperOptions).dump(any)
        os.write.over(os.pwd / "gitlab-ci.sc.yml",string)
    def job(using scope:Scope.Root)(name:String)(closure: Closure[Scope.Root >> "job"]):Unit = 
        val svk:SingleNodeKey[name.type,Scope.Root,"job"] = name.asInstanceOf
        name.apply(using scope,Left(svk))(closure)
    val image:"image" = compiletime.constValue
    given SingleValueKey[image.type,Scope.Root >> "job", String] = Key.singleValueKey
    val script:"script" = compiletime.constValue
    given MultiValueKey[script.type,Scope.Root >> "job", String] = Key.multiValueKey
    val beforeScript:"before_script" = compiletime.constValue
    given MultiValueKey[beforeScript.type,Scope.Root >> "job", String] = Key.multiValueKey
    val retry:"retry" = compiletime.constValue
    given MultiValueKey[retry.type,Scope.Root >> "job", 0|1|2] = Key.multiValueKey
    val needs:"needs" = compiletime.constValue
    given MultiValueKey[needs.type,Scope.Root >> "job",String] = Key.multiValueKey
    val stage:"stage" = compiletime.constValue
    given MultiValueKey[stage.type,Scope.Root,String] = Key.multiValueKey
    given SingleValueKey[stage.type,Scope.Root >> "job",String] = Key.singleValueKey
    