package pt.tecnico.dsi.designate

import cats.effect.Sync
import org.http4s.Uri
import org.http4s.client.Client
import pt.tecnico.dsi.designate.models.{Limit, Recordset, WithId}
import pt.tecnico.dsi.designate.services.{Limits, Quotas, Recordsets, Zones}
import fs2.Stream
import pt.tecnico.dsi.keystone.KeystoneClient

class DesignateClient[F[_]: Sync](implicit keystoneClient: KeystoneClient[F], client: Client[F]) {
  def test = true

  val uri: Uri = keystoneClient.baseUri / "v2"
  val zones = new Zones[F](uri, keystoneClient.authToken)
  val quotas = new Quotas[F](uri, keystoneClient.authToken)
  def limits: F[Limit] = services.Limits(uri, keystoneClient.authToken)
  def recordsets: Stream[F, WithId[Recordset]] = Recordsets.list(uri, keystoneClient.authToken)
}
