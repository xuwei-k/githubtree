import sbt._,Keys._

object build extends Build{

  // https://github.com/unfiltered/unfiltered/blob/v0.8.1/project/common.scala#L6
  // https://github.com/unfiltered/unfiltered/blob/v0.8.2/project/common.scala#L6
  // https://code.google.com/p/googleappengine/issues/detail?id=3091
  private final val unfilteredVersion = "0.8.1"

  lazy val root = Project(
    "githubtree",
    file(".")
  ).settings(
    sbtappengine.Plugin.webSettings ++ Seq(
      organization := "com.github.xuwei-k",
      licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
      version := "0.1.0-SNAPSHOT",
      scalaVersion := "2.11.4",
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
      ),
      resolvers += Opts.resolver.sonatypeReleases,
      resolvers += Resolver.url("typesafe", url("http://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns),
      libraryDependencies ++= (
        ("net.databinder" %% "unfiltered-filter" % unfilteredVersion) ::
        ("com.github.xuwei-k" %% "ghscala" % "0.2.14") ::
        ("com.github.xuwei-k" %% "httpz-native" % "0.2.14") ::
        ("com.chuusai" %% "shapeless" % "2.0.0") ::
        ("javax.servlet" % "servlet-api" % "2.3" % "provided") ::
        ("org.eclipse.jetty" % "jetty-webapp" % "7.4.5.v20110725" % "container") ::
        ("org.scala-sbt" %% "io" % sbtVersion.value) ::
        Nil
      ),
      resolvers ++= Seq(
       "jboss" at "https://repository.jboss.org/nexus/content/groups/public/"
      )
    ) :_*
  )

}

