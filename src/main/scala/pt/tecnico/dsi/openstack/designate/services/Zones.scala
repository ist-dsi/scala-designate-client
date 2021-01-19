package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Sync
import cats.syntax.flatMap._
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import org.log4s.getLogger
import pt.tecnico.dsi.openstack.common.services.CrudService
import pt.tecnico.dsi.openstack.designate.models._
import pt.tecnico.dsi.openstack.keystone.models.Session

final class Zones[F[_]: Sync: Client](baseUri: Uri, session: Session)
  extends CrudService[F, Zone, Zone.Create, Zone.Update](baseUri, "zone", session.authToken, wrapped = false) {

  def getByName(name: String, extraHeaders: Header*): F[Option[Zone]] =
    stream(Query.fromPairs("name" -> name), extraHeaders:_*).compile.last
  
  def applyByName(name: String, extraHeaders: Header*): F[Zone] =
    getByName(name, extraHeaders:_*).flatMap {
      case Some(zone) => F.pure(zone)
      case None => F.raiseError(new NoSuchElementException(s"""Could not find ${this.name} named "$name"."""))
    }
  
  override def defaultResolveConflict(existing: Zone, create: Zone.Create, keepExistingElements: Boolean, extraHeaders: Seq[Header]): F[Zone] = {
    val updated = Zone.Update(
      Option(create.email).filter(_ != existing.email),
      create.ttl.filter(_ != existing.ttl),
      if (create.description != existing.description) create.description else None,
    )
    if (updated.needsUpdate) update(existing.id, updated, extraHeaders:_*)
    else Sync[F].pure(existing)
  }
  
  override def createOrUpdate(create: Zone.Create, keepExistingElements: Boolean = true, extraHeaders: Seq[Header] = Seq.empty)
    (resolveConflict: (Zone, Zone.Create) => F[Zone] = defaultResolveConflict(_, _, keepExistingElements, extraHeaders)): F[Zone] =
    createHandleConflict(create, uri, extraHeaders) {
      applyByName(create.name, extraHeaders:_*).flatMap { existing =>
        getLogger.info(s"createOrUpdate: found unique $name (id: ${existing.id}) with the correct name.")
        resolveConflict(existing, create)
      }
    }
  
  override def update(id: String, update: Zone.Update, extraHeaders: Header*): F[Zone] =
    super.patch(wrappedAt, update, uri / id, extraHeaders:_*)
  
  /**
   * Show the nameservers for a zone with `id`.
   *
   * @param id ID for the zone
   * @param extraHeaders extra headers to include in the request.
   */
  def nameservers(id: String, extraHeaders: Header*): F[List[Nameserver]] =
    list[Nameserver]("nameservers", uri / id / "nameservers", extraHeaders:_*)

  /** @return the Recordsets service class capable of iteracting with the recordsets of the zone with `id`. */
  def recordsets(id: String): Recordsets[F] = new Recordsets(uri / id, session)

  val transferRequests: ZoneTransferRequests[F] = new ZoneTransferRequests(uri / "tasks", session, uri / _ / "tasks")
  val transferAccepts: ZoneTransferAccepts[F] = new ZoneTransferAccepts(uri / "tasks", session)
}
