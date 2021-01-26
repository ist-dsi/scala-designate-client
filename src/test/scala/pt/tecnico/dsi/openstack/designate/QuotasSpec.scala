package pt.tecnico.dsi.openstack.designate

import scala.annotation.nowarn
import cats.syntax.show._
import org.http4s.Header
import pt.tecnico.dsi.openstack.designate.models.Quota

class QuotasSpec extends Utils {
  val allProjectsHeader: Header = Header("x-auth-all-projects", "true")

  // These are the default quotas for the Designate we are testing against
  val defaultQuota = Quota(
    zones = 10,
    zoneRecordsets = 500,
    zoneRecords = 500,
    recordsetRecords = 20,
    apiExportSize = 1000,
  )

  "The Quotas service" should {
    s"apply quotas (existing id)" in withStubProject.use { dummyProject =>
      designate.quotas.apply(dummyProject.id, allProjectsHeader).idempotently(_ shouldBe defaultQuota)
    }
    s"apply quotas (non-existing id)" in {
      // This is not a mistake in the test. Designate returns a Quota even if the project does not exist :faceplam:
      designate.quotas.apply("non-existing-id", allProjectsHeader).idempotently(_ shouldBe defaultQuota)
    }
    
    "update quotas" in withStubProject.use { dummyProject =>
      val newQuotas = Quota.Update(
        zones = Some(15),
        zoneRecords = Some(10),
        zoneRecordsets = Some(25),
        recordsetRecords = Some(30),
        apiExportSize = Some(20),
      )
      designate.quotas.update(dummyProject.id, newQuotas, allProjectsHeader).idempotently { quota =>
        quota.zones shouldBe newQuotas.zones.value
        quota.zoneRecords shouldBe newQuotas.zoneRecords.value
        quota.zoneRecordsets shouldBe newQuotas.zoneRecordsets.value
        quota.recordsetRecords shouldBe newQuotas.recordsetRecords.value
        quota.apiExportSize shouldBe newQuotas.apiExportSize.value
      }
    }

    "reset quotas" in withStubProject.use { dummyProject =>
      designate.quotas.reset(dummyProject.id, allProjectsHeader).idempotently(_ shouldBe ())
    }
    
    s"show quotas" in withStubProject.use { dummyProject =>
      designate.quotas(dummyProject.id, allProjectsHeader).map { quotas =>
        //This line is a fail fast mechanism, and prevents false positives from the linter
        println(show"$quotas")
        """show"$quotas"""" should compile: @nowarn
      }
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
