package pt.tecnico.dsi.designate.models

import java.time.OffsetDateTime
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

  implicit val codec: Codec.AsObject[Zone] = deriveCodec[Zone](renaming.snakeCase, false, None)
}

object ZoneCreate {
  implicit val codec: Codec.AsObject[ZoneCreate] = deriveCodec[ZoneCreate](renaming.snakeCase, false, None)
}

object ZoneUpdate {
  implicit val codec: Codec.AsObject[ZoneUpdate] = deriveCodec[ZoneUpdate](renaming.snakeCase, false, None)
}

case class ZoneCreate(
  name: String,
  email: String,
  ttl: Option[Int],
  description: Option[String],
  masters: Option[Seq[String]],
  `type`: Option[Zone.Type],
  attributes: Option[Map[String, String]]
)

case class ZoneUpdate(
 email: Option[String],
 ttl: Option[Int],
 description: Option[String]
)

case class Zone(
  name: String,
  email: String,
  status: Option[Status] = None,
  action: Option[Action] = None,
  transferredAt: Option[OffsetDateTime] = None,
  version: Option[Integer] = None,
  TTL: Option[Integer] = None,
  description: Option[String] = None,
  `type`: Option[Zone.Type] = None,
  masters: Option[Seq[String]] = None,
  attributes: Option[Map[String, String]] = None,
  serial: Option[String] = None,
  updatedAt: Option[OffsetDateTime] = None,
  createdAt: Option[OffsetDateTime] = None,
  poolId: Option[String] = None,
  projectId: Option[String] = None
)
