import sbt._,Keys._

object build extends Build{

  val UF = "0.7.1"

  lazy val root = Project(
    "githubtree",
    file(".")
  ).settings(
    sbtappengine.Plugin.webSettings ++ Seq(
      organization := "com.github.xuwei-k",
      licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
      version := "0.1.0-SNAPSHOT",
      scalaVersion := "2.10.4",
      scalacOptions ++= Seq("-deprecation", "-unchecked", "-language:_"),
      libraryDependencies ++= Seq(
        // could not use new version scalaj-http
        // Google App Engine does not allow reflection and `java.nex.Proxy`
        "org.scalaj"  %% "scalaj-http" % "0.3.6",
        "net.databinder" %% "unfiltered-filter" % UF,
        "net.databinder" %% "unfiltered-spec" % UF % "test",
        "com.github.xuwei-k" %% "ghscala" % "0.2.9",
        "com.github.xuwei-k" %% "httpz" % "0.2.9",
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

