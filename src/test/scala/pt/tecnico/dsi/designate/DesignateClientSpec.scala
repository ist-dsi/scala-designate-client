package pt.tecnico.dsi.designate

import cats.effect.IO
import org.scalatest.Assertions
import org.scalatest.wordspec.AsyncWordSpec

class DesignateClientSpec extends Base {
  "Designate client" should {
    "Simply work" in {
      for {
        d <- designate
      } yield assert(d.test)
    }
  }
}
