import com.github.yjgbg.compose
import com.github.yjgbg.compose.Rx.*
import com.github.yjgbg.compose.Document.{*,given}
import com.github.yjgbg.compose.Document
import com.github.yjgbg.compose.Document.Layout.Style.DefaultWidth
import com.github.yjgbg.compose.Document.Layout.Style.DefaultHeight
import com.github.yjgbg.compose.Document.Layout.Verb.OnExit
import com.github.yjgbg.compose.Document.Layout.Oriential
import com.github.yjgbg.compose.Document.Layout.Div
import com.github.yjgbg.compose.Document.Layout.Style.Width
import com.github.yjgbg.compose.Document.Layout.Style.Height
import com.github.yjgbg.compose.OpenGLDriver

val (state,setState) = Rx.useState(false)
val (help,setHelp) = Rx.useState(false)
val (seq,setSeq) = Rx.useState(Seq(1,2,3))
@main def main = OpenGLDriver.Application(Title := state.map(_.toString())) {
  val title = Rx.usePeriod(1000,0,_+1)
  If(title.map(_ < 10)) {
    Window(Id := "test", Title := title.map(_.toString()), DefaultWidth := 800, DefaultHeight := 800) {
      Menu(Path := Seq("123")) {
        Action := { () => }
      }
      OnExit := {() => println("exit")}
      Menu() {
        Path := Seq("帮助","发行说明")
        Action := {() => setHelp(true)}
      }
      Div(Oriential := "H",Width := 200,Height := 400) {
        // Verb.OnPress := {() => setTitle(title.value + 1)}
        Div(Oriential := "V") {
          Div(Oriential := "H") {
          }
        }
      }
    }
  }
}