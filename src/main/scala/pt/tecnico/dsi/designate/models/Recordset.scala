package pt.tecnico.dsi.designate.models

import java.time.{LocalDateTime, OffsetDateTime}

import cats.effect.Sync
import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}
import pt.tecnico.dsi.designate.DesignateClient

object Recordset {
  implicit val codec: Codec.AsObject[Recordset] = deriveCodec(renaming.snakeCase)
}

object RecordsetCreate {
  implicit val codec: Codec.AsObject[RecordsetCreate] = deriveCodec(renaming.snakeCase)
}

object RecordsetUpdate {
  implicit val codec: Codec.AsObject[RecordsetUpdate] = deriveCodec(renaming.snakeCase)
}

case class RecordsetUpdate(
  ttl: Option[Integer],
  description: Option[String],
  records: Seq[String]
)

case class RecordsetCreate(
  name: String,
  ttl: Option[Integer],
  description: Option[String],
  `type`: String,
  records: Seq[String]
)

case class Recordset(
  projectId: String,
  name: String,
  ttl: Option[Integer],
  status: Status,
  action: Action,
  zoneId: String,
  zoneName: String,
  description: Option[String],
  `type`: String,
  version: Integer,
  createdAt: LocalDateTime,
  updatedAt: Option[LocalDateTime],
  records: Seq[String]
) {
  def zone[F[_]: Sync](implicit d: DesignateClient[F]): F[WithId[Zone]] = d.zones.get(zoneId)
}

