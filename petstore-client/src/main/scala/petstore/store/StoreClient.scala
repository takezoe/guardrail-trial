package petstore.store
import _root_.petstore.Implicits._
import _root_.petstore.AkkaHttpImplicits._
import _root_.petstore.definitions._
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
object StoreClient {
  def apply(host: String = "http://petstore.swagger.io")(implicit httpClient: HttpRequest => Future[HttpResponse], ec: ExecutionContext, mat: Materializer): StoreClient = new StoreClient(host = host)(httpClient = httpClient, ec = ec, mat = mat)
  def httpClient(httpClient: HttpRequest => Future[HttpResponse], host: String = "http://petstore.swagger.io")(implicit ec: ExecutionContext, mat: Materializer): StoreClient = new StoreClient(host = host)(httpClient = httpClient, ec = ec, mat = mat)
}
class StoreClient(host: String = "http://petstore.swagger.io")(implicit httpClient: HttpRequest => Future[HttpResponse], ec: ExecutionContext, mat: Materializer) {
  val basePath: String = "/v2"
  private[this] def wrap[T: FromEntityUnmarshaller](resp: Future[HttpResponse]): EitherT[Future, Either[Throwable, HttpResponse], T] = {
    EitherT(resp.flatMap(resp => if (resp.status.isSuccess) {
      Unmarshal(resp.entity).to[T].map(Right.apply _)
    } else {
      FastFuture.successful(Left(Right(resp)))
    }).recover({
      case e: Throwable =>
        Left(Left(e))
    }))
  }
  def getInventory(headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], Map[String, Int]] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[Map[String, Int]](Marshal(HttpEntity.Empty).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.GET, uri = host + basePath + "/store/inventory", entity = entity, headers = allHeaders))
    })
  }
  def placeOrder(body: Order, headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], Order] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[Order](Marshal(body).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.POST, uri = host + basePath + "/store/order", entity = entity, headers = allHeaders))
    })
  }
  def getOrderById(orderId: Long, headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], Order] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[Order](Marshal(HttpEntity.Empty).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.GET, uri = host + basePath + "/store/order/" + Formatter.addPath(orderId), entity = entity, headers = allHeaders))
    })
  }
  def deleteOrder(orderId: Long, headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], IgnoredEntity] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[IgnoredEntity](Marshal(HttpEntity.Empty).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.DELETE, uri = host + basePath + "/store/order/" + Formatter.addPath(orderId), entity = entity, headers = allHeaders))
    })
  }
}