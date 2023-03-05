
import zio.test.*

object ExampleSpec extends ZIOSpecDefault {
  def spec = suite("clock")(
    test("foo") {
      assertTrue(true)
    }
  )
}