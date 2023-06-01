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

object Recordset:
  case class Create(
    name: String,
    `type`: String,
    records: List[String],
    ttl: Option[Int] = None,
    description: Option[String] = None,
  ) derives ConfiguredCodec, ShowPretty
  
  case class Update(
    records: Option[List[String]] = None,
    ttl: Option[Int] = None,
    description: Option[String] = None,
  ) derives ConfiguredCodec, ShowPretty:
    lazy val needsUpdate: Boolean =
      // We could implement this with the next line, but that implementation is less reliable if the fields of this class change
      //  productIterator.asInstanceOf[Iterator[Option[Any]]].exists(_.isDefined)
      List(records, ttl, description).exists(_.isDefined)
case class Recordset(
  id: String,
  name: String,
  `type`: String,
  records: List[String],
  projectId: String,
  ttl: Option[Int],
  status: Status,
  action: Action,
  zoneId: String,
  zoneName: String,
  description: Option[String],
  version: Int,
  createdAt: LocalDateTime,
  updatedAt: Option[LocalDateTime],
  links: List[Link] = List.empty,
) extends Identifiable derives ConfiguredCodec, ShowPretty:
  def project[F[_]](using keystone: KeystoneClient[F]): F[Project] = keystone.projects(projectId)
  def zone[F[_]](using designate: DesignateClient[F]): F[Zone] = designate.zones(zoneId)

