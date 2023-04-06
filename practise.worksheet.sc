import scala.deriving.Mirror
trait ToJson[A]:
  extension (value:A) def toJson:String
object ToJson:
  given ToJson[Int] = ToJson{_.toString()}
  given ToJson[Double] = ToJson{_.toString()}
  given ToJson[Boolean] = ToJson{_.toString()}
  given ToJson[String] = ToJson{
    case null => "null"
    case s:String => s"\"$s\""
  }
  given [A,B <: Tuple](using a:A,b:B) : *:[A,B] = a *: b
  given [A](using a:A): *:[A,EmptyTuple] = a *: EmptyTuple
  given [A,B <: Iterable[A]](using ToJson[A]):ToJson[B] = 
    ToJson{seq => seq.map(_.toJson).mkString("[",",","]")}
  given [A](using ToJson[A]):ToJson[Map[String,A]] = ToJson{map => map.map{(k,v) => s"\"$k\":${v.toJson}"}.mkString("{",",","}")}
  def apply[A](f:A => String) = new ToJson[A]:
    extension (value: A) override def toJson: String = f(value)
  inline def derived[A](using m:Mirror.Of[A])(using t:Tuple.Map[m.MirroredElemTypes,ToJson]):ToJson[A] = 
    val labels = scala.compiletime.constValueTuple[m.MirroredElemLabels]
    apply{ 
      case null => "null"
      case (a:A) =>
        val values:m.MirroredElemTypes = Tuple.fromProduct(a.asInstanceOf[scala.Product]).asInstanceOf[m.MirroredElemTypes]
        val jsons = values.toArray.zip(t.toArray).asInstanceOf[Array[(Any,ToJson[Any])]].map{(x,y) => y.toJson(x)}
        labels.toArray.map(_.toString()).zip(jsons).map{(k,v) => s"\"$k\":$v"}.mkString("{",",","}")
    }
case class A(aa:Int,dfg:Double,string:Null|String = "fgh") derives ToJson
case class B(a:A) derives ToJson
val x = A(1,1.0)
x.toJson
val b = B(x)
b.toJson
val s = Map("1" -> b,"2" -> b,"3" -> b)
import ToJson.given
s.toJson
type Elem = [a] =>> a match {
  case String => Char
  case Array[b] => Elem[b]
  case Iterable[b] => Elem[b]
  case _ => a
}
def head[A](a:A): Elem[A] = a match
  case s:String => s.head
  case arr:Array[a] => head(arr.head)
  case iter:Iterable[i] => head(iter.head)
  case _ => a
val xxx = head("qweqw")
val yyy = head(Seq(1,2,3))