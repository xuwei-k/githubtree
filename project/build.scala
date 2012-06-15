import sbt._,Keys._

object build extends Build{

  val UF = "0.6.3"

  lazy val root = Project(
    "githubtree",
    file(".")
  ).settings(
    sbtappengine.Plugin.webSettings ++ Seq(
      organization := "com.github.xuwei-k",
      version := "0.1.0-SNAPSHOT",
      scalaVersion := "2.9.2",
      libraryDependencies ++= Seq(
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
    uri("git://github.com/xuwei-k/ghscala.git#da367f3bca5a4")
  )

}

