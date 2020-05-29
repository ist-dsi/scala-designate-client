package pt.tecnico.dsi.designate

import cats.effect.IO
import org.scalatest.Assertion
import pt.tecnico.dsi.keystone.models.{Enabler, WithId}
import pt.tecnico.dsi.keystone.services.CRUDService

abstract class CRUDSpec[T]
  (val name: String, val service: DesignateClient[IO] => CRUDService[IO, T])
  extends Utils {

  def stub: IO[T]

  val withSubCreated: IO[(WithId[T], CRUDService[IO, T])] =
    for {
      client <- designateClient
      crudService = service(client)
      expected <- stub
      createdStub <- crudService.create(expected)
    } yield (createdStub, crudService)

  s"The ${name} service" should {
    s"create ${name}s" in {
      val createIO = for {
        client <- designateClient
        expected <- stub
        createdStub <- service(client).create(expected)
      } yield (createdStub, expected)

      def test(t: (WithId[T], T)): Assertion = {
        val (createdStub, expected) = t
        createdStub.model shouldBe expected
      }

      createIO.idempotently(test)
    }

    s"list ${name}s" in {
      withSubCreated.flatMap { case (createdStub, service) =>
        service.list().compile.toList.idempotently(_ should contain (createdStub))
      }
    }

    s"get ${name}s" in {
      withSubCreated.flatMap { case (createdStub, service) =>
        service.get(createdStub.id).valueShouldIdempotentlyBe(createdStub)
      }
    }

    s"delete a ${name}" in {
      withSubCreated.flatMap { case (createdStub, service) =>
        service.delete(createdStub.id).valueShouldIdempotentlyBe(())
      }
    }
  }
}
