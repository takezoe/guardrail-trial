package petstore.pet
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
object PetClient {
  def apply(host: String = "http://petstore.swagger.io")(implicit httpClient: HttpRequest => Future[HttpResponse], ec: ExecutionContext, mat: Materializer): PetClient = new PetClient(host = host)(httpClient = httpClient, ec = ec, mat = mat)
  def httpClient(httpClient: HttpRequest => Future[HttpResponse], host: String = "http://petstore.swagger.io")(implicit ec: ExecutionContext, mat: Materializer): PetClient = new PetClient(host = host)(httpClient = httpClient, ec = ec, mat = mat)
}
class PetClient(host: String = "http://petstore.swagger.io")(implicit httpClient: HttpRequest => Future[HttpResponse], ec: ExecutionContext, mat: Materializer) {
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
  def updatePet(body: Pet, headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], IgnoredEntity] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[IgnoredEntity](Marshal(body).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.PUT, uri = host + basePath + "/pet", entity = entity, headers = allHeaders))
    })
  }
  def addPet(body: Pet, headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], IgnoredEntity] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[IgnoredEntity](Marshal(body).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.POST, uri = host + basePath + "/pet", entity = entity, headers = allHeaders))
    })
  }
  def findPetsByStatusEnum(status: PetStatus, headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], IndexedSeq[Pet]] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[IndexedSeq[Pet]](Marshal(HttpEntity.Empty).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.GET, uri = host + basePath + "/pet/findByStatus/" + Formatter.addPath(status), entity = entity, headers = allHeaders))
    })
  }
  def findPetsByStatus(status: Iterable[String], headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], IndexedSeq[Pet]] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[IndexedSeq[Pet]](Marshal(HttpEntity.Empty).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.GET, uri = host + basePath + "/pet/findByStatus" + "?" + Formatter.addArg("status", status), entity = entity, headers = allHeaders))
    })
  }
  def findPetsByTags(tags: Iterable[String], headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], IndexedSeq[Pet]] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[IndexedSeq[Pet]](Marshal(HttpEntity.Empty).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.GET, uri = host + basePath + "/pet/findByTags" + "?" + Formatter.addArg("tags", tags), entity = entity, headers = allHeaders))
    })
  }
  def getPetById(petId: Long, headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], Pet] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[Pet](Marshal(HttpEntity.Empty).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.GET, uri = host + basePath + "/pet/" + Formatter.addPath(petId), entity = entity, headers = allHeaders))
    })
  }
  def updatePetWithForm(petId: Long, name: Option[String] = None, status: Option[String] = None, headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], IgnoredEntity] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[IgnoredEntity](Marshal(FormData(List(("name", name.map(Formatter.show(_))), ("status", status.map(Formatter.show(_)))).collect({
      case (n, Some(v)) =>
        (n, v)
    }): _*)).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.POST, uri = host + basePath + "/pet/" + Formatter.addPath(petId), entity = entity, headers = allHeaders))
    })
  }
  def deletePet(petId: Long, includeChildren: Option[Boolean] = None, status: Option[PetStatus] = None, apiKey: Option[String] = None, headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], IgnoredEntity] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]](apiKey.map(v => RawHeader("api_key", Formatter.show(v)))).flatten
    wrap[IgnoredEntity](Marshal(HttpEntity.Empty).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.DELETE, uri = host + basePath + "/pet/" + Formatter.addPath(petId) + "?" + Formatter.addArg("includeChildren", includeChildren) + Formatter.addArg("status", status), entity = entity, headers = allHeaders))
    })
  }
  def uploadFile(petId: Long, additionalMetadata: Option[String] = None, file: Option[String] = None, headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], ApiResponse] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[ApiResponse](Marshal(Multipart.FormData(Source.fromIterator {
      () => List(additionalMetadata.map(v => Multipart.FormData.BodyPart("additionalMetadata", Formatter.show(v))), file.map(v => Multipart.FormData.BodyPart("file", Formatter.show(v)))).flatten.iterator
    })).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.POST, uri = host + basePath + "/pet/" + Formatter.addPath(petId) + "/uploadImage", entity = entity, headers = allHeaders))
    })
  }
}