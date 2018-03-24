package petstore.definitions
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import cats.syntax.either._
import _root_.petstore.Implicits._
case class Category(id: Option[Long] = None, name: Option[String] = None)
object Category {
  implicit val encodeCategory = {
    val readOnlyKeys = Set[String]()
    Encoder.forProduct2("id", "name") { (o: Category) => (o.id, o.name) }.mapJsonObject(_.filterKeys(key => !(readOnlyKeys contains key)))
  }
  implicit val decodeCategory = Decoder.forProduct2("id", "name")(Category.apply _)
}