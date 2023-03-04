import com.github.yjgbg.json.KubernetesDsl.*
@main def colima = context("colima","apply --server-side=true") {
  namespace("default") {
    deployment("") {
      
    }
  }
}