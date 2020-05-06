package pt.tecnico.dsi.designate

import pt.tecnico.dsi.designate.models.{Recordset, WithId}
import pt.tecnico.dsi.designate.services.{Quotas, Recordsets, Zones}
import fs2.Stream
import cats.effect.Sync
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.impl.Methods
import org.http4s.{Header, Uri}
import services._
import pt.tecnico.dsi.designate.models.Limit

trait ListService[F[_]] { this: BaseService[F] =>
  def list: Stream[F, WithId[_]]
}
class DesignateClient[F[_]: Sync](baseUri: Uri, authToken: Header)(implicit client: Client[F]) {
  def test = true

  val dsl = new Http4sClientDsl[F] with Methods
  import dsl._

  val uri: Uri = baseUri / "v2"
  val zones = new Zones[F](uri, authToken)
  val quotas = new Quotas[F](uri, authToken)
  def limits: F[Limit] = client.expect[Limit](GET(baseUri / "limits", authToken))
  def recordsets: Stream[F, WithId[Recordset]] = new BaseService[F](authToken) with ListService[F] {
    override val uri: Uri = baseUri / "recordsets"
    def list: Stream[F, WithId[Recordset]] = genericList[WithId[Recordset]]("recordsets", uri)
  }.list

}
