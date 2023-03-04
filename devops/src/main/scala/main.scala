import com.github.yjgbg.json.KubernetesDsl.*
@main def main = context("colima","apply --server-side=true") {
  namespace("default") {
  }
}