package petstore.definitions
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import cats.syntax.either._
import _root_.petstore.Implicits._
case class ApiResponse(code: Option[Int] = None, `type`: Option[String] = None, message: Option[String] = None)
object ApiResponse {
  implicit val encodeApiResponse = {
    val readOnlyKeys = Set[String]()
    Encoder.forProduct3("code", "type", "message") { (o: ApiResponse) => (o.code, o.`type`, o.message) }.mapJsonObject(_.filterKeys(key => !(readOnlyKeys contains key)))
  }
  implicit val decodeApiResponse = Decoder.forProduct3("code", "type", "message")(ApiResponse.apply _)
}