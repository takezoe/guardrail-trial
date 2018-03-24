package petstore.user
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
trait UserHandler {
  def createUser(respond: UserResource.createUserResponse.type)(body: User): scala.concurrent.Future[UserResource.createUserResponse]
  def createUsersWithArrayInput(respond: UserResource.createUsersWithArrayInputResponse.type)(body: IndexedSeq[User]): scala.concurrent.Future[UserResource.createUsersWithArrayInputResponse]
  def createUsersWithListInput(respond: UserResource.createUsersWithListInputResponse.type)(body: IndexedSeq[User]): scala.concurrent.Future[UserResource.createUsersWithListInputResponse]
  def loginUser(respond: UserResource.loginUserResponse.type)(username: String, password: String): scala.concurrent.Future[UserResource.loginUserResponse]
  def logoutUser(respond: UserResource.logoutUserResponse.type)(): scala.concurrent.Future[UserResource.logoutUserResponse]
  def getUserByName(respond: UserResource.getUserByNameResponse.type)(username: String): scala.concurrent.Future[UserResource.getUserByNameResponse]
  def updateUser(respond: UserResource.updateUserResponse.type)(username: String, body: User): scala.concurrent.Future[UserResource.updateUserResponse]
  def deleteUser(respond: UserResource.deleteUserResponse.type)(username: String): scala.concurrent.Future[UserResource.deleteUserResponse]
}
object UserResource {
  import cats.syntax.either._
  def discardEntity(implicit mat: akka.stream.Materializer): Directive0 = extractRequest.flatMap { req => 
    req.discardEntityBytes().future
    Directive.Empty
  }
  implicit def jsonFSU[T: io.circe.Decoder]: Unmarshaller[String, T] = Unmarshaller[String, T] { implicit ev => 
    string => io.circe.Json.fromString(string).as[T].left.flatMap(err => io.circe.jawn.parse(string).flatMap(_.as[T])).fold(scala.concurrent.Future.failed _, scala.concurrent.Future.successful _)
  }
  def routes(handler: UserHandler)(implicit mat: akka.stream.Materializer): Route = {
    (post & path("v2" / "user") & entity(as[User])) {
      body => complete(handler.createUser(createUserResponse)(body))
    } ~ (post & path("v2" / "user" / "createWithArray") & entity(as[IndexedSeq[User]])) {
      body => complete(handler.createUsersWithArrayInput(createUsersWithArrayInputResponse)(body))
    } ~ (post & path("v2" / "user" / "createWithList") & entity(as[IndexedSeq[User]])) {
      body => complete(handler.createUsersWithListInput(createUsersWithListInputResponse)(body))
    } ~ (get & path("v2" / "user" / "login") & (parameter(Symbol("username").as[String]) & parameter(Symbol("password").as[String])) & discardEntity) {
      (username, password) => complete(handler.loginUser(loginUserResponse)(username, password))
    } ~ (get & path("v2" / "user" / "logout") & discardEntity) {
      complete(handler.logoutUser(logoutUserResponse)())
    } ~ (get & path("v2" / "user" / Segment) & discardEntity) {
      username => complete(handler.getUserByName(getUserByNameResponse)(username))
    } ~ (put & path("v2" / "user" / Segment) & entity(as[User])) {
      (username, body) => complete(handler.updateUser(updateUserResponse)(username, body))
    } ~ (delete & path("v2" / "user" / Segment) & discardEntity) {
      username => complete(handler.deleteUser(deleteUserResponse)(username))
    }
  }
  sealed abstract class createUserResponse(val statusCode: StatusCode)
  case object createUserResponseOK extends createUserResponse(StatusCodes.OK)
  object createUserResponse {
    implicit val createUserTRM: ToResponseMarshaller[createUserResponse] = Marshaller { implicit ec => 
      resp => createUserTR(resp)
    }
    implicit def createUserTR(value: createUserResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r: createUserResponseOK.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => createUserResponse): createUserResponse = ev(value)
    def OK: createUserResponse = createUserResponseOK
  }
  sealed abstract class createUsersWithArrayInputResponse(val statusCode: StatusCode)
  case object createUsersWithArrayInputResponseOK extends createUsersWithArrayInputResponse(StatusCodes.OK)
  object createUsersWithArrayInputResponse {
    implicit val createUsersWithArrayInputTRM: ToResponseMarshaller[createUsersWithArrayInputResponse] = Marshaller { implicit ec => 
      resp => createUsersWithArrayInputTR(resp)
    }
    implicit def createUsersWithArrayInputTR(value: createUsersWithArrayInputResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r: createUsersWithArrayInputResponseOK.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => createUsersWithArrayInputResponse): createUsersWithArrayInputResponse = ev(value)
    def OK: createUsersWithArrayInputResponse = createUsersWithArrayInputResponseOK
  }
  sealed abstract class createUsersWithListInputResponse(val statusCode: StatusCode)
  case object createUsersWithListInputResponseOK extends createUsersWithListInputResponse(StatusCodes.OK)
  object createUsersWithListInputResponse {
    implicit val createUsersWithListInputTRM: ToResponseMarshaller[createUsersWithListInputResponse] = Marshaller { implicit ec => 
      resp => createUsersWithListInputTR(resp)
    }
    implicit def createUsersWithListInputTR(value: createUsersWithListInputResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r: createUsersWithListInputResponseOK.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => createUsersWithListInputResponse): createUsersWithListInputResponse = ev(value)
    def OK: createUsersWithListInputResponse = createUsersWithListInputResponseOK
  }
  sealed abstract class loginUserResponse(val statusCode: StatusCode)
  case class loginUserResponseOK(value: String) extends loginUserResponse(StatusCodes.OK)
  case object loginUserResponseBadRequest extends loginUserResponse(StatusCodes.BadRequest)
  object loginUserResponse {
    implicit val loginUserTRM: ToResponseMarshaller[loginUserResponse] = Marshaller { implicit ec => 
      resp => loginUserTR(resp)
    }
    implicit def loginUserTR(value: loginUserResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r @ loginUserResponseOK(value) =>
        Marshal(value).to[ResponseEntity].map {
          entity => Marshalling.Opaque {
            () => HttpResponse(r.statusCode, entity = entity)
          } :: Nil
        }
      case r: loginUserResponseBadRequest.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => loginUserResponse): loginUserResponse = ev(value)
    implicit def OKEv(value: String): loginUserResponse = OK(value)
    def OK(value: String): loginUserResponse = loginUserResponseOK(value)
    def BadRequest: loginUserResponse = loginUserResponseBadRequest
  }
  sealed abstract class logoutUserResponse(val statusCode: StatusCode)
  case object logoutUserResponseOK extends logoutUserResponse(StatusCodes.OK)
  object logoutUserResponse {
    implicit val logoutUserTRM: ToResponseMarshaller[logoutUserResponse] = Marshaller { implicit ec => 
      resp => logoutUserTR(resp)
    }
    implicit def logoutUserTR(value: logoutUserResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r: logoutUserResponseOK.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => logoutUserResponse): logoutUserResponse = ev(value)
    def OK: logoutUserResponse = logoutUserResponseOK
  }
  sealed abstract class getUserByNameResponse(val statusCode: StatusCode)
  case class getUserByNameResponseOK(value: User) extends getUserByNameResponse(StatusCodes.OK)
  case object getUserByNameResponseBadRequest extends getUserByNameResponse(StatusCodes.BadRequest)
  case object getUserByNameResponseNotFound extends getUserByNameResponse(StatusCodes.NotFound)
  object getUserByNameResponse {
    implicit val getUserByNameTRM: ToResponseMarshaller[getUserByNameResponse] = Marshaller { implicit ec => 
      resp => getUserByNameTR(resp)
    }
    implicit def getUserByNameTR(value: getUserByNameResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r @ getUserByNameResponseOK(value) =>
        Marshal(value).to[ResponseEntity].map {
          entity => Marshalling.Opaque {
            () => HttpResponse(r.statusCode, entity = entity)
          } :: Nil
        }
      case r: getUserByNameResponseBadRequest.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
      case r: getUserByNameResponseNotFound.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => getUserByNameResponse): getUserByNameResponse = ev(value)
    implicit def OKEv(value: User): getUserByNameResponse = OK(value)
    def OK(value: User): getUserByNameResponse = getUserByNameResponseOK(value)
    def BadRequest: getUserByNameResponse = getUserByNameResponseBadRequest
    def NotFound: getUserByNameResponse = getUserByNameResponseNotFound
  }
  sealed abstract class updateUserResponse(val statusCode: StatusCode)
  case object updateUserResponseBadRequest extends updateUserResponse(StatusCodes.BadRequest)
  case object updateUserResponseNotFound extends updateUserResponse(StatusCodes.NotFound)
  object updateUserResponse {
    implicit val updateUserTRM: ToResponseMarshaller[updateUserResponse] = Marshaller { implicit ec => 
      resp => updateUserTR(resp)
    }
    implicit def updateUserTR(value: updateUserResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r: updateUserResponseBadRequest.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
      case r: updateUserResponseNotFound.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => updateUserResponse): updateUserResponse = ev(value)
    def BadRequest: updateUserResponse = updateUserResponseBadRequest
    def NotFound: updateUserResponse = updateUserResponseNotFound
  }
  sealed abstract class deleteUserResponse(val statusCode: StatusCode)
  case object deleteUserResponseBadRequest extends deleteUserResponse(StatusCodes.BadRequest)
  case object deleteUserResponseNotFound extends deleteUserResponse(StatusCodes.NotFound)
  object deleteUserResponse {
    implicit val deleteUserTRM: ToResponseMarshaller[deleteUserResponse] = Marshaller { implicit ec => 
      resp => deleteUserTR(resp)
    }
    implicit def deleteUserTR(value: deleteUserResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r: deleteUserResponseBadRequest.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
      case r: deleteUserResponseNotFound.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => deleteUserResponse): deleteUserResponse = ev(value)
    def BadRequest: deleteUserResponse = deleteUserResponseBadRequest
    def NotFound: deleteUserResponse = deleteUserResponseNotFound
  }
}