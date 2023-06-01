package pt.tecnico.dsi.openstack.designate.services

import cats.effect.Concurrent
import cats.syntax.flatMap.*
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import org.log4s.getLogger
import pt.tecnico.dsi.openstack.common.services.CrudService
import pt.tecnico.dsi.openstack.designate.models.Recordset
import pt.tecnico.dsi.openstack.keystone.models.Session

final class Recordsets[F[_]: Concurrent: Client](baseUri: Uri, session: Session)
  extends CrudService[F, Recordset, Recordset.Create, Recordset.Update](baseUri, "recordset", session.authToken, wrapped = false) {

  def getByName(name: String, extraHeaders: Header.ToRaw*): F[Option[Recordset]] =
    stream(Query.fromPairs("name" -> name), extraHeaders*).compile.last
  
  def applyByName(name: String, extraHeaders: Header.ToRaw*): F[Recordset] =
    getByName(name, extraHeaders*).flatMap:
      case Some(recordset) => F.pure(recordset)
      case None => F.raiseError(new NoSuchElementException(s"""Could not find ${this.name} named "$name"."""))
  
  override def defaultResolveConflict(existing: Recordset, create: Recordset.Create, keepExistingElements: Boolean,
    extraHeaders: Seq[Header.ToRaw]): F[Recordset] =
    val updated = Recordset.Update(
      Option(create.records).filter(_ != existing.records),
      if create.ttl != existing.ttl then create.ttl else None,
      if create.description != existing.description then create.description else None,
    )
    if updated.needsUpdate then update(existing.id, updated, extraHeaders*)
    else Concurrent[F].pure(existing)
  
  override def createOrUpdate(create: Recordset.Create, keepExistingElements: Boolean = true, extraHeaders: Seq[Header.ToRaw] = Seq.empty)
    (resolveConflict: (Recordset, Recordset.Create) => F[Recordset] = defaultResolveConflict(_, _, keepExistingElements, extraHeaders)): F[Recordset] =
    createHandleConflict(create, uri, extraHeaders):
      stream(Query.fromPairs("name" -> create.name, "type" -> create.`type`), extraHeaders*).compile.lastOrError.flatMap { existing =>
        getLogger.info(s"createOrUpdate: found unique $name (id: ${existing.id}) with the correct name and type.")
        resolveConflict(existing, create)
      }
}