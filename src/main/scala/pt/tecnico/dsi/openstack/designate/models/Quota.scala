package pt.tecnico.dsi.openstack.designate.models

import cats.derived
import cats.derived.ShowPretty
import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

object Quota {
  implicit val codec: Codec.AsObject[Quota] = deriveCodec(renaming.snakeCase)
  implicit val show: ShowPretty[Quota] = derived.semiauto.showPretty
}
case class Quota(
  apiExportSize: Int,
  recordsetRecords: Int,
  zoneRecords: Int,
  zoneRecordsets: Int,
  zones: Int
)
