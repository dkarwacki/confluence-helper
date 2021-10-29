import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class ChildPages(
    results: List[ChildPage],
    start: Int,
    limit: Int,
    size: Int
)

object ChildPages {
  implicit val decoder: Decoder[ChildPages] = deriveDecoder[ChildPages]
  implicit val encoder: Encoder[ChildPages] = deriveEncoder[ChildPages]
}
