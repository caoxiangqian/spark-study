#spark-submit --class hivestudy.HiveOperation --master yarn --deploy-mode cluster --files $HIVE_HOME/conf/hive-site.xml --jars $HIVE_HOME/lib/mysql-connector-java-6.0.6.jar ./spark-hive-1.0-SNAPSHOT-jar-with-dependencies.jar
#spark-submit --class hivestudy.HiveOperation --master yarn --deploy-mode cluster --jars /home/hadoop/hive1.2.2/lib/datanucleus-api-jdo-3.2.6.jar /home/hadoop/hive1.2.2/lib/datanucleus-core-3.2.10.jar /home/hadoop/hive1.2.2/lib/datanucleus-rdbms-3.2.9.jar --files $HIVE_HOME/conf/hive-site.xml ./spark-hive-1.0-SNAPSHOT-jar-with-dependencies.jar
#spark-submit --class hivestudy.HiveOperation --master yarn --deploy-mode cluster ./spark-hive-1.0-SNAPSHOT.jar


spark-submit --class hivestudy.StrLenUDF --master yarn --jars $SPARK_HOME/lib/datanucleus-api-jdo-3.2.6.jar,$SPARK_HOME/lib/datanucleus-core-3.2.10.jar,$SPARK_HOME/lib/datanucleus-rdbms-3.2.9.jar ./spark-hive-1.0-SNAPSHOT.jar

spark-sql --master yarn --jars $SPARK_HOME/lib/datanucleus-api-jdo-3.2.6.jar,$SPARK_HOME/lib/datanucleus-core-3.2.10.jar,$SPARK_HOME/lib/datanucleus-rdbms-3.2.9.jar --files $SPARK_HOME/conf/hive-site.xml