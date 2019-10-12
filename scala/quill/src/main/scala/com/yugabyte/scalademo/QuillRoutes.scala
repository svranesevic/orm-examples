package com.yugabyte.scalademo

import cats.effect.Sync
import cats.implicits._
import com.yugabyte.scalademo.User.CreateUserDto
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object QuillRoutes {

  def jokeRoutes[F[_]: Sync](J: Jokes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "joke" =>
        for {
          joke <- J.get
          resp <- Ok(joke)
        } yield resp
    }
  }

  def helloWorldRoutes[F[_]: Sync](H: HelloWorld[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        for {
          greeting <- H.hello(HelloWorld.Name(name))
          resp     <- Ok(greeting)
        } yield resp
    }
  }

  def userRoutes[F[_]: Sync](U: User[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "users" =>
        for {
          allUsers <- U.all
          resp     <- Ok(allUsers)
        } yield resp

      case GET -> Root / "users" / LongVar(id) =>
        for {
          maybeUser <- U.findById(id)
          resp      <- maybeUser.fold(NotFound())(Ok(_))
        } yield resp

      case DELETE -> Root / "users" / LongVar(id) =>
        U.delete(id) *> NoContent()

      case req @ POST -> Root / "users" =>
        req.as[CreateUserDto].flatMap { createUserDto =>
          for {
            createdUser <- U.create(createUserDto)
            resp        <- Ok(createdUser)
          } yield resp
        }
    }
  }
}
