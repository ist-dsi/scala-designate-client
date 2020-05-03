package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import fs2.Stream
import org.http4s.client.Client
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.designate.models.{Nameserver, WithId, Zone, ZoneTransferAccept, ZoneTransferRequest}

final class Zones[F[_]: Sync: Client](baseUri: Uri, authToken: Header)
    extends CRUDService[F, Zone](baseUri, "zone", authToken) { self =>

  def listGroups(id: String): Stream[F, Nameserver] =
    genericList[Nameserver]("nameservers", uri / id / "nameservers")

  def recordsets(id: String): Recordsets[F] = new Recordsets[F](uri / id / "recordsets", authToken)

  object tasks {
    val uri: Uri = self.uri / "tasks"
    val transferRequests = new ZoneTransferRequests[F](uri, authToken)
    val transferAccepts = new ZoneTransferAccepts[F](uri, authToken)
  }

}
