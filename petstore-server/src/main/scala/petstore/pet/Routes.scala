package petstore.pet
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
trait PetHandler {
  def updatePet(respond: PetResource.updatePetResponse.type)(body: Pet): scala.concurrent.Future[PetResource.updatePetResponse]
  def addPet(respond: PetResource.addPetResponse.type)(body: Pet): scala.concurrent.Future[PetResource.addPetResponse]
  def findPetsByStatusEnum(respond: PetResource.findPetsByStatusEnumResponse.type)(status: PetStatus): scala.concurrent.Future[PetResource.findPetsByStatusEnumResponse]
  def findPetsByStatus(respond: PetResource.findPetsByStatusResponse.type)(status: Iterable[String]): scala.concurrent.Future[PetResource.findPetsByStatusResponse]
  def findPetsByTags(respond: PetResource.findPetsByTagsResponse.type)(tags: Iterable[String]): scala.concurrent.Future[PetResource.findPetsByTagsResponse]
  def getPetById(respond: PetResource.getPetByIdResponse.type)(petId: Long): scala.concurrent.Future[PetResource.getPetByIdResponse]
  def updatePetWithForm(respond: PetResource.updatePetWithFormResponse.type)(petId: Long, name: Option[String] = None, status: Option[String] = None): scala.concurrent.Future[PetResource.updatePetWithFormResponse]
  def deletePet(respond: PetResource.deletePetResponse.type)(petId: Long, includeChildren: Option[Boolean] = None, status: Option[PetStatus] = None, apiKey: Option[String] = None): scala.concurrent.Future[PetResource.deletePetResponse]
  def uploadFile(respond: PetResource.uploadFileResponse.type)(petId: Long, additionalMetadata: Option[String] = None, file: Option[String] = None): scala.concurrent.Future[PetResource.uploadFileResponse]
}
object PetResource {
  import cats.syntax.either._
  def discardEntity(implicit mat: akka.stream.Materializer): Directive0 = extractRequest.flatMap { req => 
    req.discardEntityBytes().future
    Directive.Empty
  }
  implicit def jsonFSU[T: io.circe.Decoder]: Unmarshaller[String, T] = Unmarshaller[String, T] { implicit ev => 
    string => io.circe.Json.fromString(string).as[T].left.flatMap(err => io.circe.jawn.parse(string).flatMap(_.as[T])).fold(scala.concurrent.Future.failed _, scala.concurrent.Future.successful _)
  }
  def routes(handler: PetHandler)(implicit mat: akka.stream.Materializer): Route = {
    (put & path("v2" / "pet") & entity(as[Pet])) {
      body => complete(handler.updatePet(updatePetResponse)(body))
    } ~ (post & path("v2" / "pet") & entity(as[Pet])) {
      body => complete(handler.addPet(addPetResponse)(body))
    } ~ (get & path("v2" / "pet" / "findByStatus" / Segment.flatMap(str => io.circe.Json.fromString(str).as[PetStatus].toOption)) & discardEntity) {
      status => complete(handler.findPetsByStatusEnum(findPetsByStatusEnumResponse)(status))
    } ~ (get & path("v2" / "pet" / "findByStatus") & parameter(Symbol("status").as[String].*) & discardEntity) {
      status => complete(handler.findPetsByStatus(findPetsByStatusResponse)(status))
    } ~ (get & path("v2" / "pet" / "findByTags") & parameter(Symbol("tags").as[String].*) & discardEntity) {
      tags => complete(handler.findPetsByTags(findPetsByTagsResponse)(tags))
    } ~ (get & path("v2" / "pet" / LongNumber) & discardEntity) {
      petId => complete(handler.getPetById(getPetByIdResponse)(petId))
    } ~ (post & path("v2" / "pet" / LongNumber) & discardEntity & (formField(Symbol("name").as[String].?) & formField(Symbol("status").as[String].?))) {
      (petId, name, status) => complete(handler.updatePetWithForm(updatePetWithFormResponse)(petId, name, status))
    } ~ (delete & path("v2" / "pet" / LongNumber) & (parameter(Symbol("includeChildren").as[Boolean].?) & parameter(Symbol("status").as[PetStatus].?)) & discardEntity & optionalHeaderValueByName("api_key")) {
      (petId, includeChildren, status, apiKey) => complete(handler.deletePet(deletePetResponse)(petId, includeChildren, status, apiKey))
    } ~ (post & path("v2" / "pet" / LongNumber / "uploadImage") & discardEntity & (formField(Symbol("additionalMetadata").as[String].?) & formField(Symbol("file").as[String].?))) {
      (petId, additionalMetadata, file) => complete(handler.uploadFile(uploadFileResponse)(petId, additionalMetadata, file))
    }
  }
  sealed abstract class updatePetResponse(val statusCode: StatusCode)
  case object updatePetResponseBadRequest extends updatePetResponse(StatusCodes.BadRequest)
  case object updatePetResponseNotFound extends updatePetResponse(StatusCodes.NotFound)
  case object updatePetResponseMethodNotAllowed extends updatePetResponse(StatusCodes.MethodNotAllowed)
  object updatePetResponse {
    implicit val updatePetTRM: ToResponseMarshaller[updatePetResponse] = Marshaller { implicit ec => 
      resp => updatePetTR(resp)
    }
    implicit def updatePetTR(value: updatePetResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r: updatePetResponseBadRequest.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
      case r: updatePetResponseNotFound.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
      case r: updatePetResponseMethodNotAllowed.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => updatePetResponse): updatePetResponse = ev(value)
    def BadRequest: updatePetResponse = updatePetResponseBadRequest
    def NotFound: updatePetResponse = updatePetResponseNotFound
    def MethodNotAllowed: updatePetResponse = updatePetResponseMethodNotAllowed
  }
  sealed abstract class addPetResponse(val statusCode: StatusCode)
  case object addPetResponseCreated extends addPetResponse(StatusCodes.Created)
  case object addPetResponseMethodNotAllowed extends addPetResponse(StatusCodes.MethodNotAllowed)
  object addPetResponse {
    implicit val addPetTRM: ToResponseMarshaller[addPetResponse] = Marshaller { implicit ec => 
      resp => addPetTR(resp)
    }
    implicit def addPetTR(value: addPetResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r: addPetResponseCreated.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
      case r: addPetResponseMethodNotAllowed.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => addPetResponse): addPetResponse = ev(value)
    def Created: addPetResponse = addPetResponseCreated
    def MethodNotAllowed: addPetResponse = addPetResponseMethodNotAllowed
  }
  sealed abstract class findPetsByStatusEnumResponse(val statusCode: StatusCode)
  case class findPetsByStatusEnumResponseOK(value: IndexedSeq[Pet]) extends findPetsByStatusEnumResponse(StatusCodes.OK)
  case object findPetsByStatusEnumResponseBadRequest extends findPetsByStatusEnumResponse(StatusCodes.BadRequest)
  object findPetsByStatusEnumResponse {
    implicit val findPetsByStatusEnumTRM: ToResponseMarshaller[findPetsByStatusEnumResponse] = Marshaller { implicit ec => 
      resp => findPetsByStatusEnumTR(resp)
    }
    implicit def findPetsByStatusEnumTR(value: findPetsByStatusEnumResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r @ findPetsByStatusEnumResponseOK(value) =>
        Marshal(value).to[ResponseEntity].map {
          entity => Marshalling.Opaque {
            () => HttpResponse(r.statusCode, entity = entity)
          } :: Nil
        }
      case r: findPetsByStatusEnumResponseBadRequest.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => findPetsByStatusEnumResponse): findPetsByStatusEnumResponse = ev(value)
    implicit def OKEv(value: IndexedSeq[Pet]): findPetsByStatusEnumResponse = OK(value)
    def OK(value: IndexedSeq[Pet]): findPetsByStatusEnumResponse = findPetsByStatusEnumResponseOK(value)
    def BadRequest: findPetsByStatusEnumResponse = findPetsByStatusEnumResponseBadRequest
  }
  sealed abstract class findPetsByStatusResponse(val statusCode: StatusCode)
  case class findPetsByStatusResponseOK(value: IndexedSeq[Pet]) extends findPetsByStatusResponse(StatusCodes.OK)
  case object findPetsByStatusResponseBadRequest extends findPetsByStatusResponse(StatusCodes.BadRequest)
  case object findPetsByStatusResponseNotFound extends findPetsByStatusResponse(StatusCodes.NotFound)
  object findPetsByStatusResponse {
    implicit val findPetsByStatusTRM: ToResponseMarshaller[findPetsByStatusResponse] = Marshaller { implicit ec => 
      resp => findPetsByStatusTR(resp)
    }
    implicit def findPetsByStatusTR(value: findPetsByStatusResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r @ findPetsByStatusResponseOK(value) =>
        Marshal(value).to[ResponseEntity].map {
          entity => Marshalling.Opaque {
            () => HttpResponse(r.statusCode, entity = entity)
          } :: Nil
        }
      case r: findPetsByStatusResponseBadRequest.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
      case r: findPetsByStatusResponseNotFound.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => findPetsByStatusResponse): findPetsByStatusResponse = ev(value)
    implicit def OKEv(value: IndexedSeq[Pet]): findPetsByStatusResponse = OK(value)
    def OK(value: IndexedSeq[Pet]): findPetsByStatusResponse = findPetsByStatusResponseOK(value)
    def BadRequest: findPetsByStatusResponse = findPetsByStatusResponseBadRequest
    def NotFound: findPetsByStatusResponse = findPetsByStatusResponseNotFound
  }
  sealed abstract class findPetsByTagsResponse(val statusCode: StatusCode)
  case class findPetsByTagsResponseOK(value: IndexedSeq[Pet]) extends findPetsByTagsResponse(StatusCodes.OK)
  case object findPetsByTagsResponseBadRequest extends findPetsByTagsResponse(StatusCodes.BadRequest)
  object findPetsByTagsResponse {
    implicit val findPetsByTagsTRM: ToResponseMarshaller[findPetsByTagsResponse] = Marshaller { implicit ec => 
      resp => findPetsByTagsTR(resp)
    }
    implicit def findPetsByTagsTR(value: findPetsByTagsResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r @ findPetsByTagsResponseOK(value) =>
        Marshal(value).to[ResponseEntity].map {
          entity => Marshalling.Opaque {
            () => HttpResponse(r.statusCode, entity = entity)
          } :: Nil
        }
      case r: findPetsByTagsResponseBadRequest.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => findPetsByTagsResponse): findPetsByTagsResponse = ev(value)
    implicit def OKEv(value: IndexedSeq[Pet]): findPetsByTagsResponse = OK(value)
    def OK(value: IndexedSeq[Pet]): findPetsByTagsResponse = findPetsByTagsResponseOK(value)
    def BadRequest: findPetsByTagsResponse = findPetsByTagsResponseBadRequest
  }
  sealed abstract class getPetByIdResponse(val statusCode: StatusCode)
  case class getPetByIdResponseOK(value: Pet) extends getPetByIdResponse(StatusCodes.OK)
  case object getPetByIdResponseBadRequest extends getPetByIdResponse(StatusCodes.BadRequest)
  case object getPetByIdResponseNotFound extends getPetByIdResponse(StatusCodes.NotFound)
  object getPetByIdResponse {
    implicit val getPetByIdTRM: ToResponseMarshaller[getPetByIdResponse] = Marshaller { implicit ec => 
      resp => getPetByIdTR(resp)
    }
    implicit def getPetByIdTR(value: getPetByIdResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r @ getPetByIdResponseOK(value) =>
        Marshal(value).to[ResponseEntity].map {
          entity => Marshalling.Opaque {
            () => HttpResponse(r.statusCode, entity = entity)
          } :: Nil
        }
      case r: getPetByIdResponseBadRequest.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
      case r: getPetByIdResponseNotFound.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => getPetByIdResponse): getPetByIdResponse = ev(value)
    implicit def OKEv(value: Pet): getPetByIdResponse = OK(value)
    def OK(value: Pet): getPetByIdResponse = getPetByIdResponseOK(value)
    def BadRequest: getPetByIdResponse = getPetByIdResponseBadRequest
    def NotFound: getPetByIdResponse = getPetByIdResponseNotFound
  }
  sealed abstract class updatePetWithFormResponse(val statusCode: StatusCode)
  case object updatePetWithFormResponseMethodNotAllowed extends updatePetWithFormResponse(StatusCodes.MethodNotAllowed)
  object updatePetWithFormResponse {
    implicit val updatePetWithFormTRM: ToResponseMarshaller[updatePetWithFormResponse] = Marshaller { implicit ec => 
      resp => updatePetWithFormTR(resp)
    }
    implicit def updatePetWithFormTR(value: updatePetWithFormResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r: updatePetWithFormResponseMethodNotAllowed.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => updatePetWithFormResponse): updatePetWithFormResponse = ev(value)
    def MethodNotAllowed: updatePetWithFormResponse = updatePetWithFormResponseMethodNotAllowed
  }
  sealed abstract class deletePetResponse(val statusCode: StatusCode)
  case object deletePetResponseOK extends deletePetResponse(StatusCodes.OK)
  case object deletePetResponseBadRequest extends deletePetResponse(StatusCodes.BadRequest)
  case object deletePetResponseNotFound extends deletePetResponse(StatusCodes.NotFound)
  object deletePetResponse {
    implicit val deletePetTRM: ToResponseMarshaller[deletePetResponse] = Marshaller { implicit ec => 
      resp => deletePetTR(resp)
    }
    implicit def deletePetTR(value: deletePetResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r: deletePetResponseOK.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
      case r: deletePetResponseBadRequest.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
      case r: deletePetResponseNotFound.type =>
        scala.concurrent.Future.successful(Marshalling.Opaque {
          () => HttpResponse(r.statusCode)
        } :: Nil)
    }
    def apply[T](value: T)(implicit ev: T => deletePetResponse): deletePetResponse = ev(value)
    def OK: deletePetResponse = deletePetResponseOK
    def BadRequest: deletePetResponse = deletePetResponseBadRequest
    def NotFound: deletePetResponse = deletePetResponseNotFound
  }
  sealed abstract class uploadFileResponse(val statusCode: StatusCode)
  case class uploadFileResponseOK(value: ApiResponse) extends uploadFileResponse(StatusCodes.OK)
  object uploadFileResponse {
    implicit val uploadFileTRM: ToResponseMarshaller[uploadFileResponse] = Marshaller { implicit ec => 
      resp => uploadFileTR(resp)
    }
    implicit def uploadFileTR(value: uploadFileResponse)(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[List[Marshalling[HttpResponse]]] = value match {
      case r @ uploadFileResponseOK(value) =>
        Marshal(value).to[ResponseEntity].map {
          entity => Marshalling.Opaque {
            () => HttpResponse(r.statusCode, entity = entity)
          } :: Nil
        }
    }
    def apply[T](value: T)(implicit ev: T => uploadFileResponse): uploadFileResponse = ev(value)
    implicit def OKEv(value: ApiResponse): uploadFileResponse = OK(value)
    def OK(value: ApiResponse): uploadFileResponse = uploadFileResponseOK(value)
  }
}