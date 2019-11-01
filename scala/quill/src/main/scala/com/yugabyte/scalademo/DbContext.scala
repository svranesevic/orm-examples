package com.yugabyte.scalademo

import com.yugabyte.scalademo.dao.public.PublicExtensions
import io.getquill._

case class DbContext()
    extends PostgresJdbcContext(
      NamingStrategy(SnakeCase, PluralizedTableNames),
      "ctx"
    )
    with PublicExtensions[PostgresDialect, CompositeNamingStrategy2[SnakeCase.type, PluralizedTableNames.type]]
