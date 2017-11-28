#!/bin/bash

YESTERDAY=`date -d '-1 day' +%Y%m%d`
echo "yesterday is $YESTERDAY"

# Log file dir
LOG_FILE_PATH=/home/hadoop/logs-hive/$YESTERDAY

for FILE in `ls $LOG_FILE_PATH`
do
	echo "the log file name is $FILE"
	DAY=${FILE:0:8}
	HOUR=${FILE:8:2}
	echo "day : $DAY, hour : $HOUR"
	#执行hive的命令
	hive --hiveconf LOG_FILE_PATH=$LOG_FILE_PATH --hiveconf FILE=$FILE --hiveconf DAY=$DAY --hiveconf HOUR=$HOUR -f /home/hadoop/load_to_hive.sql
done

hive --database log -e "show partitions log_src"




