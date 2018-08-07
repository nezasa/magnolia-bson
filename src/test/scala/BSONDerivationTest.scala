package magnolia.bson

import magnolia.debug
import reactivemongo.bson._

object examples {
  case class Coordinates(lat: Double, long: Double)
  case class City(name: String, location: Coordinates)

  sealed trait Transport
  case class Flight(airline: String) extends Transport
  case object Taxi extends Transport

  case class Trip(cities: Seq[City], transports: Seq[Transport])
  object Trip {
    import magnolia.bson.derivation.reader.semiauto._
    import magnolia.bson.derivation.writer.semiauto._

    implicit val transportReader = deriveMagnoliaReader[Transport]
    implicit val transportWriter = deriveMagnoliaWriter[Transport]
    implicit val coordinatesReader = deriveMagnoliaReader[Coordinates]
    implicit val coordinatesWriter = deriveMagnoliaWriter[Coordinates]
    implicit val cityReader = deriveMagnoliaReader[City]
    implicit val cityWriter = deriveMagnoliaWriter[City]

    implicit val tripReader: BSONReader[BSONValue, Trip] = deriveMagnoliaReader[Trip].asInstanceOf[BSONReader[BSONValue, Trip]]
    implicit val tripWriter: BSONWriter[Trip, BSONValue] = deriveMagnoliaWriter[Trip].asInstanceOf[BSONWriter[Trip,BSONValue]]
  }
}

object BSONDerivationTest {

  def main(args: Array[String]): Unit = {
    import examples._

    val cityA = City("Frauenfeld", Coordinates(1, 2))
    val cityB = City("Lisbon", Coordinates(3, 4))
    val trip =
    Trip(
      cities = Seq(cityA, cityB),
      transports = Seq(Flight("Swiss"), Taxi, Flight("Lufthansa"))
    )


    val cs = Trip.tripWriter.write(trip)
    val t = Trip.tripReader.read(cs)

    println(t == trip)
  }

}


