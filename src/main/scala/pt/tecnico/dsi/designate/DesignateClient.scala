package pt.tecnico.dsi.designate

import cats.effect.Sync
import fs2.Stream
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.impl.Methods
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.designate.models.{Limit, Recordset, WithId}
import pt.tecnico.dsi.designate.services.{Quotas, Zones, _}

class DesignateClient[F[_]: Sync](baseUri: Uri, authToken: Header)(implicit client: Client[F]) {
  val dsl = new Http4sClientDsl[F] with Methods
  import dsl._

  val uri: Uri = baseUri / "v2"

  val zones = new Zones[F](uri, authToken)
  val quotas = new Quotas[F](uri, authToken)
  val floatingIps = new FloatingIPs[F](uri, authToken)

  def limits: F[Limit] = client.expect(GET(uri / "limits", authToken))
  def recordsets: Stream[F, WithId[Recordset]] = new Recordsets[F](uri, authToken).list()
}
