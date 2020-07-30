package pt.tecnico.dsi.openstack.designate.models

import io.circe.{Codec, Encoder}
import io.circe.derivation.{deriveCodec, deriveEncoder, renaming}
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}

object FloatingIP {
  implicit val codec: Codec.AsObject[FloatingIP] = deriveCodec(renaming.snakeCase)

  object Create {
    implicit val decoder: Encoder[Create] = deriveEncoder(renaming.snakeCase)
  }
  case class Create (
    ptrdname: String,
    description: String,
    ttl: Integer,
  )
}
case class FloatingIP(
  id: String,
  ptrdname: String,
  description: String,
  ttl: Integer,
  address: String,
  status: Status,
  action: Action,
  links: List[Link] = List.empty,
) extends Identifiable
