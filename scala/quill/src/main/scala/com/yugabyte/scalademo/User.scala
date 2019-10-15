package com.yugabyte.scalademo

import cats.effect.Sync
import cats.implicits._
import com.yugabyte.scalademo.repository.UsersRepository
import io.circe.generic.auto._
import io.scalaland.chimney.dsl._
import org.http4s.circe._
import org.http4s.{ EntityDecoder, EntityEncoder }

trait User[F[_]] {

  import User._

  def create(createUserDto: CreateUserDto): F[UserDto]
  def findById(id: Long): F[Option[UserDto]]
  def all: F[AllUsersDto]
  def delete(id: Long): F[Unit]
}

object User {

  implicit def apply[F[_]](implicit u: User[F]): User[F] = u

  final case class CreateUserDto(firstName: String, lastName: String, email: String)
  object CreateUserDto {
    implicit def decoder[F[_]: Sync]: EntityDecoder[F, CreateUserDto] = jsonOf[F, CreateUserDto]
  }

  final case class UserDto(userId: Long, firstName: String, lastName: String, email: String)
  object UserDto {
    implicit def encoder[F[_]: Sync]: EntityEncoder[F, UserDto] = jsonEncoderOf[F, UserDto]
  }

  final case class AllUsersDto(content: Seq[UserDto])

  object AllUsersDto {
    implicit def encoder[F[_]: Sync]: EntityEncoder[F, AllUsersDto] = jsonEncoderOf[F, AllUsersDto]
  }

  def impl[F[_]: Sync](U: UsersRepository[F]): User[F] = new User[F] {
    override def create(createUserDto: CreateUserDto): F[UserDto] = {
      val userEntity =
        createUserDto
          .into[entity.User]
          .withFieldComputed(_.id, _ => 0L)
          .transform

      for {
        user <- U.create(userEntity)
        userDto = user.into[UserDto].withFieldRenamed(_.id, _.userId).transform
      } yield userDto
    }

    override def findById(id: Long): F[Option[UserDto]] =
      for {
        maybeUser <- U.find(id)
        maybeUserDto = maybeUser.map(_.into[UserDto].withFieldRenamed(_.id, _.userId).transform)
      } yield maybeUserDto

    override def all: F[AllUsersDto] =
      for {
        users <- U.all()
        usersDto = users.map(_.into[UserDto].withFieldRenamed(_.id, _.userId).transform)
      } yield AllUsersDto(usersDto)

    override def delete(id: Long): F[Unit] = U.delete(id)
  }
}
