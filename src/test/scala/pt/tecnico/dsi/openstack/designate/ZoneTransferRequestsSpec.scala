package pt.tecnico.dsi.openstack.designate

import java.util.UUID
import scala.annotation.nowarn
import cats.effect.{IO, Resource}
import cats.syntax.show._
import cats.instances.list._
import cats.syntax.traverse._
import pt.tecnico.dsi.openstack.designate.models.ZoneTransferRequest

class ZoneTransferRequestsSpec extends Utils {
  import designate.zones.transferRequests
  
  val withStubZoneRequest: Resource[IO, ZoneTransferRequest] = withStubZone.flatMap { zone =>
    val create = keystone.projects("admin", keystone.session.user.domainId).flatMap { adminProject =>
      // We need to set targetProjectId otherwise `list` will return an empty list
      transferRequests.create(zone.id, ZoneTransferRequest.Create(targetProjectId = Some(adminProject.id)))
    }
    Resource.make(create)(request => transferRequests.delete(request.id))
  }
  
  "Zone Transfer Requests Service" should {
    "list zones" in withStubZoneRequest.use { request =>
      transferRequests.list().idempotently(_ should contain (request))
    }
    
    "createOrUpdate zone transfer request" in withStubZone.use { zone =>
      for {
        result <- transferRequests.createOrUpdate(zone.id, ZoneTransferRequest.Create()).idempotently { request =>
          request.zoneId shouldBe zone.id
          request.zoneName shouldBe zone.name
          request.key.nonEmpty shouldBe true
        }
        requests <- transferRequests.list()
        _ <- requests.traverse(request => transferRequests.delete(request.id))
      } yield result
    }
    
    "get zone transfer request (existing id)" in withStubZoneRequest.use { request =>
      transferRequests.get(request.id).idempotently(_.value shouldBe request)
    }
    "get zone transfer request (non-existing id)" in {
      transferRequests.get(UUID.randomUUID().toString).idempotently(_ shouldBe None)
    }
    
    "apply zone transfer request (existing id)" in withStubZoneRequest.use { request =>
      transferRequests.apply(request.id).idempotently(_ shouldBe request)
    }
    "apply zone transfer request (non-existing id)" in {
      transferRequests.apply(UUID.randomUUID().toString).attempt.idempotently(_.left.value shouldBe a [NoSuchElementException])
    }
    
    "update zone transfer request" in withStubZoneRequest.use { request =>
      val update = ZoneTransferRequest.Update(Some("a newer and updated nicer description"))
      transferRequests.update(request.id, update).idempotently { updated =>
        val updatedRequest = request.copy(
          description = update.description,
          updatedAt = updated.updatedAt
        )
        updated shouldBe updatedRequest
      }
    }
    
    "delete zone transfer request" in withStubZoneRequest.use { request =>
      transferRequests.delete(request).idempotently(_ shouldBe ())
    }
    
    s"show zone transfer accepts" in withStubZoneRequest.use { request =>
      //This line is a fail fast mechanism, and prevents false positives from the linter
      println(show"$request")
      IO("""show"$request"""" should compile): @nowarn
    }
  }
}
