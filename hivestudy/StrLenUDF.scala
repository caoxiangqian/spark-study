package hivestudy

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

object StrLenUDF {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("StrLenUDF")
    val sc = new SparkContext(conf)
    val sqlContext = new HiveContext(sc)
    sqlContext.udf.register("strLen", (line: String) => {
      if (line != null)
        line.length
      else
        0
    })
    val resultDF = sqlContext.sql("select address, strLen(address) from test.people")
    resultDF.show()
    resultDF.rdd.saveAsTextFile("result" + System.currentTimeMillis())
  }
}
