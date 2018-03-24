package petstore.definitions
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import cats.syntax.either._
import _root_.petstore.Implicits._
sealed abstract class PetStatus(val value: String) { override def toString: String = value.toString }
object PetStatus {
  object members {
    case object Available extends PetStatus("available")
    case object Pending extends PetStatus("pending")
    case object Sold extends PetStatus("sold")
  }
  val Available: PetStatus = members.Available
  val Pending: PetStatus = members.Pending
  val Sold: PetStatus = members.Sold
  val values = Vector(Available, Pending, Sold)
  def parse(value: String): Option[PetStatus] = values.find(_.value == value)
  implicit val encodePetStatus: Encoder[PetStatus] = Encoder[String].contramap(_.value)
  implicit val decodePetStatus: Decoder[PetStatus] = Decoder[String].emap(value => parse(value).toRight(s"$value not a member of PetStatus"))
  implicit val addPathPetStatus: AddPath[PetStatus] = AddPath.build(_.value)
  implicit val showPetStatus: Show[PetStatus] = Show.build(_.value)
}