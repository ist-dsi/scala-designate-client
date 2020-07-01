package pt.tecnico.dsi.openstack.designate.models

import enumeratum.{Circe, Enum, EnumEntry}
import io.circe.{Decoder, Encoder}

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