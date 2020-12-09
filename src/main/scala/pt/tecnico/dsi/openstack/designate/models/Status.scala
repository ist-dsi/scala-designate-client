package pt.tecnico.dsi.openstack.designate.models

import cats.Show
import enumeratum.EnumEntry.Uppercase
import enumeratum.{CirceEnum, Enum, EnumEntry}

sealed trait Status extends EnumEntry with Uppercase
case object Status extends Enum[Status] with CirceEnum[Status] {
  case object Complete extends Status
  case object Error extends Status
  case object Pending extends Status
  case object Active extends Status

  val values: IndexedSeq[Status] = findValues
  
  implicit val show: Show[Status] = Show.fromToString
}