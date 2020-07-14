package pt.tecnico.dsi.openstack.designate.models

import io.circe.{Codec, Encoder}
import io.circe.derivation.{deriveCodec, deriveEncoder, renaming}

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
case class FloatingIP (
  ptrdname: String,
  description: String,
  ttl: Integer,
  address: String,
  status: Status,
  action: Action
)
