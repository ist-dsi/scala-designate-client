package pt.tecnico.dsi.openstack.designate.models

import cats.Show
import cats.derived.derived
import io.circe.Codec
import io.circe.derivation.ConfiguredEnumCodec

object Action:
  given Codec[Action] = ConfiguredEnumCodec.derive(_.toUpperCase)
enum Action derives Show:
  case None, Create, Delete, Update