package pt.tecnico.dsi.openstack.designate

import java.util.UUID
import cats.effect.{IO, Resource}
import org.http4s.Header
import org.scalatest.Assertion
import pt.tecnico.dsi.openstack.designate.models.{Status, ZoneTransferAccept, ZoneTransferRequest}

class ZoneTransferAcceptsSpec extends Utils {
  import designate.zones.{transferAccepts, transferRequests}

  val withStubZoneRequest: Resource[IO, (ZoneTransferRequest, Header)] = for {
    zone <- withStubZone
    project <- withStubProject
    create = transferRequests.create(zone.id, ZoneTransferRequest.Create(targetProjectId = Some(project.id)))
    transferRequestResource <- Resource.make(create)(request => transferRequests.delete(request.id))
  } yield (transferRequestResource, Header("x-auth-sudo-project-id", project.id))

  // It does not make sense to create a Resource for the Accept since Accepts cannot be deleted
  def withStubZoneAccept(test: (ZoneTransferAccept, Header) => IO[Assertion]): IO[Assertion] =
    withStubZoneRequest.use { case (request, sudoProjectIdHeader) =>
      transferAccepts.create(request.key, request.id, sudoProjectIdHeader)
        .flatMap(test(_, sudoProjectIdHeader))
    }

  "Zone Transfer Accept service" should {
    "createWithDeduplication zone transfer accept" in withStubZoneRequest.use[IO, Assertion] { case (request, sudoProjectIdHeader) =>
      transferAccepts.createWithDeduplication(request.key, request.id, sudoProjectIdHeader).idempotently { accept =>
        accept.status shouldBe Status.Complete
        accept.zoneId shouldBe request.zoneId
        accept.zoneTransferRequestId shouldBe request.id
      }
    }

    "list zone transfer accepts" in withStubZoneAccept { (accept, sudoProjectIdHeader) =>
      transferAccepts.list(sudoProjectIdHeader).idempotently { list =>
        list should contain(accept.copy(key = None))
      }
    }

    def compareFetchedAccept(receivedAccept: ZoneTransferAccept, expectedAccept: ZoneTransferAccept): Assertion = {
      receivedAccept.id shouldBe expectedAccept.id
      receivedAccept.status shouldBe Status.Complete
      receivedAccept.copy(`key` = None) shouldBe expectedAccept.copy(`key` = None)
    }

    "get zone transfer accept (existing id)" in withStubZoneAccept { (accept, sudoProjectIdHeader) =>
      transferAccepts.get(accept.id, sudoProjectIdHeader).idempotently(received => compareFetchedAccept(received.value, accept))
    }
    "get zone transfer accept (non-existing-id)" in {
      transferAccepts.get(UUID.randomUUID().toString).idempotently(_ shouldBe None)
    }

    "apply zone transfer accept (existing id)" in withStubZoneAccept { (accept, sudoProjectIdHeader) =>
      transferAccepts.apply(accept.id, sudoProjectIdHeader).idempotently(compareFetchedAccept(_, accept))
    }
    "apply zone transfer accept (non-existing-id)" in {
      transferAccepts.apply(UUID.randomUUID().toString).attempt.idempotently(_.left.value shouldBe a [NoSuchElementException])
    }
  }
}
