package com.github.xuwei_k

import unfiltered.request._
import unfiltered.response._
import java.net.URL
import sbt.Using

case class GhInfo(user:String,repo:String,branch:String = "master"){
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

class App extends unfiltered.filter.Plan {

  def intent = {
    case GET(Path("/")) =>
      view(<p> hello </p>)
    case GET(Path(Seg(user :: repo :: Nil))) =>
      tree(GhInfo(user,repo))
    case GET(Path(Seg(user :: repo :: branch :: Nil))) =>
      tree(GhInfo(user,repo,branch))
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
