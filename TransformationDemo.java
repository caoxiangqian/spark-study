import org.apache.spark.Partitioner;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import scala.Tuple2;

import java.io.*;
import java.util.*;

public class TransformationDemo {

    static List<Integer> list1 = new ArrayList<>();

    static String str = "D:\\worksoft\\jdk1.8.0_131\\bin\\java -javaagent:E:\\dev_lib\\ideaIC-2017.1.4\\lib\\idea_rt.jar=51901:E:\\dev_lib\\ideaIC-2017.1.4\\bin -Dfile.encoding=UTF-8 -classpath D:\\worksoft\\jdk1.8.0_131\\jre\\lib\\charsets.jar;";

    static List<String> list2 = new ArrayList<>();

    static JavaSparkContext sc = null;

    static List<String> readFile() {
        List<String> list2FileTxt = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream("C:\\Users\\admin\\Desktop\\eng_doc.txt")));
            String line = null;
            while ((line = br.readLine()) != null) {
                list2FileTxt.add(line);
            }
            br.close();
            System.out.println("read file ended.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return list2FileTxt;
    }


    static {
        SparkConf conf = new SparkConf();
        conf.setMaster("local").setAppName("SparkTransformationDemo");
        sc = new JavaSparkContext(conf);

        Random r = new Random();
        for (int i = 0; i < 20; i++) {
            list1.add(r.nextInt(1000));
        }

        System.out.println();

        for (String i : str.split("\\\\")) {
            list2.add(i);
        }

    }

    public static void main(String[] args) {
//        map();
//        filter();
//        flatMap();
//        mapPartitions();
//        mapPartitionsWithIndex();
//        sample();
//        union();
//        intersection();
//        distinct();
//        groupByKey();
//        reduceByKey();
//        aggregateByKey();
//        sortByKey();
//        joinAndCogroup();
//        cartesian();
//        coalesce();
//        repartition();
        repartitionAndSortWithinPartitions();
    }

    static void repartitionAndSortWithinPartitions() {
        sc.parallelize(readFile())
                .flatMapToPair(s -> {
                    List<Tuple2<String, Integer>> list = new ArrayList<>();
                    for (String str : s.split(" ")) {
                        list.add(new Tuple2<>(str, 1));
                    }
                    return list.iterator();
                })
                .repartitionAndSortWithinPartitions(new Partitioner() {
                    @Override
                    public int getPartition(Object key) {
                        int r = key.toString().hashCode() % 3;
                        return r > 0 ? r : -r;
                    }

                    @Override
                    public int numPartitions() {
                        return 3;
                    }
                })
                .distinct()
                .foreach(s -> {
                    int key = s._1.toString().hashCode() % 3;
                    System.out.println("key code " + key + " " + s);
                });
    }

    /**
     * 等同于 coalesce(numPartitions, true)
     */
    static void repartition() {
        sc.parallelize(readFile(), 2)
                .repartition(3)
                .foreach(s -> System.out.println(s));
    }


    /**
     * 重新分区
     * M : 原分区数
     * N : 新分区数
     * 如果 M < N ，分区变多了，shuffle 最好为true
     * 如果 M > N ，分区变少， shuffle 推荐为false, 不会发生shuffle，并发数为N
     * 如果 M >> N ，分区变少， shuffle 推荐为true, 会发生shuffle,就会并发执行
     */
    static void coalesce() {
        sc.parallelize(readFile(), 3)
                .coalesce(2, true)
                .foreach(s -> System.out.println(s));
    }

    static void cartesian() {
        List<Tuple2<String, Integer>> list1 = new ArrayList<>();
        list1.add(new Tuple2<>("java", 1));
        list1.add(new Tuple2<>("java", 111));
        list1.add(new Tuple2<>("scala", 2));
        list1.add(new Tuple2<>("c", 3));
        list1.add(new Tuple2<>("python", 100));
        list1.add(new Tuple2<>("swift", 100));

        List<Tuple2<String, String>> list2 = new ArrayList<>();
        list2.add(new Tuple2<>("java", "caoxiangqian"));
        list2.add(new Tuple2<>("c", "cxq"));
        list2.add(new Tuple2<>("scala", "jack"));
        list2.add(new Tuple2<>("python", "tom"));
        list2.add(new Tuple2<>("python", "tom"));
        list2.add(new Tuple2<>("python", "jerry"));
        list2.add(new Tuple2<>("sql", "jerry"));

        JavaPairRDD<String, Integer> rdd1 = sc.parallelizePairs(list1);
        JavaPairRDD<String, String> rdd2 = sc.parallelizePairs(list2);
        // 笛卡尔集
        rdd1.cartesian(rdd2).foreach(t -> System.out.println(t));

        sc.parallelize(Arrays.asList("a", "b", "c")).cartesian(
                sc.parallelize(Arrays.asList(1, 2, 3, 4))
        ).foreach(s -> System.out.println(s));

    }

    static void joinAndCogroup() {
        List<Tuple2<String, Integer>> list1 = new ArrayList<>();
        list1.add(new Tuple2<>("java", 1));
        list1.add(new Tuple2<>("java", 111));
        list1.add(new Tuple2<>("scala", 2));
        list1.add(new Tuple2<>("c", 3));
        list1.add(new Tuple2<>("python", 100));
        list1.add(new Tuple2<>("swift", 100));

        List<Tuple2<String, String>> list2 = new ArrayList<>();
        list2.add(new Tuple2<>("java", "caoxiangqian"));
        list2.add(new Tuple2<>("c", "cxq"));
        list2.add(new Tuple2<>("scala", "jack"));
        list2.add(new Tuple2<>("python", "tom"));
        list2.add(new Tuple2<>("python", "tom"));
        list2.add(new Tuple2<>("python", "jerry"));
        list2.add(new Tuple2<>("sql", "jerry"));


        JavaPairRDD<String, Integer> rdd1 = sc.parallelizePairs(list1);
        JavaPairRDD<String, String> rdd2 = sc.parallelizePairs(list2);
        // 相当于表的内联接，不会出空记录, 基于cogroup实现
        // 类似的操作还有 leftOuterJoin, rightOuterJoin, fullOuterJoin
        rdd1.join(rdd2).foreach(t -> System.out.println(t));

        /**
         *  When called on datasets of type (K, V) and (K, W), returns a
         *  dataset of (K, (Iterable<V>, Iterable<W>)) tuples. This
         *  operation is also called groupWith.
         *  不同的rdd内，会根据key对value进行分组，并放入一个集合内，然后返回一个元组，
         *  第一个元素是key(K),第二个是values的元组，values._1(Iterable<V>)是第一个rdd的value的集合，
         *  values._2(Iterable<W>)是第二个rdd的value的集合
         *
         */
        rdd1.cogroup(rdd2).foreach(t -> {
            System.out.println(t);
        });


    }

    static void sortByKey() {
        sc.parallelize(readFile())
                .flatMapToPair(s -> {
                    List<Tuple2<String, Integer>> list = new ArrayList<>();
                    for (String str : s.split(" ")) {
                        list.add(new Tuple2<>(str, 1));
                    }
                    return list.iterator();
                })
                .distinct()
                .sortByKey()
                .foreach(s -> System.out.println(s));

    }


    static void aggregateByKey() {
        sc.parallelize(readFile(), 3)
                .flatMapToPair(s -> {
                    List<Tuple2<String, Integer>> list = new ArrayList<>();
                    for (String str : s.split(" ")) {
                        list.add(new Tuple2<>(str, 1));
                    }
                    return list.iterator();
                })
                .aggregateByKey(0, (i, j) -> i + j, (i, j) -> i + j)
                .foreach(s -> System.out.println(s));

    }

    static void reduceByKey() {
        JavaRDD<String> rdd = sc.parallelize(readFile()).flatMap(s -> Arrays.asList(s.split(" ")).iterator());
        rdd.mapToPair(s -> new Tuple2<>(s, 1))
                .reduceByKey((i, j) -> i + j)
                .foreach(t -> System.out.println(t));
    }

    static void groupByKey() {
        JavaRDD<String> rdd = sc.parallelize(readFile())
                .flatMap(s -> Arrays.asList(s.split(" ")).iterator());
        rdd.distinct()
                .mapToPair(s -> new Tuple2<>(s.length(), s))
                .groupByKey()
                .foreach(g -> System.out.println(g));

    }

    // 去重
    static void distinct() {
        JavaRDD<String> rdd = sc.parallelize(readFile())
                .flatMap(s -> Arrays.asList(s.split(" ")).iterator());
        System.out.println(rdd.count());
        System.out.println(rdd.distinct().count());

    }

    static void intersection() {
        JavaRDD<String> rdd = sc.parallelize(readFile())
                .flatMap(s -> Arrays.asList(s.split(" ")).iterator());
        JavaRDD<String> rdd2 = sc.parallelize(Arrays.asList("Basics", "Hello"));
        //取交集
        rdd.intersection(rdd2).foreach(s -> System.out.println(s));
    }

    static void union() {
        JavaRDD<String> rdd = sc.parallelize(readFile());
        System.out.println(rdd.count());
        JavaRDD<String> rdd2 = rdd.union(rdd);
        System.out.println("rdd2 :" + rdd2.count());

    }

    static void sample() {
        JavaRDD<String> rdd = sc.parallelize(readFile());
        // 采样，withReplacement true: 放回，false: 不放回
        rdd.sample(true, 0.3)
                .foreach(s -> System.out.println(s));


    }

    static void mapPartitionsWithIndex() {
        sc.parallelize(readFile())
                .mapPartitionsWithIndex((index, i) -> {
                    List<String> list = new ArrayList<>();
                    while (i.hasNext()) {
                        for (String s : i.next().split(" ")) {
                            list.add(s);
                        }
                    }
                    return list.iterator();
                }, false)
                .foreach(s -> System.out.println(s));

    }

    static void mapPartitions() {
//        sc.parallelize(readFile(), 3)
//                .mapPartitions(new FlatMapFunction<Iterator<String>, String>() {
//                    @Override
//                    public Iterator<String> call(Iterator<String> stringIterator) throws Exception {
//                        List<String> list = new ArrayList<>();
//
//                        while (stringIterator.hasNext()) {
//                            for (String item : stringIterator.next().split(" ")) {
//                                list.add(item);
//                            }
//                        }
//                        return list.iterator();
//                    }
//                })
//                .mapToPair(s -> new Tuple2<>(s, 1))
//                .reduceByKey((i, j) -> i + j)
//                .foreach(t -> System.out.println(t._1 + "\t" + t._2));
        // whole lambda
        sc.parallelize(readFile())
                .mapPartitions(i -> {
                    List<String> list = new ArrayList<>();
                    while (i.hasNext()) {
                        for (String s : i.next().split(" ")) {
                            list.add(s);
                        }
                    }
                    return list.iterator();
                })
                .mapToPair(s -> new Tuple2<>(s, 1))
                .reduceByKey((i, j) -> i + j)
                .foreach(t -> System.out.println(t._1 + "\t" + t._2));

    }

    static void flatMap() {
//        JavaRDD<String> rdd = sc.parallelize(readFile());
//        JavaRDD<String> rddFlatMap = rdd.flatMap(s -> Arrays.asList(s.split(" ")).iterator());
//        Long count = rddFlatMap.count();
//        System.out.println(count);
//        rddFlatMap.mapToPair(s -> new Tuple2<>(s, 1))
//                .reduceByKey((i,j) -> i +j)
//                .foreach(t -> System.out.println(t._1 + "       " + t._2));

        sc.parallelize(readFile(), 2)
                .filter(s -> s.trim().length() != 0)
                .flatMapToPair(new PairFlatMapFunction<String, String, Integer>() {
                    @Override
                    public Iterator<Tuple2<String, Integer>> call(String s) throws Exception {
                        List<Tuple2<String, Integer>> list = new ArrayList<>();

                        for (String item : s.split(" ")) {
                            list.add(new Tuple2<>(item, 1));
                        }
                        return list.iterator();
                    }
                })
                .reduceByKey((i, j) -> i + j)
                .foreach(t -> System.out.println(t._1 + "       " + t._2));

    }


    static void filter() {
        JavaRDD<String> rdd = sc.parallelize(list2);

        JavaRDD<String> rddFilter = rdd.filter(s -> s.indexOf("jar") != -1);
        rddFilter.foreach(s -> System.out.println(s));
    }

    static void map() {
        JavaRDD<String> rdd = sc.parallelize(list2);
        JavaRDD<Tuple2<String, Integer>> rddMap = rdd.map(s -> new Tuple2<>(s, 1));
        rddMap.foreach(s -> System.out.println(s));

        System.out.println("----------------------------------------------");

        JavaPairRDD<String, Integer> rddPair = rdd.mapToPair(i -> new Tuple2<>(i, 1));

        JavaPairRDD<String, Integer> rddPair2 = rddPair.reduceByKey((i, j) -> i + j);
        rddPair2.foreach(s -> System.out.println(s._1 + ":   " + s._2));

    }

}