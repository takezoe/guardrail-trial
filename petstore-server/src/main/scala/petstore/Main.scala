package petstore

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import petstore.definitions.User
import petstore.user.{UserHandler, UserResource}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val routes = UserResource.routes(new UserHandler {override def createUsersWithArrayInput(respond: UserResource.createUsersWithArrayInputResponse.type)(body: IndexedSeq[User]): Future[UserResource.createUsersWithArrayInputResponse] = ???
    override def updateUser(respond: UserResource.updateUserResponse.type)(username: String, body: User): Future[UserResource.updateUserResponse] = ???
    override def createUsersWithListInput(respond: UserResource.createUsersWithListInputResponse.type)(body: IndexedSeq[User]): Future[UserResource.createUsersWithListInputResponse] = ???
    override def loginUser(respond: UserResource.loginUserResponse.type)(username: String, password: String): Future[UserResource.loginUserResponse] = ???
    override def logoutUser(respond: UserResource.logoutUserResponse.type)(): Future[UserResource.logoutUserResponse] = ???
    override def deleteUser(respond: UserResource.deleteUserResponse.type)(username: String): Future[UserResource.deleteUserResponse] = ???
    override def getUserByName(respond: UserResource.getUserByNameResponse.type)(username: String): Future[UserResource.getUserByNameResponse] = {
      Future(respond.OK(User(username = Some("takezoe"))))
    }
    override def createUser(respond: UserResource.createUserResponse.type)(body: User): Future[UserResource.createUserResponse] = {
      Future(respond.OK)
    }
  })

  val serverBindingFuture: Future[ServerBinding] = Http().bindAndHandle(routes, "0.0.0.0", 8081)


  sys.ShutdownHookThread {
    serverBindingFuture.flatMap(_.unbind()).onComplete { done =>
      done.failed.map { _.printStackTrace() }
      system.terminate()
    }
    Await.ready(serverBindingFuture, Duration.Inf)
  }
}