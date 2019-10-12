package com.yugabyte.scalademo.repository

import cats.effect.{ Blocker, ContextShift, Sync }
import com.yugabyte.scalademo.entity.OrderLine

trait OrderLinesRepository[F[_]] {

  def create(e: OrderLine): F[OrderLine]
  def find(id: Long): F[Option[OrderLine]]
  def update(Order: OrderLine): F[Unit]
  def delete(Order: OrderLine): F[Unit]
}

object OrderLinesRepository {

  def apply[F[_]](implicit F: OrderLinesRepository[F]): OrderLinesRepository[F] = F

  def impl[F[_]: Sync](db: DbContext, blocker: Blocker)(implicit CS: ContextShift[F]): OrderLinesRepository[F] =
    new OrderLinesRepository[F] {

      import db._

      val orderLines = quote(query[OrderLine])

      def create(orderLine: OrderLine): F[OrderLine] = blocker.delay {
        orderLine.copy(id = run(orderLines.insert(lift(orderLine)).returningGenerated(_.id)))
      }

      def find(id: Long): F[Option[OrderLine]] = blocker.delay {
        run(orderLines.filter(_.id == lift(id))).headOption
      }

      def findTest(id: Long): F[db.IO[Option[OrderLine], Effect.Read]] = blocker.delay {
        runIO(orderLines.filter(_.id == lift(id))).map(_.headOption)
      }

      def update(orderLine: OrderLine): F[Unit] =
        blocker.delay {
          run(
            orderLines
              .filter(_.id == lift(orderLine.id))
              .update(lift(orderLine))
          )
        }

      def delete(orderLine: OrderLine): F[Unit] =
        blocker.delay {
          run(orderLines.filter(_.id == lift(orderLine.id)).delete)
        }
    }
}
