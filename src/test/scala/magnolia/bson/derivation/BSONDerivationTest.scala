package magnolia.bson.derivation

import magnolia.bson.examples._
import reactivemongo.bson.{BSONArray, BSONDocumentHandler, BSONDocumentReader, BSONDocumentWriter, BSONHandler, BSONReader, BSONValue, BSONWriter}

object BSONDerivationTest {

  def main(args: Array[String]): Unit = {
    import magnolia.bson.derivation.reader.semiauto._
    import magnolia.bson.derivation.writer.semiauto._
    import magnolia.bson.derivation.handler.semiauto._
//    import reactivemongo.bson.DefaultBSONHandlers._

//    implicit val xpto: BSONReader[BSONArray, Seq[Int]] = bsonArrayToCollectionReader
//    implicit val xpto2: BSONWriter[Seq[Int], BSONArray] = findWriter2(collectionToBSONArrayCollectionWriter)
//    implicit val xpto3: BSONHandler[BSONArray, Seq[Int]] = BSONHandler.provided(xpto2, xpto)
    val a = implicitly[BSONHandler[BSONArray, Seq[Int]]]
    implicit val transportHandler: BSONDocumentHandler[Transport] = deriveMagnoliaHandler[Transport]
    implicit val coordinatesReader = deriveMagnoliaHandler[Coordinates]
    implicit val cityReader = deriveMagnoliaHandler[City]

    implicit val triphandler: BSONDocumentHandler[Trip] = deriveMagnoliaHandler[Trip]

    val cityA = City("Frauenfeld", Coordinates(1, 2))
    val cityB = City("Lisbon", Coordinates(3, 4))
    val trip =
    Trip(
      cities = Seq(cityA, cityB),
      transports = Seq(Flight("Swiss"), Taxi, Flight("Lufthansa"))
    )


    val cs = triphandler.write(trip)
    val t = triphandler.read(cs)

    println(t == trip)
  }

}
