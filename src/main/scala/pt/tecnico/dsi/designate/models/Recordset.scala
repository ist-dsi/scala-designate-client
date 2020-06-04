package pt.tecnico.dsi.designate.models

import java.time.OffsetDateTime
import cats.effect.Sync
import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}
import pt.tecnico.dsi.designate.DesignateClient

object Recordset {
  implicit val codec: Codec.AsObject[Recordset] = deriveCodec(renaming.snakeCase)
}

case class Recordset(
  projectId: String,
  name: String,
  ttl: Integer,
  status: Status,
  action: Action,
  zoneId: String,
  zoneName: String,
  description: Option[String],
  `type`: String,
  version: Integer,
  createdAt: OffsetDateTime,
  updatedAt: OffsetDateTime,
  records: Seq[String]
) {
  def zone[F[_]: Sync](implicit d: DesignateClient[F]): F[WithId[Zone]] = d.zones.get(zoneId)
}

