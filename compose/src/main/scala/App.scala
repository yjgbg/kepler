import com.github.yjgbg.compose.Rx

import com.github.yjgbg.compose.Dsl.{*,given}
import com.github.yjgbg.compose.Rx.*
import com.github.yjgbg.compose.Document.{Scope as _,*,given}
import com.github.yjgbg.compose.Document

val (state,setState) = Rx.useState(false)
val (help,setHelp) = Rx.useState(false)
val (seq,setSeq) = Rx.useState(Seq(1,2,3))
val (title,setTitle) = Rx.useState("search")
val app = Application(Name := state.map(_.toString())) {
  For(seq) {i =>
    Dialog(Name := i.toString()){}
  }
  Window(Name := title) {
    Menu(Path := Seq("123")) {
      // Action := { () => }
    }
    Menu() {
      Path := Seq("帮助","发行说明")
      // Action := {() => setHelp(true)}
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
@main def main = {
  app.map(_.show).addListener{(_,next) => println(next)}
  // app.map{ap => ap.get(Dialog).map(_.show)}.addListener{(_,newValue) => println(newValue)}
  setHelp(true)
  setHelp(false)
}