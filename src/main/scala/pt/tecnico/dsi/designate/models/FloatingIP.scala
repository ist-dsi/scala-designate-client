package pt.tecnico.dsi.designate.models

import java.net.{Inet4Address, InetAddress}

import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

import scala.concurrent.duration.FiniteDuration

object FloatingIP {
  implicit val codec: Codec.AsObject[FloatingIP] = deriveCodec[FloatingIP](renaming.snakeCase, false, None)
}

case class FloatingIP (
  ptrdname: String,
  description: String,
  ttl: Integer,
  address: String,
  status: Option[Status],
  action: Option[Action]
)
