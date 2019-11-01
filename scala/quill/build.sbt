val Http4sVersion       = "0.20.11"
val CirceVersion        = "0.12.2"
val PostgreSQLVersion   = "42.2.8"
val QuillVersion        = "3.4.10"
val QuillCodegenVersion = "3.4.10"
val FlywayVersion       = "6.0.6"
val ChimneyVersion      = "0.3.3"
val LogbackVersion      = "1.2.3"
val Specs2Version       = "4.7.1"

lazy val root = (project in file("."))
  .settings(
    organization := "com.yugabyte",
    name := "quill",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.10",
    libraryDependencies ++= Seq(
      "org.http4s"     %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"     %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s"     %% "http4s-circe"        % Http4sVersion,
      "org.http4s"     %% "http4s-dsl"          % Http4sVersion,
      "io.circe"       %% "circe-generic"       % CirceVersion,
      "org.postgresql" % "postgresql"           % PostgreSQLVersion,
      "io.getquill"    %% "quill-jdbc"          % QuillVersion,
      "io.getquill"    %% "quill-codegen-jdbc"  % QuillCodegenVersion,
      "org.flywaydb"   % "flyway-core"          % FlywayVersion,
      "io.scalaland"   %% "chimney"             % ChimneyVersion,
      "org.specs2"     %% "specs2-core"         % Specs2Version % Test,
      "ch.qos.logback" % "logback-classic"      % LogbackVersion
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification",
  "-Xfatal-warnings",
)
