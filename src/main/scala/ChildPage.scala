import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class ChildPage(id: Long, title: String)

object ChildPage {
  implicit val decoder: Decoder[ChildPage] =
    deriveDecoder[ChildPage]
  implicit val encoder: Encoder[ChildPage] =
    deriveEncoder[ChildPage]
}
