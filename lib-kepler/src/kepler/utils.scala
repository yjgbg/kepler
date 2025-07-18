package kepler

import com.jayway.jsonpath.internal.filter.ValueNodes.JsonNode
import com.jayway.jsonpath.JsonPath

object utils:
  extension [A](a:A) 
    def |>[B](closure:A => B):B = closure(a)
  extension (string:String)
    def >>(path:os.Path) = os.write.over(path,string)
  opaque type Json = String
  type JsonValue = String|Boolean|Double
  extension (json: Json)
    def path[A <: String|Boolean|Double](p:String):A  = JsonPath.read(json,p)
  end extension
