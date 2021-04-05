val unfilteredVersion = "0.10.1"

name := "githubtree"

organization := "com.github.xuwei-k"

licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.13.5"

scalacOptions ++= (
  "-language:postfixOps" ::
  "-language:implicitConversions" ::
  "-language:higherKinds" ::
  "-language:existentials" ::
  "-deprecation" ::
  "-unchecked" ::
  "-Xlint" ::
  "-Ywarn-unused" ::
  Nil
)

fullResolvers ~= {_.filterNot(_.name == "jcenter")}

libraryDependencies ++= (
  ("ws.unfiltered" %% "unfiltered-filter" % unfilteredVersion) ::
  ("com.github.xuwei-k" %% "ghscala" % "0.6.0") ::
  ("com.github.xuwei-k" %% "httpz-native" % "0.7.0") ::
  ("com.chuusai" %% "shapeless" % "2.3.3") ::
  ("javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided") ::
  ("org.scala-sbt" %% "io" % "1.5.0") ::
  Nil
)
