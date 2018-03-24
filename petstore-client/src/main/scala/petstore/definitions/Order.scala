package petstore.definitions
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import cats.syntax.either._
import _root_.petstore.Implicits._
case class Order(id: Option[Long] = None, petId: Option[Long] = None, quantity: Option[Int] = None, shipDate: Option[java.time.OffsetDateTime] = None, status: Option[OrderStatus] = None, complete: Option[Boolean] = Option(false))
object Order {
  implicit val encodeOrder = {
    val readOnlyKeys = Set[String]()
    Encoder.forProduct6("id", "petId", "quantity", "shipDate", "status", "complete") { (o: Order) => (o.id, o.petId, o.quantity, o.shipDate, o.status, o.complete) }.mapJsonObject(_.filterKeys(key => !(readOnlyKeys contains key)))
  }
  implicit val decodeOrder = Decoder.forProduct6("id", "petId", "quantity", "shipDate", "status", "complete")(Order.apply _)
}