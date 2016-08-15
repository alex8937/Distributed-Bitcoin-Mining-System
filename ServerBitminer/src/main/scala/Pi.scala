package remote


import com.roundeights.hasher.Implicits._
import scala.language.postfixOps
import scala.io.StdIn.readInt
import akka.actor._
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem
import akka.routing.RoundRobinPool
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout



object Bitcoin{
    sealed trait Message 
    case object Calculate extends Message // Start calculation message 
    case object Remote_Calculate extends Message
    
    case class Work(hashMe_origin: String, num0: Int,nrOfWorkers: Int, myid:Int) extends Message 
    // Assign task by passing num : number of co-workers
    // num0: number of zeros
    // nrOfWorkers: number of workers
    // myid: my working id among the coworkers //starting from 0
    case class Result(answer : String) extends Message // one result
    case class Totalresult(duration: Duration) extends Message// Whole results and time duraction
    
    class Worker extends Actor {
    
        def addstr(num:Long, id:String):String= { //Function for generate a suffix string 
// num: number input to be converted according to base; Start from 0 -> inf // base: the base of number; Particularly, 95 in typeable value
// id: the string to be added on
            var record = num
            var remainder:Long = 0
            var counter: Long=0
            var id0 = id
            val base: Long = 95L
            if(record == 0) {      	    
// Dubug purpose    println(0)                             
// Dubug purpose    println("[" + (32).toChar + "]") 
               id0 = id0 + (32).toChar     
	        }   

            while(record != 0) {
       	        remainder = record%base-counter
// Dubug purpose    println(remainder)
// Dubug purpose    println("[" + (remainder+32).toChar + "]")
                id0 = id0 + (remainder+32).toChar
                record = record/base
                counter = 1
                if(record >= base) {
                    id0 = addstr(record, id0)
                    record = 0
                }
	        }
            return id0
        } 
        
        def FindingHash(hashMe_origin: String, num0: Int, nrOfWorkers: Int, myid:Int): String={

            val Max_num_coworker = 10 //Max number of coworks
            var matchstr0 = ""
            var i = 1
           
            var hashMe = hashMe_origin
            var hashvalue = hashMe.sha256.hex

            for (i <- 1 to num0) {
                matchstr0=matchstr0 + "0"
            } 
            var flag = hashvalue.regionMatches(0, matchstr0, 0, num0)     
            var j = 0L
            var counterhash = 0 
            while(counterhash < 1){  //number of required string to be found for each actor
                while(flag == false){
                    hashMe = hashMe_origin
// Dubug purpose    hashMe = addstr(myid+Max_num_coworker*j,hashMe)
                    hashMe = addstr(myid+nrOfWorkers*j, hashMe)
                    hashvalue = hashMe.sha256.hex
                    flag=hashvalue.regionMatches(0, matchstr0, 0, num0)
                    j = j + 1
// Dubug purpose    println("{"+hashMe+"}")
                }
                flag=false
                counterhash=counterhash+1
                //println(hashMe+"    "+hashvalue)  //Print every answer meeting the requirement
                                  
            }
           return hashMe + "    " + hashvalue   
        }    
    
        def receive ={
            case Work(hashMe_origin,num0,nrOfWorkers,myid) =>                 
                sender ! Result(FindingHash(hashMe_origin, num0, nrOfWorkers, myid) )
         //   case "A" => for (i <-1 to 1)  println("A")
         //  case "B" => for (i <-1 to 1)  println("B")
         //   case "Stop" => context.stop(self)
         }
    }//End of class Worker     
    
    class Master(hashMe_origin: String,num0: Int,nrOfWorkers: Int) extends Actor{

        var calculateSender: ActorRef = _
        var nrOfResults: Int = 0
        val start: Long = System.currentTimeMillis        
        val workerRouter = context.actorOf(RoundRobinPool(nrOfWorkers).props(Props[Worker]),"workerRouter")  //Set up a router to assign tasks    
        def receive = {
            case Calculate =>          
                    for(i<-0 until nrOfWorkers) {
                    workerRouter !  Work(hashMe_origin,num0,nrOfWorkers,i) 
                    }
                    
                    calculateSender = sender   
            case Result(answer) =>
                println(answer) 
                nrOfResults += 1
                if (nrOfResults == 1) { //Number of answer to be found
                    calculateSender ! Totalresult((System.currentTimeMillis -start).millis)
                    self ! PoisonPill
                }
                
            case "Hello" => println("Remote worker found")
                workerRouter !  Work(hashMe_origin,num0,nrOfWorkers*nrOfWorkers,nrOfWorkers) 
                calculateSender = sender        
        }
    
    }//End of class Master    
    
   // implicit val timeout=Timeout(600 seconds)
     
    implicit val timeout = Timeout(24 hours)
    
    
    def main(args: Array[String]) {
        if(args.length < 2) {
            System.err.println("[Bitcoin]Wrong inputs!!")
            System.err.println("[Bitcoin]Usage: run <nrof zero> <nrofcoworker>")
            System.exit(1)
        }
        var nrof0 = args(0).toInt
        var nrofworker = args(1).toInt
        val system = ActorSystem("mysystem") 

       val master = ystem.actorOf(Props(new Master("alex8937",nrof0,nrofworker)),name="master")  //args(0).toInt:Number of zero; args(1).toInt:number of workers 
  
          
       val future = master ? Calculate
                
       val remote_master = system.actorOf(Props(new Master("alex8937",args(0).toInt,nrofworker+1)),name="remote_master")        
       
        
       val approximation = Await.result(future,timeout.duration).asInstanceOf[Totalresult]
       
        
       println("Spend: "+ approximation.duration)
       
       system.terminate()
    }

}








