package com.yugabyte.scalademo.repository

import cats.effect.{ Blocker, ContextShift, Sync }
import com.yugabyte.scalademo.entity.Order

trait OrdersRepository[F[_]] {

  def create(order: Order): F[Order]
  def find(id: Long): F[Option[Order]]

  def update(order: Order): F[Unit]

  def delete(order: Order): F[Unit]
}

object OrdersRepository {

  def apply[F[_]](implicit F: OrdersRepository[F]): OrdersRepository[F] = F

  def impl[F[_]: Sync](db: DbContext, blocker: Blocker)(implicit CS: ContextShift[F]): OrdersRepository[F] =
    new OrdersRepository[F] {

      import db._

      val orders = quote(query[Order])

      def create(order: Order): F[Order] = blocker.delay {
        order.copy(id = run(orders.insert(lift(order)).returningGenerated(_.id)))
      }

      def find(id: Long): F[Option[Order]] = blocker.delay {
        run(orders.filter(_.id == lift(id))).headOption
      }

      def update(order: Order): F[Unit] =
        blocker.delay {
          run(orders.filter(_.id == lift(order.id)).update(lift(order)))
        }

      def delete(order: Order): F[Unit] =
        blocker.delay {
          run(orders.filter(_.id == lift(order.id)).delete)
        }
    }
}
