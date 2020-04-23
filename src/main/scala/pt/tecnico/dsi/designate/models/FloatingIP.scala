package pt.tecnico.dsi.designate.models

import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

object FloatingIP {
  implicit val codec: Codec.AsObject[FloatingIP] = deriveCodec[FloatingIP](renaming.snakeCase, false, None)
}

case class FloatingIP (
  ptrdname: String,
  description: String,
  ttl: String,
  address: String,
  status: Option[Status],
  action: Option[Action]
)
