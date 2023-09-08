
import engine.*
import biz.Targeting.*
import biz.Creative

lazy val storageZIO = zio.ZIO.succeed{
  Searchine[Creative,biz.Targeting]
  .load(
    "IQIYI",
    Network.Network5G, // 定向条件
    Creative(10L,10L,20L,100)) // 数据对象
  .load(
    "IQIYI",
    Gender.Male && AgeBetween(18,23) || Gender.Female && AgeBetween(23,30),
    Creative(20L,20L,20L,100))
  .load(
    "IQIYI",
    Gender.Male && AgeBetween(18,23),
    Creative(20L,20L,30L,100))
  .load(
    "IQIYI",
    DNF.any(OS.IOS.values ++ OS.ANDROID.values).nn,
    Creative(20L,20L,30L,100))
  .load("BILIBILI-123",AdxCode("BILIBILI"),Creative(100L,100L,100L,100))
  .limit(10){_.orderItemId == 10L}
  .limit(20){_.orderItemId == 20L}
  .ready
}