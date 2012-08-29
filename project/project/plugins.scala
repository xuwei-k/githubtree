import sbt._

object PluginDef extends Build {
  lazy val root = Project("plugins", file(".")) dependsOn(
    uri("git://github.com/sbt/sbt-appengine#f6b72a97d6e8c1e49a064f1ba37f9b9a9ff6f5d0")
  )
}

