package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Concurrent
import cats.syntax.flatMap._
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import org.log4s.getLogger
import pt.tecnico.dsi.openstack.common.services.CrudService
import pt.tecnico.dsi.openstack.designate.models.Recordset
import pt.tecnico.dsi.openstack.keystone.models.Session

final class Recordsets[F[_]: Concurrent: Client](baseUri: Uri, session: Session)
  extends CrudService[F, Recordset, Recordset.Create, Recordset.Update](baseUri, "recordset", session.authToken, wrapped = false) {

  def getByName(name: String, extraHeaders: Header*): F[Option[Recordset]] =
    stream(Query.fromPairs("name" -> name), extraHeaders:_*).compile.last
  
  def applyByName(name: String, extraHeaders: Header*): F[Recordset] =
    getByName(name, extraHeaders:_*).flatMap {
      case Some(recordset) => F.pure(recordset)
      case None => F.raiseError(new NoSuchElementException(s"""Could not find ${this.name} named "$name"."""))
    }
  
  override def defaultResolveConflict(existing: Recordset, create: Recordset.Create, keepExistingElements: Boolean, extraHeaders: Seq[Header]): F[Recordset] = {
    val updated = Recordset.Update(
      Option(create.records).filter(_ != existing.records),
      if (create.ttl != existing.ttl) create.ttl else None,
      if (create.description != existing.description) create.description else None,
    )
    if (updated.needsUpdate) update(existing.id, updated, extraHeaders:_*)
    else Concurrent[F].pure(existing)
  }
  
  override def createOrUpdate(create: Recordset.Create, keepExistingElements: Boolean = true, extraHeaders: Seq[Header] = Seq.empty)
    (resolveConflict: (Recordset, Recordset.Create) => F[Recordset] = defaultResolveConflict(_, _, keepExistingElements, extraHeaders)): F[Recordset] =
    createHandleConflict(create, uri, extraHeaders) {
      stream(Query.fromPairs("name" -> create.name, "type" -> create.`type`), extraHeaders:_*).compile.lastOrError.flatMap { existing =>
        getLogger.info(s"createOrUpdate: found unique $name (id: ${existing.id}) with the correct name and type.")
        resolveConflict(existing, create)
      }
    }
}