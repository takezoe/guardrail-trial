package petstore.user
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
object UserClient {
  def apply(host: String = "http://petstore.swagger.io")(implicit httpClient: HttpRequest => Future[HttpResponse], ec: ExecutionContext, mat: Materializer): UserClient = new UserClient(host = host)(httpClient = httpClient, ec = ec, mat = mat)
  def httpClient(httpClient: HttpRequest => Future[HttpResponse], host: String = "http://petstore.swagger.io")(implicit ec: ExecutionContext, mat: Materializer): UserClient = new UserClient(host = host)(httpClient = httpClient, ec = ec, mat = mat)
}
class UserClient(host: String = "http://petstore.swagger.io")(implicit httpClient: HttpRequest => Future[HttpResponse], ec: ExecutionContext, mat: Materializer) {
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
  def createUser(body: User, headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], IgnoredEntity] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[IgnoredEntity](Marshal(body).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.POST, uri = host + basePath + "/user", entity = entity, headers = allHeaders))
    })
  }
  def createUsersWithArrayInput(body: IndexedSeq[User], headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], IgnoredEntity] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[IgnoredEntity](Marshal(body).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.POST, uri = host + basePath + "/user/createWithArray", entity = entity, headers = allHeaders))
    })
  }
  def createUsersWithListInput(body: IndexedSeq[User], headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], IgnoredEntity] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[IgnoredEntity](Marshal(body).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.POST, uri = host + basePath + "/user/createWithList", entity = entity, headers = allHeaders))
    })
  }
  def loginUser(username: String, password: String, headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], String] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[String](Marshal(HttpEntity.Empty).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.GET, uri = host + basePath + "/user/login" + "?" + Formatter.addArg("username", username) + Formatter.addArg("password", password), entity = entity, headers = allHeaders))
    })
  }
  def logoutUser(headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], IgnoredEntity] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[IgnoredEntity](Marshal(HttpEntity.Empty).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.GET, uri = host + basePath + "/user/logout", entity = entity, headers = allHeaders))
    })
  }
  def getUserByName(username: String, headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], User] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[User](Marshal(HttpEntity.Empty).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.GET, uri = host + basePath + "/user/" + Formatter.addPath(username), entity = entity, headers = allHeaders))
    })
  }
  def updateUser(username: String, body: User, headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], IgnoredEntity] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[IgnoredEntity](Marshal(body).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.PUT, uri = host + basePath + "/user/" + Formatter.addPath(username), entity = entity, headers = allHeaders))
    })
  }
  def deleteUser(username: String, headers: scala.collection.immutable.Seq[HttpHeader] = Nil): EitherT[Future, Either[Throwable, HttpResponse], IgnoredEntity] = {
    val allHeaders = headers ++ scala.collection.immutable.Seq[Option[HttpHeader]]().flatten
    wrap[IgnoredEntity](Marshal(HttpEntity.Empty).to[RequestEntity].flatMap {
      entity => httpClient(HttpRequest(method = HttpMethods.DELETE, uri = host + basePath + "/user/" + Formatter.addPath(username), entity = entity, headers = allHeaders))
    })
  }
}