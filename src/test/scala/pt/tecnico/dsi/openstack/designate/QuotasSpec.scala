package pt.tecnico.dsi.openstack.designate

import cats.effect.IO
import org.http4s.Header
import pt.tecnico.dsi.openstack.designate.models.Quota
import pt.tecnico.dsi.openstack.keystone.models.Project

class QuotasSpec extends Utils {
  val allProjectsHeader: Header = Header("x-auth-all-projects", "true")
  val withStubProject: IO[(DesignateClient[IO], String)] =
    for {
      keystone <- keystoneClient
      dummyProject <- keystone.projects.create(Project("dummy", "dummy project", "default"))
      designate <- client
    } yield (designate, dummyProject.id)

  "The Quotas service" should {
    "get quotas for the current project" in withStubProject.flatMap { case (designate, _) =>
      designate.quotas.get().idempotently { quotas =>
        quotas.apiExportSize shouldBe 20
        quotas.recordsetRecords shouldBe 30
        quotas.zoneRecords shouldBe 10
        quotas.zoneRecordsets shouldBe 25
        quotas.zones shouldBe 15
      }
    }

    "get quotas" in withStubProject.flatMap { case (designate, dummyProjectId) =>
      designate.quotas.get(dummyProjectId, allProjectsHeader).idempotently { quotas =>
        quotas.apiExportSize shouldBe 1000
        quotas.recordsetRecords shouldBe 20
        quotas.zoneRecords shouldBe 500
        quotas.zoneRecordsets shouldBe 500
        quotas.zones shouldBe 10
      }
    }

    "update quotas" in withStubProject.flatMap { case (designate, dummyProjectId) =>
      val newQuotas = Quota(
        apiExportSize = 20,
        recordsetRecords = 30,
        zoneRecords = 10,
        zoneRecordsets = 25,
        zones = 15
      )
      designate.quotas.update(dummyProjectId, newQuotas, allProjectsHeader).idempotently( _ shouldBe newQuotas)
    }

    "reset quotas" in withStubProject.flatMap { case (designate, dummyProjectId) =>
      designate.quotas.reset(dummyProjectId, allProjectsHeader).idempotently(_ shouldBe ())
    }
  }

  "Designate Client" should {
    "show limits" in client.flatMap(_.limits).map { limit =>
      limit.maxZones shouldBe 15
      limit.maxZoneRecords shouldBe 10
      limit.maxZoneNameLength shouldBe 255
      limit.maxRecordsetRecords shouldBe 30
      limit.maxRecordsetNameLength shouldBe 255
      limit.maxZoneRecords shouldBe 10
      limit.maxPageLimit shouldBe 1000
      limit.minTtl shouldBe None
    }
  }
}
