import akka.actor.{Actor, Props}

class CustomerActor extends Actor {

  import CustomerActor._
  import DatabaseActor._

  val dbActor = context.actorOf(DatabaseActor.props)

  override def receive: Receive = {
    case AddCustomer(_) => dbActor ! SimulateSlowDatabase
    case GetCustomer(_) => dbActor ! SimulateSlowDatabase
  }
}

object CustomerActor {
  def props = Props[CustomerActor]

  case class AddCustomer(customer: Customer)

  case class GetCustomer(customer: Customer)

}