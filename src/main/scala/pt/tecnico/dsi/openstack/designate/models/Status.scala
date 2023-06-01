package pt.tecnico.dsi.openstack.designate.models

import cats.Show
import cats.derived.derived
import io.circe.Codec
import io.circe.derivation.{Configuration, ConfiguredEnumCodec}

object Status:
  given Codec[Status] = ConfiguredEnumCodec.derive(_.toUpperCase)
enum Status derives Show:
  case Complete, Error, Pending, Active
