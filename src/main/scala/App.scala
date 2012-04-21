package com.github.xuwei_k

import unfiltered.request._
import unfiltered.response._
import java.net.URL
import sbt.Using

object GithubApi{
  import scala.io.Source
  import scala.util.parsing.json.JSON.parseFull

  def getJson[T](url:String):T = {
    val str  = Source.fromURL(url,"UTF-8").mkString
    parseFull(str).get.asInstanceOf[T]
  }

  def repositories(user:String) = {
    type JsonType = Map[String,List[Map[String,String]]]
    val json = getJson[JsonType]("https://github.com/api/v2/json/repos/show/" + user)
    json("repositories").reverse
  }

  def repositoryNames(user:String):List[String] = {
    repositories(user).map{_.apply("url").replace("https://github.com/" + user + "/","")}
  }

  def branches(user:String,repository:String):List[String] = {
    val json = getJson[Map[String,Map[String,String]]]("https://github.com/api/v2/json/repos/show/"+user+"/"+repository+"/branches") 
    json("branches").keys.toList
  }

  def defaultBranch(user:String,repository:String):String = {
    repositories(user).find{_.apply("name") == repository}.get.getOrElse("master_branch","master")
  }

  def getInfo(user:String) =
    repositoryNames(user).map{ n =>
      Repository(n,branches(user,n))
    }

}

case class Repository(name:String,branches:List[String])

case class GhInfo(user:String,repo:String)(branch:String = GithubApi.defaultBranch(user,repo)){
  val github = "https://github.com/"
  val url = new URL(
    <x>{github}{user}/{repo}/zipball/{branch}</x>.text
  )

  def html(isFile:Boolean,name:String) = {
    val path = name.split('/').tail.mkString("/")
    val link = {
      <x>{github}{user}/{repo}/{if(isFile)"blob"else"tree"}/{branch}/{path}</x>.text
    }
    <a href={link}>{path}</a>
  }
}

object App{
  @inline final val GITHUB = "https://github.com/"
}

class App extends unfiltered.filter.Plan {
  import App._

  def showUserRepos(user:String,repositories:List[Repository]) = {
    <div>
      <h1><a href={GITHUB + user}>{user}</a> repositories</h1>
      <div>{
        repositories.map{case Repository(name,branches) =>
          <h2>{name}</h2>
          <ul>{
            branches.map{ branch =>
              val ghLink = <x>{GITHUB}{user}/{name}/tree/{branch}</x>.text
              val link = <x>{user}/{name}/{branch}</x>.text
              <li>
                <span style="font-size:large;"><a href={link}>{branch}</a></span>
                <span style="font-size:x-small;"><a href={ghLink}>goto github</a></span>
              </li>
            }
          }</ul>
        }
      }</div>
    </div>
  }

  def intent = {
    case GET(Path("/")) =>
      view(<p> hello </p>)
    case GET(Path(Seg(user :: Nil))) =>
      view(showUserRepos(user,GithubApi.getInfo(user)))
    case GET(Path(Seg(user :: repo :: Nil))) =>
      tree(GhInfo(user,repo)())
    case GET(Path(Seg(user :: repo :: branch :: Nil))) =>
      tree(GhInfo(user,repo)(branch))
  }

  val files = { (url:URL) =>
    Using.urlInputStream(url){ in =>
      Using.zipInputStream(in){ zipIn =>
        Iterator.continually(zipIn.getNextEntry).takeWhile(null ne).map{ f =>
          (! f.isDirectory) -> f.toString
        }.toList
      }
    }
  }

  val toHtmlList = { i:GhInfo =>
    {files:List[(Boolean,String)] =>
      <ul>{
        files.map{f => <li>{(i.html _).tupled(f)}</li>}
      }</ul>
    }
  }

  val view = { (body: scala.xml.NodeSeq) =>
    Html(
     <html>
      <head>
        <title>githubtree</title>
      </head>
      <body>
        <div>
         { body }
        </div>
      </body>
     </html>
   )
  }

  def tree(info:GhInfo) = view(toHtmlList(info)(files(info.url)))
}
