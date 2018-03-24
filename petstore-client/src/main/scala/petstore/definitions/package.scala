package petstore
import java.time._
import io.circe.java8.{time => j8time}
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import cats.syntax.either._
package object definitions {
  val decodeLong = implicitly[Decoder[Long]]
  implicit def decodeInstant: Decoder[Instant] = j8time.decodeInstant.or(decodeLong.map(Instant.ofEpochMilli))
  implicit def decodeLocalDate: Decoder[LocalDate] = j8time.decodeLocalDateDefault.or(decodeInstant.map(_.atZone(ZoneOffset.UTC).toLocalDate))
  implicit def decodeOffsetDateTime: Decoder[OffsetDateTime] = j8time.decodeOffsetDateTimeDefault.or(decodeInstant.map(_.atZone(ZoneOffset.UTC).toOffsetDateTime))
  implicit val encodeInstant = j8time.encodeInstant
  implicit val encodeLocalDateDefault = j8time.encodeLocalDateDefault
  implicit val encodeLocalDateTimeDefault = j8time.encodeLocalDateTimeDefault
  implicit val encodeLocalTimeDefault = j8time.encodeLocalTimeDefault
  implicit val encodeOffsetDateTimeDefault = j8time.encodeOffsetDateTimeDefault
  implicit val encodeZonedDateTimeDefault = j8time.encodeZonedDateTimeDefault
}