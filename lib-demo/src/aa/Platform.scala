package aa

import scala.reflect.Selectable.reflectiveSelectable
enum Platform:
  case JVM extends Platform
  case NODEJS extends Platform
  case RNWindows extends Platform
  case RNMacOS extends Platform
  case RNAndroid extends Platform
  case RNiOS extends Platform
  case RNWeb extends Platform
  case RNElectron extends Platform
  def timestamp: String = this match
    case JVM => System.currentTimeMillis().toString
    case _ => new scala.scalajs.js.Date().getTime().toString
object Platform:
  private def test(platforms: (Platform, () => Unit)*):Option[Platform] = platforms.find{(p,closure) =>
    try 
      closure()
      true
    catch
      case _: Throwable => false
  }.map((k,_) => k)
  lazy val current: Platform = 
    test(
      NODEJS -> (() => scala.scalajs.js.Dynamic.global.process.asInstanceOf[scala.scalajs.js.Dynamic]),
      RNWindows -> (() => scala.scalajs.js.Dynamic.global.Windows),
      RNMacOS -> (() => scala.scalajs.js.Dynamic.global.MacOS),
      RNAndroid -> (() => scala.scalajs.js.Dynamic.global.Android),
      RNiOS -> (() => scala.scalajs.js.Dynamic.global.iOS),
      RNWeb -> (() => scala.scalajs.js.Dynamic.global.window.asInstanceOf[scala.scalajs.js.Dynamic]),
      RNElectron -> (() => scala.scalajs.js.Dynamic.global.Electron)
    ).getOrElse(JVM) // Default to JVM if no platform matches