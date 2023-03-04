import com.github.yjgbg.json.KubernetesDsl.*
@main def colima = context("colima") {
  namespace("default") {
    pod("nginx") {
      spec {
        container("nginx","nginx") {
        }
      }
    }
  }
}