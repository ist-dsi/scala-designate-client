package pt.tecnico.dsi.openstack.designate.models

import java.time.LocalDateTime
import cats.derived.ShowPretty
import cats.effect.Sync
import cats.{Show, derived}
import enumeratum.EnumEntry.Uppercase
import enumeratum.{CirceEnum, Enum, EnumEntry}
import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}
import pt.tecnico.dsi.openstack.keystone.KeystoneClient
import pt.tecnico.dsi.openstack.keystone.models.Project

object Zone {
  sealed trait Type extends EnumEntry with Uppercase
  case object Type extends Enum[Type] with CirceEnum[Type] {
    case object Primary   extends Type
    case object Secondary extends Type
    
    val values: IndexedSeq[Type] = findValues
    
    implicit val show: Show[Type] = Show.fromToString
  }

  object Create {
    implicit val codec: Codec[Create] = deriveCodec(renaming.snakeCase)
    implicit val show: ShowPretty[Create] = derived.semiauto.showPretty
  }
  case class Create(
    name: String,
    email: String,
    ttl: Option[Int] = None,
    description: Option[String] = None,
    masters: List[String] = List.empty,
    `type`: Zone.Type = Zone.Type.Primary,
    attributes: Map[String, String] = Map.empty
  )
  
  object Update {
    implicit val codec: Codec[Update] = deriveCodec(renaming.snakeCase)
    implicit val show: ShowPretty[Update] = derived.semiauto.showPretty
  }
  case class Update(
    email: Option[String] = None,
    ttl: Option[Int] = None,
    description: Option[String] = None
  ) {
    lazy val needsUpdate: Boolean = {
      // We could implement this with the next line, but that implementation is less reliable if the fields of this class change
      //  productIterator.asInstanceOf[Iterator[Option[Any]]].exists(_.isDefined)
      List(email, ttl, description).exists(_.isDefined)
    }
  }
  
  implicit val codec: Codec[Zone] = deriveCodec(renaming.snakeCase)
  implicit val show: ShowPretty[Zone] = derived.semiauto.showPretty
}
case class Zone(
  id: String,
  name: String,
  email: String,
  status: Status,
  action: Action,
  version: Int,
  createdAt: LocalDateTime,
  serial: Int,
  poolId: String,
  projectId: String,
  transferredAt: Option[LocalDateTime] = None,
  updatedAt: Option[LocalDateTime] = None,
  ttl: Option[Int] = None,
  description: Option[String] = None,
  `type`: Zone.Type = Zone.Type.Primary,
  masters: List[String] = List.empty,
  attributes: Map[String, String] = Map.empty,
  links: List[Link] = List.empty,
) extends Identifiable {
  def project[F[_]: Sync](implicit keystone: KeystoneClient[F]): F[Project] = keystone.projects(projectId)
}
