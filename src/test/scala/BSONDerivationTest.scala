import BSONDerivation.Typeclass
import reactivemongo.bson.{BSONArray, BSONHandler, BSONValue}
import reactivemongo.bson.DefaultBSONHandlers._

object BSONDerivationTest {

  implicit def SeqHandler[T](implicit handler: BSONHandler[_ <: BSONValue, T]): BSONHandler[BSONArray , Seq[T]] =
    BSONHandler(
      v => new BSONArrayCollectionReader[Seq, T]().read(v),
      ts => BSONArray(ts.map(handler.write))
    )


  case class Coordinates(lat: Double, long: Double)
  case class City(name: String, location: Coordinates)

  sealed trait Transport
  case class Flight(airline: String) extends Transport
  case object Taxi extends Transport

  case class Trip(cities: Seq[City], transports: Seq[Transport])

  private val cityA = City("Frauenfeld", Coordinates(1, 2))
  private val cityB = City("Lisbon", Coordinates(3, 4))
  private val trip =
    Trip(
      cities = Seq(cityA, cityB),
      transports = Seq(Flight("Swiss"), Taxi, Flight("Lufthansa"))
    )

  def main(args: Array[String]): Unit = {
    implicit val transportWriter: Typeclass[Transport] = BSONDerivation.gen[Transport]
    implicit val cityWriter: Typeclass[City] = BSONDerivation.gen[City]
    val cs = BSONDerivation.gen[Trip].write(trip)
    val t = BSONDerivation.gen[Trip].read(cs)
    println(t)
  }

}
