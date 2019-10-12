package com.yugabyte.scalademo

import cats.effect._
import cats.implicits._
import com.yugabyte.scalademo.repository.{ OrderLinesRepository, OrdersRepository, ProductsRepository, UsersRepository }
import fs2.Stream
import io.getquill.{ NamingStrategy, PluralizedTableNames, PostgresJdbcContext, SnakeCase }
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

object QuillServer {

  case class LongWrapper(l: Long) extends AnyVal

  def stream[F[_]: ConcurrentEffect](
      blocker: Blocker
  )(implicit F: Sync[F], T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {
    for {
      client <- BlazeClientBuilder[F](global).stream
      helloWorldAlg = HelloWorld.impl[F]
      jokeAlg       = Jokes.impl[F](client)

      quillCtx: repository.DbContext = new PostgresJdbcContext(
        NamingStrategy(SnakeCase, PluralizedTableNames),
        "ctx"
      )

      productsRepoAlg   = ProductsRepository.impl[F](quillCtx, blocker)
      ordersRepoAlg     = OrdersRepository.impl[F](quillCtx, blocker)
      orderLinesRepoAlg = OrderLinesRepository.impl[F](quillCtx, blocker)

      usersRepoAlg = UsersRepository.impl[F](quillCtx, blocker)
      userAlg      = User.impl[F](usersRepoAlg)

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
