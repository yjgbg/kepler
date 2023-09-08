opaque type Positive <: Int = Int
object Positive:
  def apply(n: Int): Option[Positive] =
    if (n > 0) Some(n.asInstanceOf[Positive]) else None


val a =  Positive(-1)