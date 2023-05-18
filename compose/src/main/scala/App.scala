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
import com.github.yjgbg.compose.Document.Layout.Verb.OnInit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val (state,setState) = Rx.useState(false)
val (help,setHelp) = Rx.useState(false)
val (seq,setSeq) = Rx.useState(Seq(1,2,3))
@main def main = OpenGL.Application(Title := state.map(_.toString())) {
  val (counter,setCounter) = useState(0)
  counter.addListener({(_,next) => if next == 0 then System.exit(0)},counter)
  val title = Rx.usePeriod(1000,LocalDateTime.now(),{_ => LocalDateTime.now()})
  If(true.rx) {
    Window(Id := "test", DefaultWidth := 800, DefaultHeight := 800) {
      Title := title.map(_.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
      Menu(Path := Seq("123")) {
        Action := { () => }
      }
      OnInit := {() => setCounter(counter.value+1)}
      OnExit := {() => setCounter(counter.value-1)}
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