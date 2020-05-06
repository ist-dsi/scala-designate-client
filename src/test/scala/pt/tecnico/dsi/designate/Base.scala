package pt.tecnico.dsi.designate

import cats.effect.IO
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.designate.Utils

class Base extends Utils {
  def designate: IO[DesignateClient[IO]] = scopedClient.map(s => {
    new DesignateClient[IO](Uri.unsafeFromString("http://localhost:8081/"), s.authToken)
  })
}
