package magnolia.bson.derivation

import magnolia.bson.examples._
import reactivemongo.bson.{BSONDocumentHandler, BSONDocumentReader, BSONDocumentWriter}

object BSONDerivationTest {

  def main(args: Array[String]): Unit = {
    import magnolia.bson.derivation.reader.semiauto._
    import magnolia.bson.derivation.writer.semiauto._
    import magnolia.bson.derivation.handler.semiauto._
    import reactivemongo.bson.DefaultBSONHandlers._

    implicit val transportHandler: BSONDocumentHandler[Transport] = deriveMagnoliaHandler[Transport]
    implicit val coordinatesReader = deriveMagnoliaReader[Coordinates]
    implicit val coordinatesWriter = deriveMagnoliaWriter[Coordinates]
    implicit val cityReader = deriveMagnoliaReader[City]
    implicit val cityWriter = deriveMagnoliaWriter[City]

    implicit val tripReader: BSONDocumentReader[Trip] = deriveMagnoliaReader[Trip]
    implicit val tripWriter: BSONDocumentWriter[Trip] = deriveMagnoliaWriter[Trip]

    val cityA = City("Frauenfeld", Coordinates(1, 2))
    val cityB = City("Lisbon", Coordinates(3, 4))
    val trip =
    Trip(
      cities = Seq(cityA, cityB),
      transports = Seq(Flight("Swiss"), Taxi, Flight("Lufthansa"))
    )


    val cs = tripWriter.write(trip)
    val t = tripReader.read(cs)

    println(t == trip)
  }

}
