package petstore.definitions
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import cats.syntax.either._
import _root_.petstore.Implicits._
sealed abstract class OrderStatus(val value: String) { override def toString: String = value.toString }
object OrderStatus {
  object members {
    case object Placed extends OrderStatus("placed")
    case object Approved extends OrderStatus("approved")
    case object Delivered extends OrderStatus("delivered")
  }
  val Placed: OrderStatus = members.Placed
  val Approved: OrderStatus = members.Approved
  val Delivered: OrderStatus = members.Delivered
  val values = Vector(Placed, Approved, Delivered)
  def parse(value: String): Option[OrderStatus] = values.find(_.value == value)
  implicit val encodeOrderStatus: Encoder[OrderStatus] = Encoder[String].contramap(_.value)
  implicit val decodeOrderStatus: Decoder[OrderStatus] = Decoder[String].emap(value => parse(value).toRight(s"$value not a member of OrderStatus"))
  implicit val addPathOrderStatus: AddPath[OrderStatus] = AddPath.build(_.value)
  implicit val showOrderStatus: Show[OrderStatus] = Show.build(_.value)
}