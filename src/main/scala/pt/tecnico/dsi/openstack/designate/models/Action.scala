package pt.tecnico.dsi.openstack.designate.models

import cats.Show
import enumeratum.EnumEntry.Uppercase
import enumeratum.{CirceEnum, Enum, EnumEntry}

sealed trait Action extends EnumEntry with Uppercase
case object Action extends Enum[Action] with CirceEnum[Action] {
  case object None extends Action
  case object Create extends Action
  case object Delete extends Action
  case object Update extends Action
  
  val values: IndexedSeq[Action] = findValues
  
  implicit val show: Show[Action] = Show.fromToString
}