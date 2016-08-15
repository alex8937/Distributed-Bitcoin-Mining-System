package local

import akka.actor._
import scala.io.StdIn.readLine

sealed trait Message 
case object Remote_Calculate extends Message
object Local extends App {
    implicit val system = ActorSystem("LocalSystem")
    val localActor = system.actorOf(Props[LocalActor],name="LocalActor")
    localActor ! "Start"

}

class LocalActor extends Actor {
    print("Enter your target IP:")
    var ip = readLine
    val remote = context.actorSelection("akka.tcp://mysystem@"+ip+":1234/user/remote_master")
    def receive = {
        case "Start"=>
            remote ! "Hello"
    }

}
