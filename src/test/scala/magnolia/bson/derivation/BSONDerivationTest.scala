package magnolia.bson.derivation

import magnolia.bson.examples._
import org.specs2.mutable.Specification
import reactivemongo.bson.{BSON, BSONArray, BSONDocument, BSONDocumentHandler, BSONDocumentReader, BSONDocumentWriter, BSONHandler, BSONReader, BSONValue, BSONWriter}

class BSONDerivationTest extends Specification {

  "Handler derivation" should {
    import magnolia.bson.derivation.handler.semiauto._

    implicit val transportHandler: BSONDocumentHandler[Transport] = deriveMagnoliaHandler[Transport]
    implicit val coordinatesReader = deriveMagnoliaHandler[Coordinates]
    implicit val cityReader = deriveMagnoliaHandler[City]

    implicit val triphandler: BSONDocumentHandler[Trip] = deriveMagnoliaHandler[Trip]
    "roundtrip" in {
      val cityA = City("Frauenfeld", Coordinates(1, Some(2)))
      val cityB = City("Lisbon", Coordinates(3, None))
      val trip =
        Trip(
          cities = Seq(cityA, cityB),
          transports = Seq(Flight("Swiss"), Taxi, Flight("Lufthansa"))
        )


      val cs = triphandler.write(trip)
      val t = triphandler.read(cs)

      t should_== trip
    }
    "throw error" in {
      BSON.readDocument[Coordinates](BSONDocument("lat" -> 1.0, "long" -> 1.0, "a" -> BSONArray.empty)) must_== Coordinates(1.0, Some(1.0), Seq.empty)
      BSON.readDocument[Coordinates](BSONDocument("lat" -> 1.0, "long" -> "xpto", "a" -> BSONArray.empty)) must throwA
    }
  }

}
