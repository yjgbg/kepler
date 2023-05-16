import com.github.yjgbg.compose.Rx.*
import com.github.yjgbg.compose.DocumentDsl.{*,given}
import com.github.yjgbg.compose.DocumentDsl
import com.github.yjgbg.compose.DocumentDsl.Layout.Style.DefaultWidth
import com.github.yjgbg.compose.DocumentDsl.Layout.Style.DefaultHeight
import com.github.yjgbg.compose.DocumentDsl.Layout.Verb.OnExit

val (state,setState) = Rx.useState(false)
val (help,setHelp) = Rx.useState(false)
val (seq,setSeq) = Rx.useState(Seq(1,2,3))
@main def main = Application(Name := state.map(_.toString())) {
  For(seq) {i =>
    Dialog(Name := i.toString()){}
  }
  val title = Rx.usePeriod(1000,0,_+1)
  Window(Id := "test",Name := title.map(_.toString()),DefaultWidth := 300,DefaultHeight := 300) {
    Menu(Path := Seq("123")) {
      Action := { () => }
    }
    OnExit := {() => println("exit")}
    Menu() {
      Path := Seq("帮助","发行说明")
      Action := {() => setHelp(true)}
    }
    import Layout.*
    import Style.*
    Div(Oriential := "H",Width := 200,Height := 400) {
      // Verb.OnPress := {() => setTitle(title.value + 1)}
      Div(Oriential := "V") {
        Div(Oriential := "H") {
        }
      }
    }
  }
  If(help) {
    Dialog(Name := state.map(_.toString())) {
      import Layout.*
      Verb.OnExit := {() => setHelp(false)}
      Div(Style.Height := 500,Style.Width := 300) {
        
        // Skija := {(surface) => /*一些操作*/}
      }
    }
  }
}
// @main def main = {
//   println(app.value.json)
//   app.addListener{(oldApp,newApp) => println(newApp.json)}
//   setSeq(Seq(2))
//   setHelp(true)
//   setHelp(false)
// }