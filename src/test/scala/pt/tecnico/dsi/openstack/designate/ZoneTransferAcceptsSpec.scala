package pt.tecnico.dsi.openstack.designate

import scala.util.Random
import cats.effect.IO
import org.http4s.Header
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.designate.models.{Status, Zone, ZoneTransferAccept, ZoneTransferRequest}
import pt.tecnico.dsi.openstack.designate.services.ZoneTransferAccepts
import pt.tecnico.dsi.openstack.keystone.models.Project

class ZoneTransferAcceptsSpec extends Utils {
  val withStubZonesAndRequest: IO[(ZoneTransferAccepts[IO], WithId[ZoneTransferAccept], Header)] =
    for {
      keystone <- keystoneClient
      dummyProject <- keystone.projects.create(Project("dummy", "dummy project", "default"))
      designate <- client
      dummyZone <- designate.zones.create(Zone.Create(s"zone${Random.alphanumeric.take(10).mkString.toLowerCase}.org.", "joe@example.org"))
      requestCreate = ZoneTransferRequest.Create(targetProjectId = Some(dummyProject.id))
      request <- designate.zones.tasks.transferRequests.create(dummyZone.id, requestCreate)
      transferAccepts = designate.zones.tasks.transferAccepts
      sudoProjectIdHeader = Header("x-auth-sudo-project-id", dummyProject.id)
      accept <- transferAccepts.create(request.key, request.id, sudoProjectIdHeader)
    } yield (transferAccepts, accept, sudoProjectIdHeader)

  "Zone Transfer Accept service" should {
    "create zone transfer accept" in {
      for {
        keystone <- keystoneClient
        dummyProject <- keystone.projects.create(Project("dummy", "dummy project", "default"))
        designate <- client
        dummyZone <- designate.zones.create(Zone.Create(s"zone${Random.nextInt()}.org.", "joe@example.org"))
        requestCreate = ZoneTransferRequest.Create(targetProjectId = Some(dummyProject.id))
        request <- designate.zones.tasks.transferRequests.create(dummyZone.id, requestCreate)
        transferAccepts = designate.zones.tasks.transferAccepts
        result <- transferAccepts.create(request.key, request.id, Header("x-auth-sudo-project-id", dummyProject.id)).idempotently { accept =>
          accept.status shouldBe Status.Complete
          accept.zoneId shouldBe dummyZone.id
          accept.zoneTransferRequestId shouldBe request.id
        }
      } yield result
    }

    "list zone transfer accepts" in withStubZonesAndRequest.flatMap { case (transferAccepts, accept, sudoProjectIdHeader) =>
      transferAccepts.list(sudoProjectIdHeader).compile.toList.idempotently { list =>
        list should contain(accept.copy(model = accept.model.copy(key = None)))
      }
    }

    "get zone transfer accept" in withStubZonesAndRequest.flatMap { case (transferAccepts, accept, sudoProjectIdHeader) =>
      transferAccepts.get(accept.id, sudoProjectIdHeader).idempotently { accepted =>
        accepted.id shouldBe accept.id
        accepted.status shouldBe Status.Complete
        accepted.model.copy(`key` = None) shouldBe accept.model.copy(`key` = None)
      }
    }
  }
}
