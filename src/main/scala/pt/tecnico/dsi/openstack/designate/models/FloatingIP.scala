package pt.tecnico.dsi.openstack.designate.models

import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

object FloatingIP {
  implicit val codec: Codec.AsObject[FloatingIP] = deriveCodec(renaming.snakeCase)
}
case class FloatingIP (
  ptrdname: String,
  description: String,
  ttl: Integer,
  address: String,
  status: Option[Status],
  action: Option[Action]
)
