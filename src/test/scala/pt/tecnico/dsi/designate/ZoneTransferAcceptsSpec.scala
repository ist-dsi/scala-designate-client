package pt.tecnico.dsi.designate

class ZoneTransferAcceptsSpec extends Utils {
  "Zone Transfer Accept service" should {
    "list zone transfer accepts" in {
      for {
        designate <- designateClient
        _ <- designate.zones.tasks.transferAccepts.list.compile.toList
      } yield assert(true)
    }
  }
}
