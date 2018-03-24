package petstore.store
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
import _root_.petstore.Implicits._
import _root_.petstore.AkkaHttpImplicits._
import _root_.petstore.definitions._
trait StoreHandler {
  def getInventory(respond: StoreResource.getInventoryResponse.type)(): scala.concurrent.Future[StoreResource.getInventoryResponse]
  def placeOrder(respond: StoreResource.placeOrderResponse.type)(body: Order): scala.concurrent.Future[StoreResource.placeOrderResponse]
  def getOrderById(respond: StoreResource.getOrderByIdResponse.type)(orderId: Long): scala.concurrent.Future[StoreResource.getOrderByIdResponse]
  def deleteOrder(respond: StoreResource.deleteOrderResponse.type)(orderId: Long): scala.concurrent.Future[StoreResource.deleteOrderResponse]
}
object StoreResource {
  import cats.syntax.either._
  def discardEntity(implicit mat: akka.stream.Materializer): Directive0 = extractRequest.flatMap { req => 
    req.discardEntityBytes().future
    Directive.Empty
  }
  implicit def jsonFSU[T: io.circe.Decoder]: Unmarshaller[String, T] = Unmarshaller[String, T] { implicit ev => 
    string => io.circe.Json.fromString(string).as[T].left.flatMap(err => io.circe.jawn.parse(string).flatMap(_.as[T])).fold(scala.concurrent.Future.failed _, scala.concurrent.Future.successful _)
  }
  def routes(handler: StoreHandler)(implicit mat: akka.stream.Materializer): Route = {
    (get & path("v2" / "store" / "inventory") & discardEntity) {
      complete(handler.getInventory(getInventoryResponse)())
    } ~ (post & path("v2" / "store" / "order") & entity(as[Order])) {
      body => complete(handler.placeOrder(placeOrderResponse)(body))
    } ~ (get & path("v2" / "store" / "order" / LongNumber) & discardEntity) {
      orderId => complete(handler.getOrderById(getOrderByIdResponse)(orderId))
    } ~ (delete & path("v2" / "store" / "order" / LongNumber) & discardEntity) {
      orderId => complete(handler.deleteOrder(deleteOrderResponse)(orderId))
    }
  }
  sealed abstract class getInventoryResponse(val statusCode: StatusCode)
  case class getInventoryResponseOK(value: Map[String, Int]) extends getInventoryResponse(StatusCodes.OK)
  object getInventoryResponse {
    implicit val getInventoryTRM: ToResponseMarshaller[getInventoryResponse] = Marshaller { implicit ec => 
      resp => getInventoryTR(resp)
    }
    implicit def getInventoryTR(value: getInventoryResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r @ getInventoryResponseOK(value) =>
        Marshal(value).to[ResponseEntity].map {
          entity => Marshalling.Opaque {
            () => HttpResponse(r.statusCode, entity = entity)
          } :: Nil
        }
    }
    def apply[T](value: T)(implicit ev: T => getInventoryResponse): getInventoryResponse = ev(value)
    implicit def OKEv(value: Map[String, Int]): getInventoryResponse = OK(value)
    def OK(value: Map[String, Int]): getInventoryResponse = getInventoryResponseOK(value)
  }
  sealed abstract class placeOrderResponse(val statusCode: StatusCode)
  case class placeOrderResponseOK(value: Order) extends placeOrderResponse(StatusCodes.OK)
  case object placeOrderResponseBadRequest extends placeOrderResponse(StatusCodes.BadRequest)
  object placeOrderResponse {
    implicit val placeOrderTRM: ToResponseMarshaller[placeOrderResponse] = Marshaller { implicit ec => 
      resp => placeOrderTR(resp)
    }
    implicit def placeOrderTR(value: placeOrderResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r @ placeOrderResponseOK(value) =>
        Marshal(value).to[ResponseEntity].map {
          entity => Marshalling.Opaque {
            () => HttpResponse(r.statusCode, entity = entity)
          } :: Nil
        }
      case r: placeOrderResponseBadRequest.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => placeOrderResponse): placeOrderResponse = ev(value)
    implicit def OKEv(value: Order): placeOrderResponse = OK(value)
    def OK(value: Order): placeOrderResponse = placeOrderResponseOK(value)
    def BadRequest: placeOrderResponse = placeOrderResponseBadRequest
  }
  sealed abstract class getOrderByIdResponse(val statusCode: StatusCode)
  case class getOrderByIdResponseOK(value: Order) extends getOrderByIdResponse(StatusCodes.OK)
  case object getOrderByIdResponseBadRequest extends getOrderByIdResponse(StatusCodes.BadRequest)
  case object getOrderByIdResponseNotFound extends getOrderByIdResponse(StatusCodes.NotFound)
  object getOrderByIdResponse {
    implicit val getOrderByIdTRM: ToResponseMarshaller[getOrderByIdResponse] = Marshaller { implicit ec => 
      resp => getOrderByIdTR(resp)
    }
    implicit def getOrderByIdTR(value: getOrderByIdResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r @ getOrderByIdResponseOK(value) =>
        Marshal(value).to[ResponseEntity].map {
          entity => Marshalling.Opaque {
            () => HttpResponse(r.statusCode, entity = entity)
          } :: Nil
        }
      case r: getOrderByIdResponseBadRequest.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
      case r: getOrderByIdResponseNotFound.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => getOrderByIdResponse): getOrderByIdResponse = ev(value)
    implicit def OKEv(value: Order): getOrderByIdResponse = OK(value)
    def OK(value: Order): getOrderByIdResponse = getOrderByIdResponseOK(value)
    def BadRequest: getOrderByIdResponse = getOrderByIdResponseBadRequest
    def NotFound: getOrderByIdResponse = getOrderByIdResponseNotFound
  }
  sealed abstract class deleteOrderResponse(val statusCode: StatusCode)
  case object deleteOrderResponseBadRequest extends deleteOrderResponse(StatusCodes.BadRequest)
  case object deleteOrderResponseNotFound extends deleteOrderResponse(StatusCodes.NotFound)
  object deleteOrderResponse {
    implicit val deleteOrderTRM: ToResponseMarshaller[deleteOrderResponse] = Marshaller { implicit ec => 
      resp => deleteOrderTR(resp)
    }
    implicit def deleteOrderTR(value: deleteOrderResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r: deleteOrderResponseBadRequest.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
      case r: deleteOrderResponseNotFound.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => deleteOrderResponse): deleteOrderResponse = ev(value)
    def BadRequest: deleteOrderResponse = deleteOrderResponseBadRequest
    def NotFound: deleteOrderResponse = deleteOrderResponseNotFound
  }
}