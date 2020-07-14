package pt.tecnico.dsi.openstack.designate

import cats.effect.{IO, Resource}
import org.http4s.Header
import org.scalatest.{Assertion, BeforeAndAfterAll}
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.designate.models.{Status, ZoneTransferAccept, ZoneTransferRequest}

class ZoneTransferAcceptsSpec extends Utils with BeforeAndAfterAll {
  import designate.zones.tasks.{transferAccepts, transferRequests}

  val withStubZoneRequest: Resource[IO, (WithId[ZoneTransferRequest], Header)] = for {
    zone <- withStubZone
    project <- withStubProject
    create = transferRequests.create(zone.id, ZoneTransferRequest.Create(targetProjectId = Some(project.id)))
    transferRequestResource <- Resource.make(create)(request => transferRequests.delete(request.id))
  } yield (transferRequestResource, Header("x-auth-sudo-project-id", project.id))

  // It does not make sense to create a Resource for the Accept since Accepts cannot be deleted
  def withStubZoneAccept(test: (WithId[ZoneTransferAccept], Header) => IO[Assertion]): IO[Assertion] =
    withStubZoneRequest.use { case (request, sudoProjectIdHeader) =>
      transferAccepts.create(request.key, request.id, sudoProjectIdHeader)
        .flatMap(test(_, sudoProjectIdHeader))
    }

  "Zone Transfer Accept service" should {
    "create zone transfer accept" in withStubZoneRequest.use[IO, Assertion] { case (request, sudoProjectIdHeader) =>
      transferAccepts.create(request.key, request.id, sudoProjectIdHeader).idempotently { accept =>
        accept.status shouldBe Status.Complete
        accept.zoneId shouldBe request.zoneId
        accept.zoneTransferRequestId shouldBe request.id
      }
    }

    "list zone transfer accepts" in withStubZoneAccept { (accept, sudoProjectIdHeader) =>
      transferAccepts.list(sudoProjectIdHeader).compile.toList.idempotently { list =>
        list should contain(accept.copy(model = accept.model.copy(key = None)))
      }
    }

    "get zone transfer accept" in withStubZoneAccept { (accept, sudoProjectIdHeader) =>
      transferAccepts.get(accept.id, sudoProjectIdHeader).idempotently { accepted =>
        accepted.id shouldBe accept.id
        accepted.status shouldBe Status.Complete
        accepted.model.copy(`key` = None) shouldBe accept.model.copy(`key` = None)
      }
    }
  }
}
