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
      scalaVersion := "2.10.4-RC3",
      libraryDependencies ++= Seq(
//        "com.github.xuwei-k" %% "ghscala" % "0.2.4",
        "net.databinder" %% "dispatch-http" % "0.8.10",
        "net.databinder" %% "unfiltered-filter" % UF,
        "net.databinder" %% "unfiltered-spec" % UF % "test",
        "javax.servlet" % "servlet-api" % "2.3" % "provided",
        "org.eclipse.jetty" % "jetty-webapp" % "7.4.5.v20110725" % "container"
      ),
      libraryDependencies <+= sbtDependency,
      resolvers ++= Seq(
       "jboss" at "https://repository.jboss.org/nexus/content/groups/public/"
      )
    ) :_*
  ).dependsOn(
    uri("git://github.com/xuwei-k/ghscala.git#74064df9b")
  )

}

