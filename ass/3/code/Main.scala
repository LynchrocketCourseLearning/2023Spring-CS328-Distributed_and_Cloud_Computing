import org.apache.spark.sql.{SparkSession} // not necessary in spark-shell
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

import java.time.temporal.ChronoUnit

object parking {
    def main(args: Array[String]): Unit = {
        val spark = SparkSession.builder().appName("ShenZhen_Parking_Analysis").master("local[*]").getOrCreate() // not necessary in spark-shell

        val fileName = "hdfs://localhost:9000/CS328_ASS3/parking_data_sz.csv"
        val schema = StructType(Seq(
            StructField("out_time", TimestampType),
            StructField("admin_region", StringType),
            StructField("in_time", TimestampType),
            StructField("berthage", StringType),
            StructField("section", StringType)
        ))
        // read in dataset
        val textFile = {spark.read
                        .option("header", true)
                        .option("dateFormat", "yyyy-MM-dd HH:mm:ss")
                        .option("mode", "DROPMALFORMED")
                        .schema(schema)
                        .csv(fileName)}
        
        // coarse process
        val dateDF = {textFile
                        .filter("out_time > in_time")
                        .withColumn("duration", col("out_time").cast(IntegerType) - col("in_time").cast(IntegerType))}

        dateDF.cache()

        // problem 1
        val berthageCountOfEachSection = {dateDF
                                            .groupBy("section")
                                            .agg(countDistinct("berthage").alias("count"))}

        // problem 2
        val uniqueBerthageWithSection = {dateDF
                                            .dropDuplicates("berthage")
                                            .select("berthage", "section")}

        // problem 3
        val avgParkingTImeOfEachSection = {dateDF
                                            .groupBy("section")
                                            .agg(avg("duration").cast(IntegerType).alias("avg_parking_time"))
                                            .orderBy(desc("avg_parking_time"))}

        // problem 4
        val avgParkingTImeOfEachParkingLot = {dateDF
                                                .groupBy("berthage")
                                                .agg(avg("duration").cast(IntegerType).alias("avg_parking_time"))
                                                .orderBy(desc("avg_parking_time"))}

        // problem 5
        val timeList = udf((inTime: java.sql.Timestamp, duration: Int) => {
            val localDateTime = inTime.toLocalDateTime()
            val steps = duration / 1800

            var timeList = ""
            for(idx <- 0 until steps) {
                timeList += localDateTime.plus(idx * 30, ChronoUnit.MINUTES).toString + ","
            }
            timeList += localDateTime.plus(steps * 30, ChronoUnit.MINUTES).toString
            timeList
        })
        val slide30MinWindows = {dateDF
                                    .withColumn("time_list", timeList(col("in_time"), col("duration")))
                                    .withColumn("time", explode(split(col("time_list"), ",")))
                                    .groupBy(
                                        col("section"),
                                        window(col("time"), "30 minutes")
                                    ).agg(countDistinct("berthage").alias("count"))}
        val busyPercentage = {slide30MinWindows
                                .join(berthageCountOfEachSection.withColumnRenamed("count", "section_berthage_count"), "section")
                                .withColumn("percentage", round(col("count")*100/col("section_berthage_count"), 2))
                                .orderBy(asc("window.start"))
                                .selectExpr("date_format(window.start, 'yyyy-MM-dd HH:mm:ss') as start_time", "date_format(window.end, 'yyyy-MM-dd HH:mm:ss') as end_time", "section", "count", "percentage")}


        // save result
        val destDir = "hdfs://localhost:9000/CS328_ASS3/result/"
        berthageCountOfEachSection.coalesce(1).write.option("header", true).csv(destDir + "r1")
        uniqueBerthageWithSection.coalesce(1).write.option("header", true).csv(destDir + "r2")
        avgParkingTImeOfEachSection.coalesce(1).write.option("header", true).csv(destDir + "r3")
        avgParkingTImeOfEachParkingLot.coalesce(1).write.option("header", true).csv(destDir + "r4")
        busyPercentage.coalesce(1).write.option("header", true).option("dateFormat", "yyyy-MM-dd HH:mm:ss").csv(destDir + "r5")

        Thread.sleep(10000)
    }
}
