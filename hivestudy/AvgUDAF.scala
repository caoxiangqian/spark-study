package hivestudy

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.Row
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.types._

class AvgUDAF extends UserDefinedAggregateFunction {

  override def inputSchema: StructType = StructType(StructField("value", DoubleType) :: Nil)

  override def bufferSchema: StructType = StructType(StructField("total", DoubleType) :: StructField("count", IntegerType) :: Nil)

  override def dataType: DataType = DoubleType

  override def deterministic: Boolean = true

  override def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer(0) = 0.0
    buffer(1) = 0
  }

  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    buffer(0) = buffer.getAs[Double](0) + input.getAs[Double](0)
    buffer(1) = buffer.getAs[Integer](1) + 1
  }

  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    buffer1(0) = buffer1.getAs[Double](0) + buffer2.getAs[Double](0)
    buffer1(1) = buffer1.getAs[Integer](1) + buffer2.getAs[Integer](1)
  }

  override def evaluate(buffer: Row): Any = {
    if (buffer.getAs[Integer](1) == 0)
      0
    else
      buffer.getAs[Double](0) / buffer.getAs[Integer](1)
  }
}

object AvgUDAF {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("AvgUDAF")
    val sc = new SparkContext(conf)
    val sqlContext = new HiveContext(sc)

    sqlContext.udf.register("my_avg", new AvgUDAF)
    sqlContext.sql("use test")
    val resultRdd = sqlContext.sql("select my_avg(age) from people")
    resultRdd.show()
  }
}
