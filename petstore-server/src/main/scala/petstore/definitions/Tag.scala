package petstore.definitions
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import cats.syntax.either._
import _root_.petstore.Implicits._
case class Tag(id: Option[Long] = None, name: Option[String] = None)
object Tag {
  implicit val encodeTag = {
    val readOnlyKeys = Set[String]()
    Encoder.forProduct2("id", "name") { (o: Tag) => (o.id, o.name) }.mapJsonObject(_.filterKeys(key => !(readOnlyKeys contains key)))
  }
  implicit val decodeTag = Decoder.forProduct2("id", "name")(Tag.apply _)
}