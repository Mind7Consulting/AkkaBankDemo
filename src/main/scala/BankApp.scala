import BankActor.CreateCustomer
import akka.actor.ActorSystem

import scala.io.Source

object BankApp extends App {
  val operations = Source.fromResource("accountsOperations.csv")
    .getLines.toList.tail
    .map(_.split(","))
    .map(element => Operation(element(0).toInt, element(1).toDouble))

  val customers = Source.fromResource("customers.csv").getLines.toList.tail
    .map(_.split(","))
    .map(element => Customer(element(1), element(0).toInt))

  val system = ActorSystem("BankSystem")

  val bankActor = system.actorOf(BankActor.props, "bankActor")

  // create all the customers
  customers.foreach { customer =>
    bankActor ! CreateCustomer(customer)
  }

  // handle the operations on an infinite loop
  Iterator.continually(operations).flatten.foreach { operation =>
    bankActor ! operation
    Thread.sleep(100)
  }

}
