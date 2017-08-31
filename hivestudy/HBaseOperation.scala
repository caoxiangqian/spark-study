package sparksqlstudy

import org.apache.hadoop.hbase.{CellUtil, HBaseConfiguration}
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer

case class People(id: Integer, age: String, name: String)

object HBaseOperation {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
    conf.setAppName("HBaseOperation")
    //    conf.set("spark.executor.extraJavaOptions", "-XX:-OmitStackTraceInFastThrow -XX:-UseGCOverheadLimit")

    val sc = new SparkContext(conf)

    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    val hbaseConf = HBaseConfiguration.create()
    hbaseConf.set("hbase.mapreduce.inputtable", "people")
    hbaseConf.set("hbase.zookeeper.qurom", "hadoop1:2181")

    val hbaseRDD = sc.newAPIHadoopRDD(
      hbaseConf,
      classOf[TableInputFormat],
      classOf[ImmutableBytesWritable],
      classOf[Result])

    println("count =================================== " + hbaseRDD.count())

    val peopleDF = hbaseRDD.map(tuple => {
      //id
      val id = Bytes.toString(tuple._1.get()).trim.toInt
      val result = tuple._2

      val arrBuf = ArrayBuffer[String]()
      for (cell <- result.rawCells()) {
        arrBuf += Bytes.toString(CellUtil.cloneValue(cell))
      }
      (id, arrBuf(0), arrBuf(1))
    })
      .map(t => People(t._1, t._2, t._3))
      .toDF()

    peopleDF.registerTempTable("people")

    sqlContext.sql("select id, age, name from people").show()


  }

}
