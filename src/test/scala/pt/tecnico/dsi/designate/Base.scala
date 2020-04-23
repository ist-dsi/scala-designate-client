package pt.tecnico.dsi.designate

import cats.effect.IO
import pt.tecnico.dsi.designate.Utils

class Base extends Utils {
  def designate: IO[DesignateClient[IO]] = scopedClient.map(s => {
    implicit val x = s
    new DesignateClient[IO]
  })
}
