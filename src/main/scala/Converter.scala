package githubtree

import com.google.cloud.functions.{HttpFunction, HttpRequest, HttpResponse}
import java.io.OutputStream
import scala.jdk.CollectionConverters._
import java.util.Collections

object Converter {
  def request(req: HttpRequest): unfiltered.request.HttpRequest[HttpRequest] =
    new unfiltered.request.HttpRequest[HttpRequest](req) {
      override def headerNames: Iterator[String] =
        req.getHeaders.asScala.keysIterator
      override def headers(name: String): Iterator[String] =
        req.getHeaders.getOrDefault(name, Collections.emptyList()).iterator().asScala
      override def inputStream: java.io.InputStream =
        req.getInputStream
      override def isSecure: Boolean =
        req.getUri.startsWith("https")
      override def method: String =
        req.getMethod
      override def parameterNames: Iterator[String] =
        req.getQueryParameters.keySet().asScala.iterator
      override def parameterValues(param: String): Seq[String] =
        req.getQueryParameters.getOrDefault(param, Collections.emptyList()).asScala.toSeq
      override def protocol: String =
        req.getUri.split(':').headOption.getOrElse("")
      override def reader: java.io.Reader =
        req.getReader
      override def remoteAddr: String =
        "" // TODO
      override def uri: String =
        req.getPath + req.getQuery.map("?" + _).orElse("")
    }

  def response(res: HttpResponse): unfiltered.response.HttpResponse[HttpResponse] = {
    new unfiltered.response.HttpResponse[HttpResponse](res) {
      private[this] var code: Option[Int] = None
      override def status(statusCode: Int): Unit = {
        code = Some(statusCode)
        res.setStatusCode(statusCode)
      }

      override def status: Int =
        code.getOrElse(200)

      override def outputStream: OutputStream =
        res.getOutputStream

      override def redirect(url: String): Unit =
        ???

      override def header(name: String, value: String): Unit =
        res.appendHeader(name, value)
    }
  }

  def intent(i: unfiltered.Cycle.Intent[HttpRequest, HttpResponse]): HttpFunction =
    (req: HttpRequest, res: HttpResponse) => {
      val request = Converter.request(req)
      i.lift.apply(request) match {
        case Some(func) =>
          func.apply(Converter.response(res)).underlying
        case None =>
          res.setStatusCode(404)
          res.getWriter.write("not found")
      }
    }
}

abstract class UnfilteredCouldFunction extends HttpFunction {
  override final def service(req: HttpRequest, res: HttpResponse): Unit = {
    Converter.intent(intent).service(req, res)
  }
  def intent: unfiltered.Cycle.Intent[HttpRequest, HttpResponse]
}
