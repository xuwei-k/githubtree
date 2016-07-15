// https://github.com/unfiltered/unfiltered/blob/v0.8.1/project/common.scala#L6
// https://github.com/unfiltered/unfiltered/blob/v0.8.2/project/common.scala#L6
// https://code.google.com/p/googleappengine/issues/detail?id=3091
val unfilteredVersion = "0.8.1"

name := "githubtree"

organization := "com.github.xuwei-k"

licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.8"

scalacOptions ++= (
  "-language:postfixOps" ::
  "-language:implicitConversions" ::
  "-language:higherKinds" ::
  "-language:existentials" ::
  "-deprecation" ::
  "-unchecked" ::
  "-Xlint" ::
  "-Ywarn-unused-import" ::
  "-Ywarn-unused" ::
  Nil
)

resolvers += Opts.resolver.sonatypeReleases

fullResolvers ~= {_.filterNot(_.name == "jcenter")}

resolvers += Resolver.url("typesafe", url("http://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns)

libraryDependencies ++= (
  ("net.databinder" %% "unfiltered-filter" % unfilteredVersion) ::
  ("com.github.xuwei-k" %% "ghscala" % "0.4.0") ::
  ("com.github.xuwei-k" %% "httpz-native" % "0.4.0") ::
  ("com.chuusai" %% "shapeless" % "2.3.1") ::
  ("javax.servlet" % "servlet-api" % "2.3" % "provided") ::
  ("org.scala-sbt" %% "io" % sbtVersion.value) ::
  Nil
)
