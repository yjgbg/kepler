package com.github.yjgbg.adserving

import zhttp.http.*
import zhttp.service.Server
import zio.*

object web extends ZIOAppDefault {
  override def run = Server.start(8090, app)
  val app: HttpApp[Any, Throwable] =
    given cats.Functor[Task] with cats.Applicative[Task] with {
      override def map[A, B](fa: Task[A])(f: A => B): Task[B] = fa.map(f)
      override def ap[A, B](ff: Task[A => B])(fa: Task[A]): Task[B] = for {
        f <- ff
        a <- fa 
      } yield f(a)
      override def pure[A](x: A): Task[A] = ZIO.succeed(x)
    }
    Http.collectZIO[Request] {
      case req @ Method.GET -> !! / "bid" / adxCode / nid => for {
        _ <- Console.printLine(adxCode+"/"+nid)
        // chunk <- req.body
        chunk = null
        adxAdaptor = AdxAdaptor(adxCode)
        evaluator = adxAdaptor.evaluator(nid,chunk)
        searchResult <- biz.searchine.search(adxAdaptor.limit,evaluator)
      } yield adxAdaptor.handler(searchResult)
    }
}


