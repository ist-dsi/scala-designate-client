package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Sync
import cats.syntax.flatMap._
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.openstack.common.services.CrudService
import pt.tecnico.dsi.openstack.designate.models.Recordset
import pt.tecnico.dsi.openstack.keystone.models.Session

final class Recordsets[F[_]: Sync: Client](baseUri: Uri, session: Session)
  extends CrudService[F, Recordset, Recordset.Create, Recordset.Update](baseUri, "recordset", session.authToken, wrapped = false) {

  def getByName(name: String, extraHeaders: Header*): F[Option[Recordset]] =
    stream(Query.fromPairs("name" -> name), extraHeaders:_*).compile.last
  
  def applyByName(name: String, extraHeaders: Header*): F[Recordset] =
    stream(Query.fromPairs("name" -> name), extraHeaders:_*).compile.lastOrError
  
  override def defaultResolveConflict(existing: Recordset, create: Recordset.Create, keepExistingElements: Boolean, extraHeaders: Seq[Header]): F[Recordset] = {
    val updated = Recordset.Update(
      Option(create.records).filter(_ != existing.records),
      if (create.ttl != existing.ttl) create.ttl else None,
      if (create.description != existing.description) create.description else None,
    )
    if (updated.needsUpdate) update(existing.id, updated, extraHeaders:_*)
    else Sync[F].pure(existing)
  }
  
  override def createOrUpdate(create: Recordset.Create, keepExistingElements: Boolean = true, extraHeaders: Seq[Header] = Seq.empty)
    (resolveConflict: (Recordset, Recordset.Create) => F[Recordset] = defaultResolveConflict(_, _, keepExistingElements, extraHeaders)): F[Recordset] =
    createHandleConflict(create, uri, extraHeaders) {
      applyByName(create.name, extraHeaders:_*).flatMap(resolveConflict(_, create))
    }
}