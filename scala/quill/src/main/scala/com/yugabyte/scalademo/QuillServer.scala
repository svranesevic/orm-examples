package com.yugabyte.scalademo

import cats.effect._
import cats.implicits._
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

object QuillServer {

  def stream[F[_]: ConcurrentEffect](
      blocker: Blocker
  )(implicit F: Sync[F], T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {
    val db = new DbContext

    for {
      client <- BlazeClientBuilder[F](global).stream
      helloWorldAlg = HelloWorld.impl[F]
      jokeAlg       = Jokes.impl[F](client)

      userAlg = User.impl[F](db, blocker)

      httpApp = (
        QuillRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
        QuillRoutes.jokeRoutes[F](jokeAlg) <+>
        QuillRoutes.userRoutes[F](userAlg)
      ).orNotFound

      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

      exitCode <- BlazeServerBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
