package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import cats.syntax.functor._
import fs2.Stream
import io.circe.Codec
import org.http4s.Status.{Conflict, Successful}
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.{Header, Query, Response, Uri}
import pt.tecnico.dsi.designate.models.WithId

abstract class AsymmetricCRUDService[F[_]: Sync: Client, Model: Codec](baseUri: Uri, val name: String, authToken: Header)
  extends BaseService[F](authToken) {

  type Update
  type Create

  import dsl._

  val pluralName = s"${name}s"
  override val uri: Uri = baseUri / pluralName

  def list(): Stream[F, WithId[Model]] = list[WithId[Model]](pluralName, uri, Query.empty)
  def list(query: Query): Stream[F, WithId[Model]] = list[WithId[Model]](pluralName, uri, query)

  def create(value: Create)(implicit codec: Codec[Create]): F[WithId[Model]] = unwrap(POST(value, uri, authToken))

  protected def createHandleConflict(value: Create)(onConflict: Response[F] => F[WithId[Model]])
                                    (implicit codec: Codec[Create]): F[WithId[Model]] =
    client.fetch(POST(value, uri, authToken)) {
      case Successful(response) => response.as[WithId[Model]]
      case Conflict(response) => onConflict(response)
      case response => F.raiseError(UnexpectedStatus(response.status))
    }

  def get(id: String): F[WithId[Model]] = get[Model](id)

  def update(id: String, value: Update)(implicit d: Codec[Update]): F[WithId[Model]] = genericUpdate[Model, Update](id, value)

  def delete(value: WithId[Model]): F[Unit] = delete(value.id)
  def delete(id: String): F[Unit] = delete(uri / id)
}
