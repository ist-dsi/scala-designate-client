package pt.tecnico.dsi.designate

import pt.tecnico.dsi.designate.models.Quota

class QuotasSpec extends Utils {
  "The Quotas service" should {
    "get quotas" in {
      for {
        client <- designateClient
        _ <- client.quotas.get
      } yield assert(true)
    }

    "reset quotas in project" in {
      for {
        // We need a sample project
        keystone <- keystoneClient
        project <- keystone.projects.list().compile.toList.map(_.head)
        client <- designateClient
        _ <- client.quotas.reset(project.id)
      } yield assert(true)
    }

    "get project quotas" in {
      for {
        keystone <- keystoneClient
        project <- keystone.projects.list().compile.toList.map(_.head)
        client <- designateClient
        _ <- client.quotas.get(project.id)
      } yield assert(true)
    }

    val dummyQuota = Quota(
      apiExportSize = 20,
      recordsetRecords = 30,
      zoneRecords = 10,
      zoneRecordsets = 25,
      zones = 15
    )

    "update project quotas" in {
      for {
        keystone <- keystoneClient
        project <- keystone.projects.list().compile.toList.map(_.head)
        client <- designateClient
        _ <- client.quotas.update(project.id, dummyQuota)
      } yield assert(true)
    }
  }
}
