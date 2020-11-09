package pt.tecnico.dsi.openstack.designate.models

import java.time.LocalDateTime
import cats.effect.Sync
import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}
import pt.tecnico.dsi.openstack.designate.DesignateClient
import pt.tecnico.dsi.openstack.keystone.KeystoneClient
import pt.tecnico.dsi.openstack.keystone.models.Project

object ZoneTransferAccept {
  implicit val codec: Codec.AsObject[ZoneTransferAccept] = deriveCodec(renaming.snakeCase)
}
case class ZoneTransferAccept(
  id: String,
  key: Option[String],
  status: Status,
  projectId: String,
  zoneId: String,
  createdAt: LocalDateTime,
  updatedAt: Option[LocalDateTime],
  zoneTransferRequestId: String,
  links: List[Link] = List.empty,
) extends Identifiable {
  def project[F[_]: Sync](implicit keystone: KeystoneClient[F]): F[Project] = keystone.projects(projectId)
  def zone[F[_]: Sync](implicit designate: DesignateClient[F]): F[Zone] = designate.zones(zoneId)
  def zoneTransferRequest[F[_]: Sync](implicit designate: DesignateClient[F]): F[ZoneTransferRequest] =
    designate.zones.transferRequests(zoneTransferRequestId)
}
