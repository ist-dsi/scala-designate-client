package pt.tecnico.dsi.openstack.designate

import cats.effect.Concurrent
import fs2.Stream
import io.circe.Decoder
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{EntityDecoder, Uri, circe}
import pt.tecnico.dsi.openstack.designate.models.{Limit, Recordset}
import pt.tecnico.dsi.openstack.designate.services.{FloatingIPs, Quotas, Recordsets, Zones}
import pt.tecnico.dsi.openstack.keystone.models.{ClientBuilder, Session}

object DesignateClient extends ClientBuilder:
  final type OpenstackClient[F[_]] = DesignateClient[F]
  final val `type`: String = "dns"
  
  override def apply[F[_]: Concurrent: Client](baseUri: Uri, session: Session): DesignateClient[F] =
    new DesignateClient[F](baseUri, session)
class DesignateClient[F[_]: Concurrent](baseUri: Uri, session: Session)(using client: Client[F]):
  val uri: Uri = if baseUri.path.dropEndsWithSlash.toString.endsWith("v2") then baseUri else baseUri / "v2"

  val zones = new Zones[F](uri, session)
  val quotas = new Quotas[F](uri, session)
  val floatingIps = new FloatingIPs[F](uri, session)

  def limits(using decoder: Decoder[Limit]): F[Limit] =
    val dsl = new Http4sClientDsl[F] {}
    import dsl.*
    given EntityDecoder[F, Limit] = circe.accumulatingJsonOf
    client.expect(GET(uri / "limits", session.authToken))

  def recordsets: Stream[F, Recordset] = new Recordsets[F](uri, session).stream()
