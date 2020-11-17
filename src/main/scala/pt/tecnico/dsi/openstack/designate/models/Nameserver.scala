package pt.tecnico.dsi.openstack.designate.models

import cats.{Show, derived}
import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

object Nameserver {
  implicit val codec: Codec.AsObject[Nameserver] = deriveCodec(renaming.snakeCase)
  implicit val show: Show[Nameserver] = derived.semiauto.show
}
case class Nameserver(hostname: String, priority: Int)
