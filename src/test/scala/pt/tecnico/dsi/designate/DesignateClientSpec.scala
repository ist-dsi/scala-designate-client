package pt.tecnico.dsi.designate

class DesignateClientSpec extends Utils {
  "Designate Client" should {
    "show limits" in {
      for {
        client <- designateClient
        _ <- client.limits
      } yield assert(true)
    }

    "show recordsets" in {
      for {
        client <- designateClient
        _ <- client.recordsets.compile.toList
      } yield assert(true)
    }
  }
}
