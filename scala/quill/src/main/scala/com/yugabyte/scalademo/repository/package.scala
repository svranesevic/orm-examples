package com.yugabyte.scalademo

import io.getquill.{ CompositeNamingStrategy2, PluralizedTableNames, PostgresJdbcContext, SnakeCase }

package object repository {

  type DbContext = PostgresJdbcContext[CompositeNamingStrategy2[SnakeCase.type, PluralizedTableNames.type]]
}
