package pt.tecnico.dsi.designate

import pt.tecnico.dsi.designate.models.Quota

class QuotasSpec extends Utils {
  "The Quotas service" should {
    "get quotas" in {
      for {
        client <- designateClient
        _ <- client.quotas.get
      } yield assert { true }
    }

    "reset quotas in project" in {
      for {
        keystone <- keystoneClient
        // We need a sample project
        project <- keystone.projects.list().head.compile.lastOrError
        client <- designateClient
        isIdempotent <- client.quotas.reset(project.id).valueShouldIdempotentlyBe(())
      } yield isIdempotent
    }

    "get project quotas" in {
      for {
        keystone <- keystoneClient
        project <- keystone.projects.list().head.compile.lastOrError
        client <- designateClient
        actual <- client.quotas.get(project.id)
        isIdempotent <- client.quotas.get(project.id).valueShouldIdempotentlyBe(actual)
      } yield isIdempotent
    }

    val dummyQuota = Quota(
      apiExportSize = 20,
      recordsetRecords = 30,
      zoneRecords = 10,
      zoneRecordsets = 25,
      zones = 15
    )

    "set project quotas" in {
      for {
        keystone <- keystoneClient
        project <- keystone.projects.list().head.compile.lastOrError
        client <- designateClient
        isIdempotent <- client.quotas.update(project.id, dummyQuota).valueShouldIdempotentlyBe(dummyQuota)
      } yield isIdempotent
    }
  }
}
