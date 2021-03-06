package ideal.sylph.plugins.flink.transform

import ideal.sylph.etl.api.TransForm
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.table.api.{Table, TableEnvironment}
import org.apache.flink.types.Row

@SerialVersionUID(2L) //使用注解来制定序列化id
class SqlWindow extends TransForm[DataStream[Row]] {

  override def transform(stream: DataStream[Row]): DataStream[Row] = {

    //    val tb = stream.map(row => {
    //      val value = row.getField(1).asInstanceOf[String]
    //      val json = new JSONObject(value.replaceAll("\\}\u0001\\{", ","))
    //      (
    //        json.getString("user_id"),
    //        json.getString("client_type"),
    //        new Timestamp(json.getLong("server_time"))
    //      )
    //    })

    //    println(tb.dataType)
    //    tableEnv.registerDataStream("tp", tb, 'user_id, 'client_type,'rowtime.rowtime)
    //    val result2:Table = tableEnv.sql(
    //      """SELECT user_id, count(1) FROM tp
    //        | GROUP BY HOP(proctime, INTERVAL '5' SECOND, INTERVAL '5' MINUTE),
    //        |  user_id""".stripMargin
    //    )
    val execEnv: StreamExecutionEnvironment = stream.getExecutionEnvironment
    val tableEnv = TableEnvironment.getTableEnvironment(execEnv)
    val result2: Table = tableEnv.sqlQuery(
      s"""SELECT TUMBLE_START(rowtime, INTERVAL '5' SECOND) AS s,
         |  TUMBLE_END(rowtime, INTERVAL '5' SECOND) AS e,
         |  user_id,
         |  COUNT(1) as cnt
         | FROM tp
         | GROUP BY user_id, TUMBLE(rowtime, INTERVAL '5' SECOND)
         |
      """.stripMargin)

    tableEnv.toAppendStream(result2, classOf[Row])
  }

  override def driverInit(optionMap: java.util.Map[String, Object]): Unit = {
  }
}
