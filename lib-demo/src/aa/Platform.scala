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
  private def test(platforms: (Platform, () => Any)*):Option[Platform] = platforms.find{(p,closure) =>
    try 
      closure() match
        case false => false
        case _ => true
    catch
      case _: Throwable => false
  }.map((k,_) => k)
  lazy val current: Platform = 
    test(
      NODEJS -> (() => scala.scalajs.js.Dynamic.global.process),
      RNAndroid -> (() => scala.scalajs.js.Dynamic.global.Android),
      RNiOS -> (() => scala.scalajs.js.Dynamic.global.iOS),
      RNMacOS -> (() => scala.scalajs.js.Dynamic.global.electron.process.platform.asInstanceOf[String] == "darwin"),
      RNWindows -> (() => scala.scalajs.js.Dynamic.global.electron.process.platform.asInstanceOf[String] == "win32"),
      RNElectron -> (() => scala.scalajs.js.Dynamic.global.electron), // electron 必须在web前面，因为electron环境是加强的web环境
      RNWeb -> (() => scala.scalajs.js.Dynamic.global.window),
      JVM -> (() => true) // Default to JVM if no platform matches
    ).get