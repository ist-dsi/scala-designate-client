package pt.tecnico.dsi.openstack.designate.models

import java.time.LocalDateTime
import cats.effect.Sync
import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.designate.DesignateClient

object Recordset {
  implicit val codec: Codec.AsObject[Recordset] = deriveCodec(renaming.snakeCase)

  object Create {
    implicit val codec: Codec.AsObject[Create] = deriveCodec(renaming.snakeCase)
  }
  case class Create(
    name: String,
    ttl: Option[Integer],
    description: Option[String],
    `type`: String,
    records: List[String]
  )

  object Update {
    implicit val codec: Codec.AsObject[Update] = deriveCodec(renaming.snakeCase)
  }
  case class Update(
    ttl: Option[Integer],
    description: Option[String],
    records: List[String]
  )
}
case class Recordset(
  projectId: String,
  name: String,
  ttl: Option[Integer],
  status: Status,
  action: Action,
  zoneId: String,
  zoneName: String,
  description: Option[String],
  version: Integer,
  createdAt: LocalDateTime,
  updatedAt: Option[LocalDateTime],
  `type`: String,
  records: List[String]
) {
  def zone[F[_]: Sync](implicit d: DesignateClient[F]): F[WithId[Zone]] = d.zones.get(zoneId)
}

