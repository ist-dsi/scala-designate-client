package pt.tecnico.dsi.openstack.designate.models

import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}

object Quota {
  implicit val codec: Codec.AsObject[Quota] = deriveCodec(renaming.snakeCase)
}
case class Quota(
  apiExportSize: Int,
  recordsetRecords: Int,
  zoneRecords: Int,
  zoneRecordsets: Int,
  zones: Int
)
