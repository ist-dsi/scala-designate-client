package pt.tecnico.dsi.designate.models

import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

object Nameserver {
  implicit val codec: Codec.AsObject[Nameserver] = deriveCodec(renaming.snakeCase)
}

case class Nameserver (
  hostname: String,
  priority: Integer
)
