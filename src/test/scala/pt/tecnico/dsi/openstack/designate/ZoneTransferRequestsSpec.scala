package pt.tecnico.dsi.openstack.designate

import cats.effect.IO
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.designate.models.{Zone, ZoneTransferRequest}
import pt.tecnico.dsi.openstack.designate.services.ZoneTransferRequests

class ZoneTransferRequestsSpec extends Utils {
  val dummyZoneCreate: Zone.Create = Zone.Create("requests.org.", "john.doe@requests.org")
  val withStubZoneRequest: IO[(ZoneTransferRequests[IO], WithId[ZoneTransferRequest])] =
    for {
      keystone <- keystoneClient
      adminProject <- keystone.projects.get("admin", keystone.session.user.domainId)
      designate <- client
      transferRequests = designate.zones.tasks.transferRequests
      dummyZone <- designate.zones.create(dummyZoneCreate)
      // We need to set targetProjectId otherwise `list` will return an empty list
      requestCreate = ZoneTransferRequest.Create(targetProjectId = Some(adminProject.id))
      dummyTransferRequest <- transferRequests.create(dummyZone.id, requestCreate)
    } yield (transferRequests, dummyTransferRequest)

  "Zone Tranfer Requests Service" should {
    "list zones" in withStubZoneRequest.flatMap { case (transferRequests, request) =>
      transferRequests.list().compile.toList.idempotently(_ should contain (request))
    }

    "create zone transfer request" in {
      for {
        designate <- client
        zones = designate.zones
        dummyZone <- zones.create(dummyZoneCreate)
        result <- zones.tasks.transferRequests.create(dummyZone.id, ZoneTransferRequest.Create()).idempotently { request =>
          request.zoneId shouldBe dummyZone.id
          request.zoneName shouldBe dummyZone.name
          request.key.nonEmpty shouldBe true
        }
      } yield result
    }

    "get zone transfer request" in withStubZoneRequest.flatMap { case (transferRequests, request) =>
      transferRequests.get(request.id).idempotently(_ shouldBe request)
    }

    "update zone transfer request" in withStubZoneRequest.flatMap { case (transferRequests, request) =>
      val update = ZoneTransferRequest.Update(Some("a newer and updated nicer description"))
      val updatedRequest = request.copy(model = request.model.copy(description = update.description))
      transferRequests.update(request.id, update).idempotently(_ shouldBe updatedRequest)
    }

    "delete zone transfer request" in withStubZoneRequest.flatMap { case (transferRequests, request) =>
      transferRequests.delete(request.id).idempotently(_ shouldBe ())
    }
  }
}
