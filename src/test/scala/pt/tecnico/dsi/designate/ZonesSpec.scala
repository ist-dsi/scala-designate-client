package pt.tecnico.dsi.designate

class ZonesSpec extends Utils {
  "The Zones service" should {
    "list zones" in {
      for {
        client <- designateClient
        isIdempotent <- client.zones.list().compile.toList.idempotently(_ shouldBe empty)
      } yield isIdempotent
    }
  }
}
