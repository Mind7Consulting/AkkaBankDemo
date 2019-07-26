import BankActor.CreateCustomer
import CustomerActor.AddCustomer
import akka.actor.{Actor, ActorLogging, Props}

class BankActor extends Actor with ActorLogging {

  import CheckingAccountActor._

  val checkingAccountActor = context.actorOf(CheckingAccountActor.props, "checking-account-actor")

  val customerActor = context.actorOf(CustomerActor.props, "customer-actor")

  override def receive: Receive = {
    case CreateCustomer(customer) => customerActor ! AddCustomer(customer)
    case Operation(_, action) =>
      if (action > 0) checkingAccountActor ! DepositCheckingAccount(action.toInt)
      else checkingAccountActor ! WithdrawCheckingAccount(action.toInt)
  }
}

object BankActor {
  def props = Props(new BankActor())

  case class CreateCustomer(customer: Customer)

}

// ---------- With routing --------
//class BankActor extends Actor with ActorLogging {
//
//  import BankActor._
//  import CheckingAccountActor._
//  import CustomerActor._
//
//  private val resizer = DefaultResizer(lowerBound = 2, upperBound = 15)
//  private val routerCheckingAccount =
//    context.actorOf(RoundRobinPool(5, Some(resizer)).props(CheckingAccountActor.props), "router30")
//
//  private val customerActor = context.actorOf(CustomerActor.props, "customer")
//
//  override def receive: Receive = {
//    case CreateCustomer(customer) => customerActor ! AddCustomer(customer)
//    case MakeOperation(operation) =>
//      if (operation.action > 0)
//        routerCheckingAccount ! DepositCheckingAccount(operation.action.toInt)
//      else
//        routerCheckingAccount ! WithdrawCheckingAccount(operation.action.toInt)
//  }
//}
//
//object BankActor {
//  def props = Props[BankActor]
//
//  case class CreateCustomer(customer: Customer)
//
//  case class MakeOperation(operation: Operation)
//
//}