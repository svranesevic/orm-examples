package com.yugabyte.scalademo.repository

import cats.effect.{ Blocker, ContextShift, Sync }
import com.yugabyte.scalademo.entity.Product

trait ProductsRepository[F[_]] {

  def create(product: Product): F[Product]
  def find(id: Long): F[Option[Product]]
  def update(product: Product): F[Unit]
  def delete(product: Product): F[Unit]
}

object ProductsRepository {

  def apply[F[_]](implicit F: ProductsRepository[F]): ProductsRepository[F] = F

  def impl[F[_]: Sync](db: DbContext, blocker: Blocker)(implicit CS: ContextShift[F]): ProductsRepository[F] =
    new ProductsRepository[F] {

      import db._

      val products = quote(query[Product])

      def create(product: Product): F[Product] = blocker.delay {
        product.copy(id = run(products.insert(lift(product)).returningGenerated(_.id)))
      }

      def find(id: Long): F[Option[Product]] = blocker.delay {
        run(products.filter(_.id == lift(id))).headOption
      }

      def update(product: Product): F[Unit] =
        blocker.delay {
          run(products.filter(_.id == lift(product.id)).update(lift(product)))
        }

      def delete(product: Product): F[Unit] =
        blocker.delay {
          run(products.filter(_.id == lift(product.id)).delete)
        }
    }
}
