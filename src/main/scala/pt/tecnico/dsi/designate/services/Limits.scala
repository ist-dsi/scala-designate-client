package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.impl.Methods
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.designate.models
import pt.tecnico.dsi.designate.models.Limit

object Limits {
  def apply[F[_]: Sync](baseUri: Uri, authToken: Header)(implicit client: Client[F]): F[Limit] = {
    val dsl = new Http4sClientDsl[F] with Methods
    import dsl._
    client.expect[Limit](dsl.GET(baseUri / "limits", authToken))
  }
}

