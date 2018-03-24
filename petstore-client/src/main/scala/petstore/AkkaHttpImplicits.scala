package petstore
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import cats.syntax.either._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.{ Unmarshal, Unmarshaller, FromEntityUnmarshaller }
import akka.http.scaladsl.marshalling.{ Marshal, Marshaller, Marshalling, ToEntityMarshaller, ToResponseMarshaller }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ Directive, Directive0, Route }
import akka.http.scaladsl.util.FastFuture
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import cats.data.EitherT
import scala.concurrent.{ ExecutionContext, Future }
import scala.language.implicitConversions
import cats.implicits._
import cats.data.EitherT
import scala.concurrent.Future
import petstore.Implicits._
object AkkaHttpImplicits {
  private[this] def pathEscape(s: String): String = Uri.Path.Segment.apply(s, Uri.Path.Empty).toString
  implicit def addShowablePath[T](implicit ev: Show[T]): AddPath[T] = AddPath.build[T](v => pathEscape(ev.show(v)))
  private[this] def argEscape(k: String, v: String): String = Uri.Query.apply((k, v)).toString
  implicit def addShowableArg[T](implicit ev: Show[T]): AddArg[T] = AddArg.build[T](key => v => argEscape(key, ev.show(v)))
  type HttpClient = HttpRequest => Future[HttpResponse]
  type TraceBuilder = String => HttpClient => HttpClient
  implicit final def jsonMarshaller(implicit printer: Printer = Printer.noSpaces): ToEntityMarshaller[io.circe.Json] = Marshaller.withFixedContentType(MediaTypes.`application/json`) {
    json => HttpEntity(MediaTypes.`application/json`, printer.pretty(json))
  }
  implicit final def jsonEntityMarshaller[A](implicit J: io.circe.Encoder[A], printer: Printer = Printer.noSpaces): ToEntityMarshaller[A] = jsonMarshaller(printer).compose(J.apply)
  implicit final val jsonUnmarshaller: FromEntityUnmarshaller[io.circe.Json] = Unmarshaller.byteStringUnmarshaller.forContentTypes(MediaTypes.`application/json`).map({
    case ByteString.empty =>
      throw Unmarshaller.NoContentException
    case data =>
      jawn.parseByteBuffer(data.asByteBuffer).fold(throw _, identity)
  })
  implicit def jsonEntityUnmarshaller[A](implicit J: io.circe.Decoder[A]): FromEntityUnmarshaller[A] = {
    def decode(json: io.circe.Json) = J.decodeJson(json).fold(throw _, identity)
    jsonUnmarshaller.map(decode)
  }
  implicit val ignoredUnmarshaller: FromEntityUnmarshaller[IgnoredEntity] = Unmarshaller.strict(_ => IgnoredEntity.empty)
}