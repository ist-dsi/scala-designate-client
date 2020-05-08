package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import fs2.Stream
import org.http4s.client.Client
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.designate.models.{Nameserver, Zone}

final class Zones[F[_]: Sync: Client](baseUri: Uri, authToken: Header)
  extends CRUDService[F, Zone](baseUri, "zone", authToken) { self =>

  def listGroups(id: String): Stream[F, Nameserver] =
    genericList[Nameserver]("nameservers", uri / id / "nameservers")

  def recordsets(id: String): Recordsets[F] = new Recordsets(uri / id / "recordsets", authToken)

  object tasks {
    val uri: Uri = self.uri / "tasks"
    val transferRequests: ZoneTransferRequests[F] = new ZoneTransferRequests(uri, authToken)
    val transferAccepts: ZoneTransferAccepts[F] = new ZoneTransferAccepts(uri, authToken)
  }
}
