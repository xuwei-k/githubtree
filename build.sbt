val unfilteredVersion = "0.12.0-M1"

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
  ("com.google.cloud.functions" % "functions-framework-api" % "1.0.4") ::
  ("ws.unfiltered" %% "unfiltered" % unfilteredVersion) ::
  ("com.github.xuwei-k" %% "ghscala" % "0.6.0") ::
  ("com.github.xuwei-k" %% "httpz-native" % "0.6.1") ::
  ("com.chuusai" %% "shapeless" % "2.3.3") ::
  Nil
)

assembly / assemblyOutputPath := file("output") / "githubtree-assembly.jar"
