package pt.tecnico.dsi.designate.models

import enumeratum.{Circe, Enum, EnumEntry}
import io.circe.{Decoder, Encoder}

sealed trait Status extends EnumEntry
case object Status extends Enum[Status] {
  implicit val circeEncoder: Encoder[Status] = Circe.encoderUppercase(this)
  implicit val circeDecoder: Decoder[Status] = Circe.decoderUppercaseOnly(this)

  case object Complete extends Status
  case object Error   extends Status
  case object Pending extends Status
  case object Active  extends Status

  val values: IndexedSeq[Status] = findValues
}