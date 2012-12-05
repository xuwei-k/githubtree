package com.github.xuwei_k

import unfiltered.request._
import unfiltered.response._
import java.net.URL
import sbt.Using
import com.github.xuwei_k.ghscala.{GhScala}

object GithubApi{

  @inline final val GITHUB = "https://github.com/"

  def repositoryNames(user:String):List[String] = GhScala.repos(user).map(_.name)

  def branches(user:String,repository:String):List[String] = {
    GhScala.refs(user,repository).map{_.name}
  }

  def defaultBranch(user:String,repository:String):String =
    GhScala.repo(user,repository).master

  def getInfo(user:String):List[Repository] =
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
    <span><a target="_blank" href={link}>{path}</a> {if(isFile) size + " bytes" else ""}</span>
  }
}

case class FileInfo(isFile:Boolean,name:String,size:Long)


class App extends unfiltered.filter.Plan {
  import GithubApi.GITHUB

  def showUserRepos(user:String,repositories:List[Repository]) = {
    <div>
      <h1><a target="_blank" href={GITHUB + user}>{user}</a> repositories</h1>
      <div>{
        repositories.map{case Repository(name,branches) =>
          <h2>{name}</h2>
          <ul>{
            branches.map{ branch =>
              val ghLink = <x>{GITHUB}{user}/{name}/tree/{branch}</x>.text
              val link = <x>/{user}/{name}/{branch}</x>.text
              <li>
                <span style="font-size:large;"><a target="_blank" href={link}>{branch}</a></span>
                <span style="font-size:x-small;"><a target="_blank" href={ghLink}>goto github</a></span>
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

  val main:unfiltered.filter.Plan.Intent = {
    case GET(Path("/")) =>
      view(<p> hello </p>)
    case GET(Path(Seg(user :: Nil))) =>
      view(showUserRepos(user,GithubApi.getInfo(user)))
    case GET(Path(Seg(user :: repo :: Nil)) & Params(p)) =>
      tree(GhInfo(user,repo)(),sortFunc(p))
    case GET(Path(Seg(user :: repo :: branch :: Nil)) & Params(p)) =>
      tree(GhInfo(user,repo)(branch),sortFunc(p))
  }

  override def intent = {
    case request if main.isDefinedAt(request) =>
    try{
      main(request)
    }catch{
      case e:Throwable =>
        e.printStackTrace
        ResponseString(e.toString + "\n\n" + e.getStackTrace.mkString("\n")) ~> InternalServerError
    }
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
        <meta name="robots" content="noindex,nofollow" />
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
