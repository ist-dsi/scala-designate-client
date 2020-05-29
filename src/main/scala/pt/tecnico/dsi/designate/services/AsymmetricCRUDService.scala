package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import cats.syntax.functor._
import fs2.Stream
import io.circe.Codec
import org.http4s.Status.{Conflict, Successful}
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.{Header, Query, Request, Response, Uri}
import pt.tecnico.dsi.designate.models.WithId

abstract class AsymmetricCRUDService[F[_]: Sync: Client, T: Codec](baseUri: Uri, val name: String, authToken: Header)
  extends BaseService[F](authToken) {

  type U
  type C

  import dsl._

  val pluralName = s"${name}s"
  override val uri: Uri = baseUri / pluralName

  /** Takes a request and unwraps its return value. */
  protected def unwrap(request: F[Request[F]]): F[WithId[T]] = unwrap(request, name)
  /** Puts a value inside a wrapper. */
  protected def wrap(value: T): Map[String, T] = wrap(value, name)

  def list(): Stream[F, WithId[T]] = genericList[WithId[T]](pluralName, uri, Query.empty)
  def list(query: Query): Stream[F, WithId[T]] = genericList[WithId[T]](pluralName, uri, query)

  def create(value: C)(implicit codec: Codec[C]): F[WithId[T]] = unwrap(POST(value, uri, authToken))

  protected def createHandleConflict(value: C)(onConflict: Response[F] => F[WithId[T]])(implicit codec: Codec[C]): F[WithId[T]] =
    client.fetch(POST(value, uri, authToken)) {
      case Successful(response) => response.as[Map[String, WithId[T]]].map(_.apply(name))
      case Conflict(response) => onConflict(response)
      case response => F.raiseError(UnexpectedStatus(response.status))
    }

  def get(id: String): F[WithId[T]] = genericGet(name, id)

  def update(id: String, value: U)(implicit codec: Codec[U]): F[WithId[T]] = genericUpdate(name, id, value)

  def delete(value: WithId[T]): F[Unit] = delete(value.id)
  def delete(id: String): F[Unit] = genericDelete(uri / id)
}
