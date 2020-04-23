package pt.tecnico.dsi.designate.models

import io.circe.{Codec, Decoder, Encoder}
import io.circe.derivation.{deriveCodec, deriveEncoder, renaming}

object Nameserver {
  implicit val codec: Codec.AsObject[Nameserver] = deriveCodec[Nameserver](renaming.snakeCase, false, None)
}

case class Nameserver (
  hostname: String,
  priority: Integer
)
