package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import fs2.Stream
import org.http4s.client.Client
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.designate.models.{Nameserver, Recordset, WithId, Zone}

trait ListService[F[_]] { this: BaseService[F] =>
  def list: Stream[F, WithId[_]]
}

object Recordsets {
  def list[F[_]: Sync: Client](baseUri: Uri, authToken: Header): Stream[F, WithId[Recordset]]
    = new BaseService[F](authToken) with ListService[F] {
      override val uri: Uri = baseUri / "recordsets"
      def list: Stream[F, WithId[Recordset]] = genericList[WithId[Recordset]]("recordsets", uri)
    }.list
}

final class Recordsets[F[_]: Sync: Client](baseUri: Uri, authToken: Header)
    extends CRUDService[F, Recordset](baseUri, "recordset", authToken) {

}
