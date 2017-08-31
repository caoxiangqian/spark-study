package hivestudy

import org.apache.spark.sql.Row
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer

object UserBehaviorAnalyze {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("UserBehaviorAnalyze")
    val sc = new SparkContext(conf)
    val sqlContext = new HiveContext(sc)
    import scala.collection.mutable.Map
    val queryParamMap: Map[String, List[String]] = Map()

    queryParamMap += ("cities" -> List("beijing"))

    val queryParamMapBroadcast = sc.broadcast(queryParamMap)

    val file = sc.textFile("/user/hadoop/spark-sql-test-data.txt")
    // 过滤数据
    val filterRdd = file.filter(line => {
      val arr = line.split("\t")
      val city = arr(3)
      queryParamMapBroadcast.value.get("cities").get
      val cities: List[String] = queryParamMapBroadcast.value.get("cities").get
      if (cities.size > 0 && cities.contains(city))
        true
      else
        false
    })

    //uv top 3

    val uvRdd = filterRdd.map(line => {
      val arr = line.split("\t")
      val ip = arr(0)
      val date = arr(1)
      val page = arr(2)
      (date + "_" + page, ip)
    }).groupByKey()
      .map(tuple => {
        val date_page = tuple._1
        val ips = tuple._2.iterator
        import scala.collection.mutable.Set
        val ipsSet: Set[String] = Set()
        for (ip <- ips) {
          ipsSet.add(ip)
        }
        (date_page, ipsSet.size)
      }).map(tuple => {
      val date_page = tuple._1
      val uv = tuple._2
      val arr = date_page.split("_")
      Row(arr(0), arr(1), uv) // date, page, uv
    })

    val schema = StructType(
      StructField("date", StringType, true) ::
        StructField("page", StringType, true) ::
        StructField("uv", IntegerType, true) ::
        Nil
    )
    val uvDF = sqlContext.createDataFrame(uvRdd, schema)
    uvDF.registerTempTable("date_page_uv")

    val resultRdd = sqlContext.sql(
      """
        select date,page,uv from (
        select date,page,uv, row_number() over (partition by date order by uv desc) rank
        from date_page_uv
        ) tmp where tmp.rank >= 3
      """.stripMargin)
    resultRdd.show()

    val totalUvRdd = resultRdd.map(row => (row(0), row(1) + "_" + row(2)))
      .groupByKey()
      .map(tuple => {
        val date = tuple._1
        val page_uv_list = tuple._2.iterator
        var total_uv = 0
        var str = date
        for (page_uv <- page_uv_list) {
          val arr = page_uv.split("_")
          total_uv += arr(1).toInt
          str += "," + page_uv
        }
        (total_uv, str)
      })
      .sortByKey(false)

    val top3DetailRdd = totalUvRdd.flatMap(tuple => {
      var str = tuple._2.toString
//      str = str.substring(0, str.length - 1)
      val arr = str.split(",")
      val date = arr(0)

      var listBuffer = new ListBuffer[Tuple3[String,String,Integer]]()
      for (i <- Array.range(1, arr.length)) {
        println(arr(i))
        val tmpArr = arr(i).split("_")
        listBuffer.append(Tuple3(date, tmpArr(0), tmpArr(1).toInt))
      }
      //      var list = scala.collection.mutable.List[String]()
      listBuffer
    }).map(tuple => Row(tuple._1, tuple._2, tuple._3))

    val df = sqlContext.createDataFrame(top3DetailRdd, schema)
    df.show()
    //    df.saveAsTable("top3detail")

  }

}
