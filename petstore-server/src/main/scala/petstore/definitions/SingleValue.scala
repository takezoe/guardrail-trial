package petstore.definitions
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import cats.syntax.either._
import _root_.petstore.Implicits._
case class SingleValue(id: Option[Long] = None)
object SingleValue {
  implicit val encodeSingleValue = {
    val readOnlyKeys = Set[String]()
    Encoder.forProduct1("id") { (o: SingleValue) => o.id }.mapJsonObject(_.filterKeys(key => !(readOnlyKeys contains key)))
  }
  implicit val decodeSingleValue = Decoder.forProduct1("id")(SingleValue.apply _)
}