package petstore.definitions
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import cats.syntax.either._
import _root_.petstore.Implicits._
case class User(id: Option[Long] = None, username: Option[String] = None, firstName: Option[String] = None, lastName: Option[String] = None, email: Option[String] = None, password: Option[String] = None, phone: Option[String] = None, userStatus: Option[Int] = None)
object User {
  implicit val encodeUser = {
    val readOnlyKeys = Set[String]()
    Encoder.forProduct8("id", "username", "firstName", "lastName", "email", "password", "phone", "userStatus") { (o: User) => (o.id, o.username, o.firstName, o.lastName, o.email, o.password, o.phone, o.userStatus) }.mapJsonObject(_.filterKeys(key => !(readOnlyKeys contains key)))
  }
  implicit val decodeUser = Decoder.forProduct8("id", "username", "firstName", "lastName", "email", "password", "phone", "userStatus")(User.apply _)
}