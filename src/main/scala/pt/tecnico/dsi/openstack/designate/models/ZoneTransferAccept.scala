package pt.tecnico.dsi.openstack.designate.models

import java.time.LocalDateTime
import cats.derived.derived
import cats.derived.ShowPretty
import io.circe.derivation.ConfiguredCodec
import org.typelevel.cats.time.instances.localdatetime.given
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}
import pt.tecnico.dsi.openstack.designate.DesignateClient
import pt.tecnico.dsi.openstack.keystone.KeystoneClient
import pt.tecnico.dsi.openstack.keystone.models.Project

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
) extends Identifiable  derives ConfiguredCodec, ShowPretty:
  def project[F[_]](using keystone: KeystoneClient[F]): F[Project] = keystone.projects(projectId)
  def zone[F[_]](using designate: DesignateClient[F]): F[Zone] = designate.zones(zoneId)
  def zoneTransferRequest[F[_]](using designate: DesignateClient[F]): F[ZoneTransferRequest] = designate.zones.transferRequests(zoneTransferRequestId)
