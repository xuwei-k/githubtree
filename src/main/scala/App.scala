package com.github.xuwei_k

import unfiltered.request._
import unfiltered.response._
import java.net.URL
import sbt.Using

object GithubApi{
  import scala.io.Source
  import scala.util.parsing.json.JSON.parseFull

  @inline final val GITHUB = "https://github.com/"
  @inline private final val API2 = GITHUB + "api/v2/json/"

  def getJson[T](url:String):T = {
    val str  = Source.fromURL(url,"UTF-8").mkString
    parseFull(str).get.asInstanceOf[T]
  }

  def repositories(user:String) = {
    type JsonType = Map[String,List[Map[String,String]]]
    val json = getJson[JsonType](API2 + "repos/show/" + user)
    json("repositories").reverse
  }

  def repositoryNames(user:String):List[String] = {
    repositories(user).map{_.apply("url").replace(GITHUB + user + "/","")}
  }

  def branches(user:String,repository:String):List[String] = {
    val json = getJson[Map[String,Map[String,String]]](API2 + "repos/show/"+user+"/"+repository+"/branches")
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
  import GithubApi._

  val url = new URL(
    <x>{GITHUB}{user}/{repo}/zipball/{branch}</x>.text
  )

  def html(f:FileInfo) = {
    import f._
    val path = name.split('/').tail.mkString("/")
    val link = {
      <x>{GITHUB}{user}/{repo}/{if(isFile)"blob"else"tree"}/{branch}/{path}</x>.text
    }
    <span><a href={link}>{path}</a> {if(isFile) size + " bytes" else ""}</span>
  }
}

case class FileInfo(isFile:Boolean,name:String,size:Long)


class App extends unfiltered.filter.Plan {
  import GithubApi.GITHUB

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

  val sortBySize = (f:FileInfo) => f.size

  def sortBySize_?(p:Params.Map) =
    p.get("sort").flatMap{_.headOption.map{"size"==}}.getOrElse(false)

  def sortFunc(p:Params.Map) =
    if(sortBySize_?(p)) Some(sortBySize) else None

  def intent = {
    case GET(Path("/")) =>
      view(<p> hello </p>)
    case GET(Path(Seg(user :: Nil))) =>
      view(showUserRepos(user,GithubApi.getInfo(user)))
    case GET(Path(Seg(user :: repo :: Nil)) & Params(p)) =>
      tree(GhInfo(user,repo)(),sortFunc(p))
    case GET(Path(Seg(user :: repo :: branch :: Nil)) & Params(p)) =>
      tree(GhInfo(user,repo)(branch),sortFunc(p))
  }

  val files = { (url:URL) =>
    Using.urlInputStream(url){ in =>
      Using.zipInputStream(in){ zipIn =>
        Iterator.continually(zipIn.getNextEntry).takeWhile(null ne).map{ f =>
          FileInfo(! f.isDirectory, f.toString, f.getSize)
        }.toList
      }
    }
  }

  val toHtmlList = { i:GhInfo =>
    {files:List[FileInfo] =>
      <ul>{
        files.map{f => <li>{i.html(f)}</li>}
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

  def tree[A](info:GhInfo,sort:Option[FileInfo => A])(implicit ord:Ordering[A]) = {
    val fileList = files(info.url)
    val data = sort.map{f => fileList.sortBy(f)}.getOrElse(fileList)
    view(toHtmlList(info)(data))
  }
}
