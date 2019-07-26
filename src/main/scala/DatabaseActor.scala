import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, Props}

import scala.util.Random

class DatabaseActor extends Actor with ActorLogging {

  import DatabaseActor._

  val minExecutionTimeInMillis = context.system.settings.config.getDuration("database.min-execution-time", TimeUnit.MILLISECONDS)
  val maxExecutionTimeInMillis = context.system.settings.config.getDuration("database.max-execution-time", TimeUnit.MILLISECONDS)
  val minSlowMultiplier = context.system.settings.config.getInt("database.min-slow-multiplier")
  val maxSlowMultiplier = context.system.settings.config.getInt("database.max-slow-multiplier")
  val minSlowCount = context.system.settings.config.getInt("database.min-slow-count")
  val maxSlowCount = context.system.settings.config.getInt("database.max-slow-count")
  val connectionTimeoutInMillis = context.system.settings.config.getDuration("database.connection-timeout", TimeUnit.MILLISECONDS)

  var minTime = minExecutionTimeInMillis
  var maxTime = maxExecutionTimeInMillis

  var slowCount = 0
  var slowReset = 0

  override def postRestart(reason: Throwable): Unit =
    log.info(s"I've restarted after a failure : $reason. That's what is called resilience")

  def receive = {
    case DatabaseRequest(req) =>
      log.info(s"Database received Request : $req")
      Thread.sleep(Util.randomWithin(minTime, maxTime))
      sender ! DatabaseResponse(s"Result for $req")
    case SimulateSlowDatabase =>
        val multiplier = Util.randomWithin(minSlowMultiplier, maxSlowMultiplier)
        minTime = minExecutionTimeInMillis * multiplier
        maxTime = maxExecutionTimeInMillis * multiplier
        slowCount = 0
        slowReset = Util.randomWithin(minSlowCount, maxSlowCount).toInt
    case SimulateDatabaseConnectionTimeout =>
      log.error("Timing out")
      throw new DatabaseConnectionTimeout(s"Database timeout after $connectionTimeoutInMillis millis") //timeout = true
  }
}

case class Customer(name: String, accountId: Int)

case class CheckingAccount(accountId: Int, liquidity: Int)

case class Operation(accountId: Int, action: Double)

object DatabaseActor {
  def props = Props[DatabaseActor]

  case class DatabaseRequest(request: String)

  case class DatabaseResponse(response: String)

  case class DatabaseConnectionTimeout(message: String) extends RuntimeException(message)

  case object SimulateSlowDatabase

  case object SimulateDatabaseConnectionTimeout

}

object Util {
  val random = new Random

  def randomWithin(min: Long, max: Long): Long = min + random.nextInt((max - min).toInt)

  def randomPercentage: Double = random.nextDouble() * 100
}
