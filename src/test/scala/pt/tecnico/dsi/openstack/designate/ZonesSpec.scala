package pt.tecnico.dsi.openstack.designate

import cats.effect.IO
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.designate.models.Zone
import pt.tecnico.dsi.openstack.designate.services.Zones

class ZonesSpec extends Utils {
  val dummyZoneCreate: Zone.Create = Zone.Create("zones.org.", "john.doe@zones.org")

  val withStubZone: IO[(Zones[IO], WithId[Zone])] =
    for {
      designate <- client
      zones = designate.zones
      dummyZone <- zones.create(dummyZoneCreate)
    } yield (zones, dummyZone)

  "The Zones service" should {
    "list zones" in withStubZone.flatMap { case (zones, dummyZone) =>
      zones.list().compile.toList.idempotently(_ should contain (dummyZone))
    }

    "create zones" in {
      for {
        designate <- client
        result <- designate.zones.create(dummyZoneCreate).idempotently { actual =>
          actual.email shouldBe dummyZoneCreate.email
          actual.name shouldBe dummyZoneCreate.name
          actual.description shouldBe dummyZoneCreate.description
        }
      } yield result
    }

    "get zone" in withStubZone.flatMap { case (zones, dummyZone) =>
      zones.get(dummyZone.id).idempotently(_ shouldBe dummyZone)
    }

    "update zone" in withStubZone.flatMap { case (zones, dummyZone) =>
      val dummyZoneUpdate = Zone.Update(
        email = Some("afonso@example.org"),
        ttl = Some(600),
        description = Some("new description")
      )
      zones.update(dummyZone.id, dummyZoneUpdate).idempotently { actual =>
        dummyZoneUpdate.email.forall(_ == actual.model.email) shouldBe true
        dummyZoneUpdate.description shouldBe actual.model.description
        dummyZoneUpdate.ttl shouldBe actual.model.ttl
      }
    }

    "delete zone" in withStubZone.flatMap { case (zones, dummyZone) =>
      zones.delete(dummyZone.id).idempotently(_ shouldBe ())
    }

    "list nameservers" in withStubZone.flatMap { case (zones, dummyZone) =>
      zones.nameservers(dummyZone.id).compile.toList.idempotently { nameservers =>
        nameservers.length should be >= 1
      }
    }
  }
}
