package pt.tecnico.dsi.openstack.designate

import cats.effect.Sync
import fs2.Stream
import io.circe.Decoder
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{EntityDecoder, Header, Uri, circe}
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.designate.models.{Limit, Recordset}
import pt.tecnico.dsi.openstack.designate.services.{FloatingIPs, Quotas, Recordsets, Zones}

class DesignateClient[F[_]: Sync](baseUri: Uri, authToken: Header)(implicit client: Client[F]) {
  val uri: Uri = baseUri / "v2"

  val zones = new Zones[F](uri, authToken)
  val quotas = new Quotas[F](uri, authToken)
  val floatingIps = new FloatingIPs[F](uri, authToken)

  def limits(implicit decoder: Decoder[Limit]): F[Limit] = {
    val dsl = new Http4sClientDsl[F] {}
    import dsl._
    implicit val jsonDecoder: EntityDecoder[F, Limit] = circe.accumulatingJsonOf
    client.expect(GET(uri / "limits", authToken))
  }

  def recordsets: Stream[F, WithId[Recordset]] = new Recordsets[F](uri, authToken).list()
}
