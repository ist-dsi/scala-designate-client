package pt.tecnico.dsi.openstack.designate

import cats.effect.IO
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.common.services.CrudService

abstract class CrudSpec[M, C, U](val name: String, val service: DesignateClient[IO] => CrudService[IO, M, C, U]) extends Utils {
  def stub: IO[C]

  val withSubCreated: IO[(WithId[M], CrudService[IO, M, C, U])] =
    for {
      client <- client
      crudService = service(client)
      expected <- stub
      createdStub <- crudService.create(expected)
    } yield (createdStub, crudService)

  s"The $name service" should {
    s"create ${name}s" in {
      for {
        client <- client
        expected <- stub
        result <- service(client).create(expected).idempotently(_.model shouldBe expected)
      } yield result
    }

    s"list ${name}s" in withSubCreated.flatMap { case (createdStub, service) =>
      service.list().compile.toList.idempotently(_ should contain (createdStub))
    }

    s"get ${name}s" in withSubCreated.flatMap { case (createdStub, service) =>
      service.get(createdStub.id).idempotently(_ shouldBe createdStub)
    }

    s"delete a $name" in withSubCreated.flatMap { case (createdStub, service) =>
      service.delete(createdStub.id).idempotently(_ shouldBe ())
    }
  }
}
