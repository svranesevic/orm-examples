package com.yugabyte.scalademo

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult

class HelloWorldSpec extends org.specs2.mutable.Specification {

  "HelloWorld" >> {
    "return 200" >> {
      uriReturns200()
    }
    "return hello world" >> {
      uriReturnsHelloWorld()
    }
  }

  private[this] val retHelloWorld: Response[IO] = {
    val getHW = Request[IO](Method.GET, uri"/hello/world")
    val helloWorld = HelloWorld.impl[IO]
    QuillRoutes.helloWorldRoutes(helloWorld).orNotFound(getHW).unsafeRunSync()
  }

  private[this] def uriReturns200(): MatchResult[Status] =
    retHelloWorld.status must beEqualTo(Status.Ok)

  private[this] def uriReturnsHelloWorld(): MatchResult[String] =
    retHelloWorld.as[String].unsafeRunSync() must beEqualTo("{\"message\":\"Hello, world\"}")
}