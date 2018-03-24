package petstore

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import petstore.user.UserClient
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import petstore.AkkaHttpImplicits.HttpClient
import petstore.definitions.User

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  implicit val httpClient: HttpClient = (request: HttpRequest) => Http().singleRequest(request)

  val client = UserClient("http://localhost:8081")

  val result = for {
    _ <- client.createUser(User(username = Some("takezoe")))
    user <- client.getUserByName("takezoe")
  } yield user

  val user = Await.result(result.value, Duration.Inf)
  println(user)
}
