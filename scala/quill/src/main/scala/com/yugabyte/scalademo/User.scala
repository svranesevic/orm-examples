package com.yugabyte.scalademo

import cats.effect.{ Blocker, ContextShift, Sync }
import com.yugabyte.scalademo.dao.public.Users
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

  final case class UserDto(userId: Int, firstName: String, lastName: String, email: String)
  object UserDto {
    implicit def encoder[F[_]: Sync]: EntityEncoder[F, UserDto] = jsonEncoderOf[F, UserDto]
  }

  final case class AllUsersDto(content: Seq[UserDto])

  object AllUsersDto {
    implicit def encoder[F[_]: Sync]: EntityEncoder[F, AllUsersDto] = jsonEncoderOf[F, AllUsersDto]
  }

  def impl[F[_]: Sync](dbContext: DbContext, blocker: Blocker)(implicit CS: ContextShift[F]): User[F] = new User[F] {
    import dbContext._

    override def create(createUserDto: CreateUserDto): F[UserDto] =
      blocker.delay {
        val newUserEntity = createUserDto.toEntity

        val createdUserEntity = run(
          dbContext.UsersDao.query
            .insert(lift(newUserEntity))
            .returning(u => u)
        )

        createdUserEntity.toDto
      }

    override def findById(id: Long): F[Option[UserDto]] =
      blocker.delay {
        run(dbContext.UsersDao.query.filter(_.id == lift(id))).headOption
          .map(_.toDto)
      }

    override def all: F[AllUsersDto] = blocker.delay {
      val users = run(dbContext.UsersDao.query.filter(_ => true))
      AllUsersDto(users.map(_.toDto))
    }

    override def delete(id: Long): F[Unit] = blocker.delay {
      run(dbContext.UsersDao.query.filter(_.id == lift(id)).delete)
    }
  }

  implicit class DtoToEntityConverter(dto: CreateUserDto) {
    implicit def toEntity: Users =
      dto
        .into[Users]
        .withFieldComputed(_.id, _ => 0)
        .transform
  }

  implicit class EntityToDtoConverter(entity: Users) {
    implicit def toDto: UserDto =
      entity
        .into[UserDto]
        .withFieldRenamed(_.id, _.userId)
        .transform
  }
}
