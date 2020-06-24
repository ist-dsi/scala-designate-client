package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import fs2.Stream
import io.circe.Codec
import io.circe.syntax._
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.designate.models.{Status, WithId, ZoneTransferRequest, ZoneTransferRequestUpdate}

final class ZoneTransferRequests[F[_]: Sync](baseUri: Uri, authToken: Header)(implicit client: Client[F])
  extends BaseService[F](authToken) {

  // We cannot extend CRUDService directly because `create` does not belong to uri
  def crudService: AsymmetricCRUDService[F, ZoneTransferRequest] =
    new AsymmetricCRUDService[F, ZoneTransferRequest](baseUri, name = "transfer_request", authToken) {
      override type Update = ZoneTransferRequestUpdate
  }

  def get(id: String): F[WithId[ZoneTransferRequest]] = crudService.get(id)

  def delete(id: String): F[Unit] = delete(uri / id)

  def list(query: Query): Stream[F, WithId[ZoneTransferRequest]] = crudService.list(query)
  def list(): Stream[F, WithId[ZoneTransferRequest]] = crudService.list()

  def list(status: Option[Status]): Stream[F, WithId[ZoneTransferRequest]] =
    crudService.list(Query.fromPairs("status" -> status.asJson.toString()))

  // TODO: Couldn't use `crudService.update` because it relies on private type `Update`
  def update(id: String, value: ZoneTransferRequestUpdate)(implicit c: Codec[ZoneTransferRequestUpdate]): F[WithId[ZoneTransferRequest]]
    = super.update(uri / id, value)

  override val uri: Uri = baseUri / "transfer_requests"
}
