package pt.tecnico.dsi.openstack.designate.models

import cats.derived.derived
import cats.derived.ShowPretty
import io.circe.derivation.ConfiguredCodec

case class Limit(
  maxPageLimit: Int,
  maxRecordsetNameLength: Int,
  maxRecordsetRecords: Int,
  maxZoneNameLength: Int,
  maxZoneRecords: Int,
  maxZoneRecordsets: Int,
  maxZones: Int,
  minTtl: Option[Int]
) derives ConfiguredCodec, ShowPretty

