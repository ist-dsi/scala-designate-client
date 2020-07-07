package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Sync
import fs2.Stream
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.common.services.Service
import pt.tecnico.dsi.openstack.designate.models.ZoneTransferAccept

final class ZoneTransferAccepts[F[_]: Sync](baseUri: Uri, authToken: Header)(implicit client: Client[F])
  extends Service[F](authToken) {

  val uri: Uri = baseUri / "transfer_accepts"

  def create(key: String, zoneTransferRequestId: String, extraHeaders: Header*): F[WithId[ZoneTransferAccept]] =
    super.post(wrappedAt = None, Map("key" -> key, "zone_transfer_request_id" -> zoneTransferRequestId), uri, extraHeaders:_*)

  def list(extraHeaders: Header*): Stream[F, WithId[ZoneTransferAccept]] =
    super.list[WithId[ZoneTransferAccept]]("transfer_accepts", uri, Query.empty, extraHeaders:_*)

  def get(zoneTransferAcceptId: String, extraHeaders: Header*): F[WithId[ZoneTransferAccept]] =
    super.get(wrappedAt = None, uri / zoneTransferAcceptId, extraHeaders:_*)
}
