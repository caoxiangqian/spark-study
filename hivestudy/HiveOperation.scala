package hivestudy

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.hive.HiveContext

object HiveOperation {

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("HiveOperation")
    val sc = new SparkContext(sparkConf)

    val sqlContext = new HiveContext(sc)
    sqlContext.sql("use test")
    val teenage = sqlContext.sql("SELECT id,name,age,address FROM people WHERE age > 16 AND age < 30")
    teenage.show()
    sqlContext.sql("create table teenage_people LIKE people")
    teenage.write.insertInto("teenage_people")
  }
}
