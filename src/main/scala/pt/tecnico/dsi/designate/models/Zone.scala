package pt.tecnico.dsi.designate.models

import java.time.OffsetDateTime

import enumeratum.{Circe, Enum, EnumEntry}
import io.circe.{Codec, Decoder, Encoder}
import io.circe.derivation.{deriveCodec, deriveEncoder, renaming}
import pt.tecnico.dsi.keystone.models.Interface
import pt.tecnico.dsi.keystone.models.auth.Credential

sealed trait Status extends EnumEntry
case object Status extends Enum[Status] {

  implicit val circeEncoder: Encoder[Status] = Circe.encoderUppercase(this)
  implicit val circeDecoder: Decoder[Status] = Circe.decoderUppercaseOnly(this)

  case object Error   extends Status
  case object Pending extends Status
  case object Active  extends Status

  val values: IndexedSeq[Status] = findValues
}

sealed trait Action extends EnumEntry
case object Action extends Enum[Action] {

  implicit val circeEncoder: Encoder[Action] = Circe.encoderUppercase(this)
  implicit val circeDecoder: Decoder[Action] = Circe.decoderUppercaseOnly(this)

  case object `None`  extends Action
  case object Create  extends Action
  case object Delete  extends Action
  case object Update  extends Action

  val values: IndexedSeq[Action] = findValues
}

sealed trait ZoneType extends EnumEntry
case object ZoneType extends Enum[ZoneType] {

  implicit val circeEncoder: Encoder[ZoneType] = Circe.encoderUppercase(this)
  implicit val circeDecoder: Decoder[ZoneType] = Circe.decoderUppercaseOnly(this)

  case object Primary   extends ZoneType
  case object Secondary extends ZoneType

  val values: IndexedSeq[ZoneType] = findValues
}

object Zone {
  implicit val codec: Codec.AsObject[Zone] = deriveCodec[Zone](renaming.snakeCase, false, None)
}

case class Zone(
  name: String,
  email: String,
  status: Option[Status] = None,
  action: Option[Action] = None,
  transferredAt: Option[OffsetDateTime] = None,
  version: Option[Integer] = None,
  TTL: Option[Integer] = None,
  description: Option[String] = None,
  `type`: Option[ZoneType] = None,
  masters: Option[Seq[String]] = None,
  attributes: Option[Map[String, String]] = None,
  serial: Option[String] = None,
  updatedAt: Option[OffsetDateTime] = None,
  createdAt: Option[OffsetDateTime] = None,
  poolId: Option[String] = None,
  projectId: Option[String] = None
)
