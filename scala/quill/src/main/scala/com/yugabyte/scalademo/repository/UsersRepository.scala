package com.yugabyte.scalademo.repository

import cats.effect.{ Blocker, ContextShift, Sync }
import com.yugabyte.scalademo.entity.User

trait UsersRepository[F[_]] {

  def create(user: User): F[User]
  def find(id: Long): F[Option[User]]
  def all(): F[List[User]]
  def update(user: User): F[Unit]
  def delete(id: Long): F[Unit]
}

object UsersRepository {

  def apply[F[_]](implicit F: UsersRepository[F]): UsersRepository[F] = F

  def impl[F[_]: Sync](db: DbContext, blocker: Blocker)(implicit CS: ContextShift[F]): UsersRepository[F] =
    new UsersRepository[F] {

      import db._

      val users = quote(query[User])

      def create(user: User): F[User] = blocker.delay {
        val generatedId = run(users.insert(lift(user)).returningGenerated(_.id))
        user.copy(id = generatedId)
      }

      def find(id: Long): F[Option[User]] = blocker.delay {
        run(users.filter(_.id == lift(id))).headOption
      }

      def all(): F[List[User]] = blocker.delay {
        run(users)
      }

      def update(user: User): F[Unit] =
        blocker.delay {
          run(users.filter(_.id == lift(user.id)).update(lift(user)))
        }

      def delete(id: Long): F[Unit] =
        blocker.delay {
          run(users.filter(_.id == lift(id)).delete)
        }
    }
}
