import com.github.yjgbg.json.KubernetesDsl.*
@main def colima = context("colima") {
  namespace("default") {
    deployment("") {

    }
  }
}