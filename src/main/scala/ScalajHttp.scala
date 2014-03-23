package ghscala

import scalaj.http._
import scalaz.Endo

object ScalajHttp{

  val OPTIONS = List(HttpOptions.connTimeout(30000), HttpOptions.readTimeout(30000))

  def apply(req: httpz.Request): Http.Request = {
    val r0 = req.method match {
      case "GET"      => get(req.url)
      case "POST"     => post(req.url)
    }
    val r1 = r0.params(req.params.toList)
    req.basicAuth.fold(r1){case (user, pass) => r1.auth(user, pass)}
  }

  private def defaultOptions(r: Http.Request) =
    r.options(OPTIONS).header("User-Agent", "scalaj-http")

  private def get(url:String): Http.Request = defaultOptions(Http(url))

  private def post(url:String): Http.Request = defaultOptions(Http.post(url))

}

