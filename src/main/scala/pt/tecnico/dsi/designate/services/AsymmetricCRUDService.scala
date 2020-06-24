package pt.tecnico.dsi.designate.services

import cats.effect.Sync
import cats.syntax.functor._
import fs2.Stream
import io.circe.{Codec, Encoder}
import org.http4s.Status.{Conflict, Successful}
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.{EntityDecoder, EntityEncoder, Header, Query, Response, Uri}
import pt.tecnico.dsi.designate.models.WithId

abstract class AsymmetricCRUDService[F[_]: Sync: Client, Model: Codec](baseUri: Uri, val name: String, authToken: Header)
  extends BaseService[F](authToken) {

  type Update
  type Create

  import dsl._

  val pluralName = s"${name}s"
  override val uri: Uri = baseUri / pluralName

  def list(query: Query = Query.empty): Stream[F, WithId[Model]] = super.list[WithId[Model]](pluralName, uri, query)

  def create(value: Create)(implicit encoder: Encoder[Create]): F[WithId[Model]] =
    super.create(uri, value, wrappedAt = Some(name))

  protected def createHandleConflict(value: Create)(onConflict: Response[F] => F[WithId[Model]])
    (implicit encoder: Encoder[Create]): F[WithId[Model]] = {
    implicit val d: EntityDecoder[F, WithId[Model]] = unwrapped(Some(name))
    implicit val e: EntityEncoder[F, Create] = wrapped(Some(name))
    client.fetch(POST(value, uri, authToken)) {
      case Successful(response) => response.as[WithId[Model]]
      case Conflict(response) => onConflict(response)
      case response => F.raiseError(UnexpectedStatus(response.status))
    }
  }

  def get(id: String): F[WithId[Model]] = super.get(uri / id)
  def update(id: String, value: Update)(implicit d: Encoder[Update]): F[WithId[Model]] = super.update(uri / id, value)

  def delete(value: WithId[Model]): F[Unit] = delete(value.id)
  def delete(id: String): F[Unit] = super.delete(uri / id)
}
