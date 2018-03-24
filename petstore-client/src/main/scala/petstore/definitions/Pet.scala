package petstore.definitions
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import cats.syntax.either._
import _root_.petstore.Implicits._
case class Pet(id: Option[Long] = None, category: Option[Category] = None, name: String, photoUrls: IndexedSeq[String] = IndexedSeq.empty, tags: Option[IndexedSeq[Tag]] = Option(IndexedSeq.empty), status: Option[PetStatus] = None)
object Pet {
  implicit val encodePet = {
    val readOnlyKeys = Set[String]()
    Encoder.forProduct6("id", "category", "name", "photoUrls", "tags", "status") { (o: Pet) => (o.id, o.category, o.name, o.photoUrls, o.tags, o.status) }.mapJsonObject(_.filterKeys(key => !(readOnlyKeys contains key)))
  }
  implicit val decodePet = Decoder.forProduct6("id", "category", "name", "photoUrls", "tags", "status")(Pet.apply _)
}