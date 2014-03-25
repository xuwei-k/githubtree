package githubtree

import unfiltered.request._
import unfiltered.response._
import java.net.URL
import sbt.Using
import httpz._
import ghscala._
import scalaz.{Ordering => _, One => _, Two => _, _}
import scalaz.Id.Id

object GithubApi{
  import RequestF._

  private def runOne[A](o: One[A], conf: Config): A =
    try {
      val s = ScalajHttp(conf(o.req)).asString
      o.decode(o.req, o.parse(o.req, s))
    } catch {
      case e: scalaj.http.HttpException =>
        o.error(Error.http(e))
    }

  val scalajInterpreter =
    new Interpreter[Id] {
      def go[A](a: RequestF[A]) = a match {
        case o @ One() =>
          runOne(o, Endo.idEndo)
        case t @ Two() =>
          t.f(run(t.x), run(t.y))
      }
    }.interpreter

  @inline final val GITHUB = "https://github.com/"

  def repositoryNames(user: String): Action[List[String]] =
    Github.repos(user).map(_.map(_.name))

  def branches(user: String, repository: String): Action[List[String]] =
    Github.branches(user, repository).map(_.map(_.name))

  def defaultBranch(user: String, repository: String): Action[String] =
    Github.repo(user, repository).map(_.master)

  def getInfo(user: String): Action[List[Repository]] = {
    import syntax.traverse._
    import std.list._
    for{
      names <- repositoryNames(user)
      r <- names.map(n =>
        branches(user, n).map(b => Repository(n, b))
      ).sequenceU
    } yield r
  }

}

import GithubApi._

final case class Repository(name: String, branches: List[String])

final case class GhInfo(user: String, repo: String)(branch: String = GithubApi.defaultBranch(user, repo).interpretBy(scalajInterpreter).getOrElse("master")){

  val url = new URL(
    s"${GITHUB}${user}/${repo}/zipball/${branch}"
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

  def showUserRepos(user: String, repositories: List[Repository]) = {
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

  val sortBySize = (f: FileInfo) => f.size

  def sortBySize_?(p: Params.Map) =
    p.get("sort").flatMap{_.headOption.map{"size"==}}.getOrElse(false)

  def sortFunc(p: Params.Map) =
    if(sortBySize_?(p)) Some(sortBySize) else None

  object Hub {
    def unapply(p: Params.Map): Boolean =
      p.isDefinedAt("hub")
  }

  val main: unfiltered.filter.Plan.Intent = {
    case GET(Path("/")) =>
      view(<p> hello </p>)
    case GET(Path(Seg(user :: Nil)) & Params(Hub())) =>
      Github.repos(user).map( repos =>
        PlainTextContent ~> ResponseString(
          repos.filterNot(_.fork).map(r =>
            "hub clone " + user + "/" + r.name
          ).mkString(" &&\n")
        )
      ).mapRequest(
        Endo(_.addParam("per_page", "100"))
      ).interpretBy(
        scalajInterpreter
      ).valueOr(throw _)
    case GET(Path(Seg(user :: Nil))) =>
      GithubApi.getInfo(user).map(repos =>
        view(showUserRepos(user, repos))
      ).interpretBy(scalajInterpreter).valueOr{
        case httpz.Error.Http(e) =>
          System.err.println(e.toString)
          throw e
        case e =>
          System.err.println(e)
          throw new Exception(e.toString)
      }
    case GET(Path(Seg(user :: repo :: Nil)) & Params(p)) =>
      tree(GhInfo(user, repo)(), sortFunc(p))
    case GET(Path(Seg(user :: repo :: branch :: Nil)) & Params(p)) =>
      tree(GhInfo(user, repo)(branch), sortFunc(p))
  }

  override def intent = {
    case request if main.isDefinedAt(request) =>
    try{
      main(request)
    }catch{
      case e: Throwable =>
        e.printStackTrace
        System.err.println(e)
        System.err.println(e.getStackTrace.mkString("\n"))
        ResponseString(e.toString + "\n\n" + e.getStackTrace.mkString("\n")) ~> InternalServerError
    }
  }

  val files = { (url: URL) =>
    Using.urlInputStream(url){ in =>
      Using.zipInputStream(in){ zipIn =>
        Iterator.continually(zipIn.getNextEntry).takeWhile(null ne).map{ f =>
          FileInfo(! f.isDirectory, f.toString, f.getSize)
        }.toList
      }
    }
  }

  val toHtmlList = { i: GhInfo =>
    {files: List[FileInfo] =>
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

  def tree[A](info: GhInfo, sort: Option[FileInfo => A])(implicit ord: Ordering[A]) = {
    val fileList = files(info.url)
    val data = sort.map{f => fileList.sortBy(f)}.getOrElse(fileList)
    view(toHtmlList(info)(data))
  }
}
