package pt.tecnico.dsi.openstack.designate

import java.util.UUID
import cats.effect.{IO, Resource}
import cats.instances.list._
import cats.syntax.traverse._
import org.http4s.client.UnexpectedStatus
import org.scalatest.Assertion
import pt.tecnico.dsi.openstack.designate.models.ZoneTransferRequest

class ZoneTransferRequestsSpec extends Utils {
  import designate.zones.tasks.transferRequests

  val withStubZoneRequest: Resource[IO, ZoneTransferRequest] = withStubZone.flatMap { zone =>
    val create = keystone.projects.get("admin", keystone.session.user.domainId).flatMap { adminProject =>
      // We need to set targetProjectId otherwise `list` will return an empty list
      transferRequests.create(zone.id, ZoneTransferRequest.Create(targetProjectId = Some(adminProject.id)))
    }
    Resource.make(create)(request => transferRequests.delete(request.id))
  }

  // Intellij gets confused and thinks ioAssertion2FutureAssertion conversion its being applied inside of `Resource.use`
  // instead of outside of `use`. We are explicit on the types params for `use` so Intellij doesn't show us an error.

  "Zone Transfer Requests Service" should {
    "list zones" in withStubZoneRequest.use[IO, Assertion] { request =>
      transferRequests.list().compile.toList.idempotently(_ should contain (request))
    }

    "create zone transfer request" in withStubZone.use[IO, Assertion] { zone =>
      for {
        result <- transferRequests.create(zone.id, ZoneTransferRequest.Create()).idempotently { request =>
          request.zoneId shouldBe zone.id
          request.zoneName shouldBe zone.name
          request.key.nonEmpty shouldBe true
        }
        requests <- transferRequests.list().compile.toList
        _ <- requests.traverse(request => transferRequests.delete(request.id))
      } yield result
    }

    "get zone transfer request (existing id)" in withStubZoneRequest.use[IO, Assertion] { request =>
      transferRequests.get(request.id).idempotently(_.value shouldBe request)
    }
    "get zone transfer request (non-existing id)" in {
      transferRequests.get(UUID.randomUUID().toString).idempotently(_ shouldBe None)
    }

    "apply zone transfer request (existing id)" in withStubZoneRequest.use[IO, Assertion] { request =>
      transferRequests.apply(request.id).idempotently(_ shouldBe request)
    }
    "apply zone transfer request (non-existing id)" in {
      transferRequests.apply(UUID.randomUUID().toString).attempt.idempotently(_.left.value shouldBe a [UnexpectedStatus])
    }

    "update zone transfer request" in withStubZoneRequest.use[IO, Assertion] { request =>
      val update = ZoneTransferRequest.Update(Some("a newer and updated nicer description"))
      transferRequests.update(request.id, update).idempotently { updated =>
        val updatedRequest = request.copy(
          description = update.description,
          updatedAt = updated.updatedAt
        )
        updated shouldBe updatedRequest
      }
    }

    "delete zone transfer request" in withStubZoneRequest.use[IO, Assertion] { request =>
      transferRequests.delete(request).idempotently(_ shouldBe ())
    }
  }
}
