# .bash_profile

# Get the aliases and functions
if [ -f ~/.bashrc ]; then
	. ~/.bashrc
fi

# User specific environment and startup programs
export JAVA_HOME=/usr/java/latest
export JRE_HOME=$JAVA_HOME/jre
export CLASSPATH=.:$CLASSPATH:$JAVA_HOME/lib:$JRE_HOME/lib
export HADOOP_HOME=/home/hadoop/hadoop273
PATH=$PATH:$JAVA_HOME/bin:$JRE_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$HOME/.local/bin:$HOME/bin

export SCALA_HOME=/home/hadoop/scala-2.12.2
PATH=$PATH:$SCALA_HOME/bin

export SPARK_HOME=/home/hadoop/spark1.6.3
PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin

export HIVE_HOME=/home/hadoop/hive1.2.2
PATH=$PATH:$HIVE_HOME/bin

export HBASE_HOME=/home/hadoop/hbase1.2.6
PATH=$PATH:$HBASE_HOME/bin

export PATH

