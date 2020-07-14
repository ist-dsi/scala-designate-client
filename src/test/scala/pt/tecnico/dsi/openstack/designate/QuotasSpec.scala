package pt.tecnico.dsi.openstack.designate

import cats.effect.IO
import org.http4s.Header
import org.scalatest.Assertion
import pt.tecnico.dsi.openstack.designate.models.Quota

class QuotasSpec extends Utils {
  val allProjectsHeader: Header = Header("x-auth-all-projects", "true")

  // These are the default quotas for the Designate we are testing against
  val defaultQuota = Quota(
    apiExportSize = 1000,
    recordsetRecords = 20,
    zoneRecords = 500,
    zoneRecordsets = 500,
    zones = 10,
  )

  // Intellij gets confused and thinks ioAssertion2FutureAssertion conversion its being applied inside of `Resource.use`
  // instead of outside of `use`. We are explicit on the types params for `use` so Intellij doesn't show us an error.

  "The Quotas service" should {
    "get quotas for the current project" in {
      designate.quotas.get().idempotently(_ shouldBe defaultQuota)
    }

    "get quotas" in withStubProject.use[IO, Assertion] { dummyProject =>
      designate.quotas.get(dummyProject.id, allProjectsHeader).idempotently(_ shouldBe defaultQuota)
    }

    "update quotas" in withStubProject.use[IO, Assertion] { dummyProject =>
      val newQuotas = Quota(
        apiExportSize = 20,
        recordsetRecords = 30,
        zoneRecords = 10,
        zoneRecordsets = 25,
        zones = 15
      )
      designate.quotas.update(dummyProject.id, newQuotas, allProjectsHeader).idempotently( _ shouldBe newQuotas)
    }

    "reset quotas" in withStubProject.use[IO, Assertion] { dummyProject =>
      designate.quotas.reset(dummyProject.id, allProjectsHeader).idempotently(_ shouldBe ())
    }
  }

  "Designate Client" should {
    "show limits" in designate.limits.map { limit =>
      limit.maxPageLimit shouldBe 1000
      limit.maxRecordsetNameLength shouldBe 255
      limit.maxRecordsetRecords shouldBe 20
      limit.maxZoneNameLength shouldBe 255
      limit.maxZoneRecords shouldBe 500
      limit.maxZoneRecordsets shouldBe 500
      limit.maxZones shouldBe 10
      limit.minTtl shouldBe None
    }
  }
}
