package com.yugabyte.scalademo

import io.getquill.codegen.jdbc.ComposeableTraitsJdbcCodegen
import io.getquill.codegen.model.{ CustomNames, NameParser }

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class Generator() extends ComposeableTraitsJdbcCodegen(configPrefix = "ctx", "com.yugabyte.scalademo.dao", false) {

  import io.getquill.codegen.util.StringUtil._

  override def nameParser: NameParser = CustomNames(
    cm => cm.columnName.snakeToLowerCamel,
    tm => tm.tableName.snakeToUpperCamel
  )
}

object QuillCodegen extends App {

  private val generator = new Generator

  generator.querySchemaNaming

  val res =
    generator
      .writeAllFiles("src/main/scala/com/yugabyte/scalademo/dao")
      .map { f =>
        println(s"${Console.GREEN}Generated files:${Console.RESET} $f")
      }

  Await.result(res, 1 minute)
}
