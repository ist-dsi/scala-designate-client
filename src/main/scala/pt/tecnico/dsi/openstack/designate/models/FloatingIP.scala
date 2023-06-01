package pt.tecnico.dsi.openstack.designate.models

import cats.derived.derived
import cats.derived.ShowPretty
import io.circe.derivation.{ConfiguredEncoder, ConfiguredCodec}
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}

object FloatingIP:
  case class Create(
    ptrdname: String,
    description: String,
    ttl: Int,
  ) derives ConfiguredEncoder, ShowPretty
case class FloatingIP(
  id: String,
  ptrdname: String,
  description: String,
  ttl: Int,
  address: String,
  status: Status,
  action: Action,
  links: List[Link] = List.empty,
) extends Identifiable derives ConfiguredCodec, ShowPretty
