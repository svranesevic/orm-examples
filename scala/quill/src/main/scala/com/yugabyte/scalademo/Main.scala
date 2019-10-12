package com.yugabyte.scalademo

import cats.effect.{ Blocker, ExitCode, IO, IOApp }
import org.flywaydb.core.Flyway

object Main extends IOApp {

  def applyMigrations(): IO[Unit] = IO.delay {
    Flyway
      .configure()
      .dataSource(s"jdbc:postgresql://localhost:5433/postgres", "postgres", "mysecretpassword")
      .load()
      .migrate()
  }

  def run(args: List[String]): IO[ExitCode] =
    Blocker[IO].use { blocker =>
      for {
        _ <- applyMigrations()
        _ <- QuillServer
          .stream[IO](blocker)
          .compile
          .drain
      } yield ExitCode.Success
    }
}
