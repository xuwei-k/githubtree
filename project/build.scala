import sbt._,Keys._

object build extends Build{

  lazy val root = Project(
    "githubtree",
    file(".")
  ).settings(
    sbtappengine.Plugin.webSettings ++ Seq(
      organization := "com.github.xuwei-k",
      licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
      version := "0.1.0-SNAPSHOT",
      scalaVersion := "2.10.4",
      scalacOptions ++= (
        "-language:postfixOps" ::
        "-language:implicitConversions" ::
        "-language:higherKinds" ::
        "-language:existentials" ::
        "-deprecation" ::
        "-unchecked" ::
        "-Xlint" ::
        Nil
      ),
      resolvers += Opts.resolver.sonatypeReleases,
      libraryDependencies ++= Seq(
        "net.databinder" %% "unfiltered-filter" % "0.8.0",
        "com.github.xuwei-k" %% "ghscala" % "0.2.13",
        "com.github.xuwei-k" %% "httpz-native" % "0.2.13",
        "com.chuusai" %% "shapeless" % "2.0.0" cross CrossVersion.full,
        "javax.servlet" % "servlet-api" % "2.3" % "provided",
        "org.eclipse.jetty" % "jetty-webapp" % "7.4.5.v20110725" % "container"
      ),
      libraryDependencies <+= sbtDependency,
      resolvers ++= Seq(
       "jboss" at "https://repository.jboss.org/nexus/content/groups/public/"
      )
    ) :_*
  )

}

