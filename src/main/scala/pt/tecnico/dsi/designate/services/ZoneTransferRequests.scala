package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import fs2.Stream
import org.http4s.client.Client
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.designate.models.{WithId, ZoneTransferRequest}

final class ZoneTransferRequests[F[_]: Sync](baseUri: Uri, authToken: Header)(implicit client: Client[F])
  extends BaseService[F](authToken) {
  import dsl._

  override val uri: Uri = baseUri

  val crud: CRUDService[F, ZoneTransferRequest]
    = new CRUDService[F, ZoneTransferRequest](uri, "transfer_request", authToken) {
  }

  def create(zoneId: String): F[WithId[ZoneTransferRequest]]
    = client.expect[WithId[ZoneTransferRequest]](POST(uri, authToken))

  def delete(id: String): F[Unit] = crud.delete(id)
  def list: Stream[F, WithId[ZoneTransferRequest]] = crud.list()
  def update(id: String, value: ZoneTransferRequest): F[WithId[ZoneTransferRequest]] = crud.update(id, value)
}
