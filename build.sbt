organization := "com.github.xuwei_k"

name := "githubtree"

version := "0.1.0-SNAPSHOT"

sbtappengine.Plugin.webSettings

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-filter" % "0.6.1",
  "net.databinder" %% "unfiltered-spec" % "0.6.1" % "test"
) ++ Seq( // local testing
  "javax.servlet" % "servlet-api" % "2.3" % "provided",
   "org.eclipse.jetty" % "jetty-webapp" % "7.4.5.v20110725" % "container"
)

libraryDependencies <+= sbtDependency

resolvers ++= Seq(
 "jboss" at  "https://repository.jboss.org/nexus/content/groups/public/"
)




