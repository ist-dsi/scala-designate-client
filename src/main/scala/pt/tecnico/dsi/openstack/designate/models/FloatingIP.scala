package pt.tecnico.dsi.openstack.designate.models

import cats.derived
import cats.derived.ShowPretty
import io.circe.{Codec, Encoder}
import io.circe.derivation.{deriveCodec, deriveEncoder, renaming}
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}

object FloatingIP {
  object Create {
    implicit val decoder: Encoder[Create] = deriveEncoder(renaming.snakeCase)
    implicit val show: ShowPretty[Create] = derived.semiauto.showPretty
  }
  case class Create (ptrdname: String, description: String, ttl: Int)
  
  implicit val codec: Codec.AsObject[FloatingIP] = deriveCodec(renaming.snakeCase)
  implicit val show: ShowPretty[FloatingIP] = derived.semiauto.showPretty
}
case class FloatingIP(
  id: String,
  ptrdname: String,
  description: String,
  ttl: Int,
  address: String,
  status: Status,
  action: Action,
  links: List[Link] = List.empty,
) extends Identifiable
