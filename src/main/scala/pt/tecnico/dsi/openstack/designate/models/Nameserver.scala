package pt.tecnico.dsi.openstack.designate.models

import cats.Show
import cats.derived.derived
import io.circe.derivation.ConfiguredCodec

case class Nameserver(
  hostname: String,
  priority: Int,
) derives ConfiguredCodec, Show
