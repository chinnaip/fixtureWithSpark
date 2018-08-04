package com.hp.curiosity.fixtures.general

import java.util.UUID

import com.hp.propertiesservice.ZkCentralPropertiesService
import kafka.producer.KeyedMessage
import org.apache.log4j.Logger
import org.apache.spark.{SparkConf, SparkContext, _}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream._
import play.api.libs.json._
import org.apache.spark.streaming.kafka010.{ConsumerStrategies,LocationStrategies}
import org.apache.spark.streaming.kafka010.KafkaUtils
import scala.collection.immutable
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import com.github.benfradet.spark.kafka010.writer._
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.elasticsearch.spark._
import scala.collection.immutable.Set
import scala.collection.mutable.Set
import scala.collection.JavaConversions._
import org.elasticsearch.hadoop.cfg.ConfigurationOptions._
import scala.collection.mutable.HashMap
import scala.annotation.tailrec
import org.joda.time.{DateTimeComparator, DateTimeZone, LocalDate, LocalDateTime}
import org.joda.time.DateTime
import scala.util.Random


case class PaCommonConfig(val ps_environment: String,
  val application_namespace: String,
      val ps_zk_quorum: String,val ps_kafka_truststore_path:String,val ps_kafka_truststore_passwd:String) {

  val log = Logger.getLogger(getClass.getName)

  val msgProducerBroker = "metadata.broker.list"

  val esNodes = "es.nodes"
  val kafkaSecurityProtocol = "kafka.security.protocol"
  val kafkaTrustStoreLocation = "kafka.ssl.truststore.path"
  val kafkaTrustStorePassword = "kafka.ssl.truststore.password"
  val securityProtocol = "security.protocol"
  val trustStoreLocation = "ssl.truststore.location"
  val trustStorePassword = "ssl.truststore.password"
  val kafkaAutoCommit = "true"
  val kafkaOffsetReset = "earliest"

  lazy val zk = new ZkCentralPropertiesService(ps_environment, application_namespace, ps_zk_quorum)

  def getKafkaBroker() = {
    zk.getProperty(msgProducerBroker).get()
  }
    
  def getSecureKafkaConsumerProperties(cg: String, brokers: String) = {
    val kProps = new HashMap[String, Object]()
    kProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    kProps.put(ConsumerConfig.GROUP_ID_CONFIG, cg + UUID.randomUUID().toString())
    kProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    kProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    kProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaAutoCommit)
    kProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaOffsetReset)
    kProps.put(securityProtocol, zk.getProperty(kafkaSecurityProtocol).get())
    kProps.put(trustStoreLocation, ps_kafka_truststore_path)
    kProps.put(trustStorePassword, ps_kafka_truststore_passwd)
    kProps
  }
  
  def getSecureKafkaProducerProperties(brokers: String) = {
    val kProps = new java.util.Properties()
    kProps.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    kProps.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    kProps.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    kProps.put(securityProtocol, zk.getProperty(kafkaSecurityProtocol).get())
    kProps.put(trustStoreLocation, ps_kafka_truststore_path)
    kProps.put(trustStorePassword, ps_kafka_truststore_passwd)
    kProps
  }

  def getEScfg() = {

    HashMap(ES_MAPPING_ID -> "prediction_id",ES_NET_HTTP_AUTH_PASS -> zk.getProperty(ES_NET_HTTP_AUTH_PASS).get(),ES_NET_HTTP_AUTH_USER -> zk.getProperty(ES_NET_HTTP_AUTH_USER).get, ES_NET_USE_SSL -> zk.getProperty(ES_NET_USE_SSL).get(), ES_NODES_DISCOVERY -> zk.getProperty(ES_NODES_DISCOVERY).get(), ES_NODES_WAN_ONLY -> zk.getProperty(ES_NODES_WAN_ONLY).get(), ES_NODES -> zk.getProperty(ES_NODES).get(), ES_PORT -> "9200", ES_INDEX_AUTO_CREATE -> "false", ES_MAPPING_DATE_RICH_OBJECT -> "true", "pushdown" -> " true", "spark.sql.inMemoryColumnarStorage.compressed" -> "true", "es.input.json" -> "yes")

  }
}

class PaConnectionsUtil

object PaConnectionsUtil {

    val log = Logger.getLogger(getClass.getName)
    val kafkaAutoCommit = "true"
    val kafkaOffsetReset = "earliest"
    log.setLevel(org.apache.log4j.Level.ERROR)

    val sc = new SparkConf().setAppName("PA-FITNESSE").setMaster("local[*]")
    sc.set("spark.authenticate","true")
    sc.set("spark.authenticate.secret","paSparkAuthenticateSecretKey")
    val sparkCtx = SparkContext.getOrCreate(sc)
    class SeqAccumulatorParam[B] extends AccumulatorParam[Seq[B]] {
        override def zero(initialValue: Seq[B]): Seq[B] = Seq[B]() //initialize the accumulator
        override def addInPlace(s1: Seq[B], s2: Seq[B]): Seq[B] = s1++s2 // add into the accumulator
    }


   

    def collectThingFormationList(acc: Accumulator[Seq[String]]): (DStream[String] => Unit) = {
        def c(input: DStream[String]): Unit = {

            input.foreachRDD {r: RDD[String] =>  {
                r.foreach {
                        e: String => {
                            acc += Seq(e);
                            log.info(e)
                        }
                    }
            }
            }
        }
        c _
    }

  def parseJsonPath(path: String) = {

    @tailrec
    def _parse(j: JsPath, nodes: List[String]): JsPath = {
      nodes match {
        case x :: Nil => (j \ x)
        case x :: xs  => _parse(j \ x, xs)
        case _        =>  j
      }
    }

    _parse(__, if(path.isEmpty()) List[String]() else path.split('/').toList)
  }

  def jsPick(j: String, path: String) = {
    Json.parse(j).transform((parseJsonPath(path)).json.pick)
  }

  def parseJsonValue(j: String, path: String) = jsPick(j, path).asOpt.get

  def jsUpdateValue[A](j: String, path: String, key: String, value: A)(implicit w: Writes[A]) = {
    Json.stringify(Json.parse(j).transform(parseJsonPath(path).json.update(__.read[JsObject].map(o => o ++ Json.obj(key -> value)))).asOpt.get)
  }
    
    
    def randomUUIDString: String = {
        UUID.randomUUID().toString()
    }

  def produceFileToEsParameterized(path: String, resource: String,
                      cfg: HashMap[String, String], jp:String, serial_no: String) {
    val sparkCtx = SparkContext.getOrCreate(sc)
    val sqlContext: SQLContext = new SQLContext(sparkCtx)
    val nodes = jp.split('/')
    def _randomizeDateAndSerialNumber(js:String,index:Int) = {
    jsUpdateValue(jsUpdateValue(js,"/","latest_symptom_timestamp",LocalDateTime.now().minusDays(index).minusMillis(index).toString()),"/", "prediction_id", UUID.randomUUID().toString)  
    }
    val producerEventLogs: RDD[String] = readAndReplaceFile(path, sqlContext, nodes.dropRight(1).mkString("/"),nodes.last, serial_no).zipWithIndex.map(x => _randomizeDateAndSerialNumber(x._1,x._2.toInt))
      writeToEs(producerEventLogs,resource,cfg)
    
  }
  
  
  def produceFileToKafkaSecure(path: String, topic: String,
                               producerConfig: java.util.Properties,jp:String, serial_no: String) {
    val sparkCtx = SparkContext.getOrCreate(sc)
    val sqlContext: SQLContext = new SQLContext(sparkCtx)
    val nodes = jp.split('/')
    val producerEventLogs: RDD[String] = readAndReplaceFile(path, sqlContext, nodes.dropRight(1).mkString("/"),nodes.last, serial_no)
    writeToKafka(producerEventLogs,topic,producerConfig)
  }
  
  def writeToKafka(logs: RDD[String],topic:String,cfg:java.util.Properties)  = {
    logs.writeToKafka(cfg, (devConfig: String) => { new ProducerRecord[String, String](topic, devConfig) })
  }
  
  def writeToEs(logs:RDD[String],resource:String,cfg:HashMap[String,String]) = {

    logs.saveJsonToEs(resource, Map[String, String]() ++ cfg)
  }
  
  def produceMsgToKafkaParameterized(msg:String,topic:String,
      producerConfig: java.util.Properties,jp:String, serial_no: String) {
    val sparkCtx = SparkContext.getOrCreate(sc)
    writeToKafka(sparkCtx.parallelize(Seq(msg)),topic,producerConfig)
    
  }
  def produceMsgToEsParameterized(msg: String, resource: String,
                      cfg: HashMap[String, String], jp:String, serial_no: String) {
    val sparkCtx = SparkContext.getOrCreate(sc)
    writeToEs(sparkCtx.parallelize(Seq(msg)),resource,cfg)
  }

  def readAndReplaceFile(path: String, sqlContext: SQLContext,jp:String,randomNoKey: String, serial_no: String): RDD[String] = {

    val eventLogs: DataFrame = sqlContext.read.json("file:" + path)
   
    eventLogs.toJSON.map(e => jsUpdateValue(e, jp, randomNoKey, serial_no))

  } 
    
    def replaceSnInString(whichString: String,
                        newChar: String): String = {

        whichString.replace("RANDOM_SERIAL_NUMBER", newChar)
    }



  def consumeFromKafkaByThingAndThingFormationSecure(topics: String, thing_id: String, thingFormation_id: String, expiration: Int, kafkaParams: scala.collection.mutable.Map[String, Object]) = {
      val sparkCtx = SparkContext.getOrCreate(sc)
      val ssc = new StreamingContext(sparkCtx,Seconds(expiration/3000))

    val outputmsgs = sparkCtx.accumulator(Seq[String]())(new SeqAccumulatorParam[String]())


    val consumerStrategy = ConsumerStrategies.Subscribe[String, String](topics.split(",").map(_.trim).filter(!_.isEmpty).toSet, kafkaParams)

    val kafkaStream = KafkaUtils.createDirectStream(ssc, LocationStrategies.PreferBrokers, consumerStrategy)
    val logs = kafkaStream.map(_.value())

    collectThingFormationList(outputmsgs)(logs.filter(x => x.contains(thing_id) && (x.contains(thingFormation_id))))

    //Start streaming
    ssc.start()

    var awaiting: Boolean = true

    while (!(outputmsgs.value.length > 0) && awaiting) {
      awaiting = ssc.awaitTerminationOrTimeout(expiration)
    }
    
    
    

    val o = if (outputmsgs.value.length > 0) outputmsgs.value else Seq[String]("NoMessageReceived")
    ssc.stop(false)
    o(0)
  }

  def consumeFromEsByThingAndThingFormation(resource: String, query: String, cfg: HashMap[String, String], expiration: Int) = {

    val sparkCtx = SparkContext.getOrCreate(sc)

    val logs = sparkCtx.esJsonRDD(resource, query, Map[String, String]() ++ cfg).collect().toList

    val o = if (logs.length > 0) logs.map(x => x._2) else Seq[String]("NoMessageReceived")
    o(0)
  }
}
