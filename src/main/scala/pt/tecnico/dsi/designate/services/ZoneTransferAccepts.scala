package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import fs2.Stream
import org.http4s.client.Client
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.designate.models.{WithId, ZoneTransferAccept}
import io.circe.syntax._

final class ZoneTransferAccepts[F[_]: Sync](baseUri: Uri, authToken: Header)(implicit client: Client[F])
  extends BaseService[F](authToken) {
  import dsl._

  override val uri: Uri = baseUri / "transfer_accepts"

  def create(key: String, zoneTransferRequestId: String): F[WithId[ZoneTransferAccept]] = {
    val body = Map("key" -> key, "zone_transfer_request_id" -> zoneTransferRequestId)
    client.expect(POST(body.asJson, uri, authToken))
  }

  def list: Stream[F, WithId[ZoneTransferAccept]] = list[WithId[ZoneTransferAccept]]("transfer_accepts", uri)

  def get(zoneTransferAcceptId: String): F[WithId[ZoneTransferAccept]] = client.expect(GET(uri / zoneTransferAcceptId, authToken))
}
