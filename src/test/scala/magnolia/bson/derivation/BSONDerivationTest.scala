package magnolia.bson.derivation

import magnolia.bson.examples._
import org.specs2.mutable.Specification
import reactivemongo.bson.{BSONArray, BSONDocumentHandler, BSONDocumentReader, BSONDocumentWriter, BSONHandler, BSONReader, BSONValue, BSONWriter}

class BSONDerivationTest extends Specification {

  "Handler derivation" should {
    "roudntrip" in {
      import magnolia.bson.derivation.handler.semiauto._

      implicit val transportHandler: BSONDocumentHandler[Transport] = deriveMagnoliaHandler[Transport]
      implicit val coordinatesReader = deriveMagnoliaHandler[Coordinates]
      implicit val cityReader = deriveMagnoliaHandler[City]

      implicit val triphandler: BSONDocumentHandler[Trip] = deriveMagnoliaHandler[Trip]

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
  }

}
