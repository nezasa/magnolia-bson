package magnolia.bson

object examples {
  case class Coordinates(lat: Double, long: Double, a: Seq[Int] = Seq.empty)
  case class City(name: String, location: Coordinates)

  sealed trait Transport
  case class Flight(airline: String) extends Transport
  case object Taxi extends Transport

  case class Trip(cities: Seq[City], transports: Seq[Transport])

}
