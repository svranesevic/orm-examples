package com.yugabyte.scalademo.dao.public

case class Product(id: Int, productName: String, description: String, price: scala.math.BigDecimal)

case class FlywaySchemaHistory(installedRank: Int, version: Option[String], description: String, `type`: String, script: String, checksum: Option[Int], installedBy: String, installedOn: java.time.LocalDateTime, executionTime: Int, success: Boolean)

case class OrderLine(id: Int, orderId: Int, productId: Int, units: Int)

case class Orders(id: Int, orderTotal: scala.math.BigDecimal, userId: Int)

case class Users(id: Int, firstName: String, lastName: String, email: String)

trait PublicExtensions[Idiom <: io.getquill.idiom.Idiom, Naming <: io.getquill.NamingStrategy] {
  this:io.getquill.context.Context[Idiom, Naming] =>

  object ProductDao {
      def query = quote {
          querySchema[Product](
            "public.product",
            _.id -> "id",
            _.productName -> "product_name",
            _.description -> "description",
            _.price -> "price"
          )
                    
        }
                  
    }

    object FlywaySchemaHistoryDao {
      def query = quote {
          querySchema[FlywaySchemaHistory](
            "public.flyway_schema_history",
            _.installedRank -> "installed_rank",
            _.version -> "version",
            _.description -> "description",
            _.`type` -> "type",
            _.script -> "script",
            _.checksum -> "checksum",
            _.installedBy -> "installed_by",
            _.installedOn -> "installed_on",
            _.executionTime -> "execution_time",
            _.success -> "success"
          )
                    
        }
                  
    }

    object OrderLineDao {
      def query = quote {
          querySchema[OrderLine](
            "public.order_line",
            _.id -> "id",
            _.orderId -> "order_id",
            _.productId -> "product_id",
            _.units -> "units"
          )
                    
        }
                  
    }

    object OrdersDao {
      def query = quote {
          querySchema[Orders](
            "public.orders",
            _.id -> "id",
            _.orderTotal -> "order_total",
            _.userId -> "user_id"
          )
                    
        }
                  
    }

    object UsersDao {
      def query = quote {
          querySchema[Users](
            "public.users",
            _.id -> "id",
            _.firstName -> "first_name",
            _.lastName -> "last_name",
            _.email -> "email"
          )
                    
        }
                  
    }
}
