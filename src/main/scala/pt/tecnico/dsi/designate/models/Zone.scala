package pt.tecnico.dsi.designate.models

import java.time.{LocalDateTime, OffsetDateTime}

import enumeratum.{Circe, Enum, EnumEntry}
import io.circe.{Codec, Decoder, Encoder}
import io.circe.derivation.{deriveCodec, renaming}

object Zone {
  sealed trait Type extends EnumEntry
  case object Type extends Enum[Type] {
    implicit val circeEncoder: Encoder[Type] = Circe.encoderUppercase(this)
    implicit val circeDecoder: Decoder[Type] = Circe.decoderUppercaseOnly(this)

    case object Primary   extends Type
    case object Secondary extends Type

    val values: IndexedSeq[Type] = findValues
  }

  implicit val codec: Codec[Zone] = deriveCodec(renaming.snakeCase)
}

object ZoneCreate {
  implicit val codec: Codec[ZoneCreate] = deriveCodec(renaming.snakeCase)
}

object ZoneUpdate {
  implicit val codec: Codec[ZoneUpdate] = deriveCodec(renaming.snakeCase)
}

case class ZoneCreate(
  name: String,
  email: String,
  ttl: Option[Integer] = None,
  description: Option[String] = None,
  masters: Option[Seq[String]] = None,
  `type`: Option[Zone.Type] = None,
  attributes: Option[Map[String, String]] = None
)

case class ZoneUpdate(
 email: Option[String] = None,
 ttl: Option[Integer] = None,
 description: Option[String] = None
)

case class Zone(
  name: String,
  email: String,
  status: Status,
  action: Action,
  // TODO: "Version of this resource", failing test: WithId(model: Zone(version: 57 -> 58))
  //version: Integer,
  createdAt: LocalDateTime,
  serial: Integer,
  poolId: String,
  projectId: String,
  transferredAt: Option[LocalDateTime] = None,
  updatedAt: Option[LocalDateTime] = None,
  ttl: Option[Integer] = None,
  description: Option[String] = None,
  `type`: Option[Zone.Type] = None,
  masters: Option[Seq[String]] = None,
  attributes: Option[Map[String, String]] = None,
)
