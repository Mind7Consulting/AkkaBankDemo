import CheckingAccountActor._
import DatabaseActor._
import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props}
import akka.routing.{DefaultResizer, RoundRobinPool}


class CheckingAccountActor extends Actor with ActorLogging {
  //  val tick =
  //    context.system.scheduler.scheduleOnce(10 seconds, self, "stop")

      val resizer = DefaultResizer(lowerBound = 2, upperBound = 15)
      val databaseActor = context.actorOf(RoundRobinPool(5, Some(resizer)).props(DatabaseActor.props), "databaseActor")
//  val databaseActor = context.actorOf(DatabaseActor.props, "databaseActor")
  import scala.concurrent.duration._
    override val supervisorStrategy = {
      OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 10 seconds) {
        case _ =>
          context.become(receiveWithKafka)
          Restart
      }
    }
  val dbActor = context.actorOf(DatabaseActor.props)
  var counter = 0


  override def receive: Receive = slowWithDrawReceive

  def regularReceive: Receive = {
    case AddCheckingAccount(_) => dbActor ! SimulateSlowDatabase
    case DepositCheckingAccount(amount) =>
      databaseActor ! DatabaseRequest(s"Making a deposit of $amount")
    case WithdrawCheckingAccount(amount) =>
      databaseActor ! DatabaseRequest(s"Withdrawing $amount")
  }

  def slowWithDrawReceive: Receive = {
    case AddCheckingAccount(_) => dbActor ! SimulateSlowDatabase
    case DepositCheckingAccount(amount) =>
      databaseActor ! DatabaseRequest(s"Making a deposit of $amount")
    case WithdrawCheckingAccount(amount) =>
      databaseActor ! SimulateDatabaseConnectionTimeout
    case DatabaseResponse(_) =>
      counter = counter + 1
      log.info(s"I've processed $counter operations")
    case "stop" =>
      context.system.terminate() // NEVER DO THiS IN PRODUCTION

  }

  def receiveWithKafka: Receive = {
    case _ =>
      databaseActor ! DatabaseRequest(s"Writing to Kafka")
  }
}

object CheckingAccountActor {
  def props = Props[CheckingAccountActor]

  case class AddCheckingAccount(accountId: Int)

  case class DeleteCheckingAccount(accountId: Int)

  case class DepositCheckingAccount(amount: Int)

  case class WithdrawCheckingAccount(amount: Int)

}