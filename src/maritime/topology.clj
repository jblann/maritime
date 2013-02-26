(ns maritime.topology
  (:use [backtype.storm clojure config])
  (:require [maritime.volpe-parse :as volpe])
  (:import [backtype.storm StormSubmitter LocalCluster])
  (:import [backtype.storm.contrib.cassandra.bolt CassandraBatchingBolt CassandraBolt])
  (:gen-class))

(defn datafile [filename] (str "/Users/ryan/data/" filename))
(def volpe-fields ["rowid" "id" "name" "mmsi" "destPort" "eta" "maxDraft" "type"
                   "length" "timestamp" "coords" "speed" "trueHeading" "navStatus"
                   "courseOverGround" "receivingStation" "rateOfTurn" "receiverClass"
                   "messageType"])
(defspout xml-spout ["xml"]
  [conf context collector]
  (let [files (atom '("volpe1.xml" "volpe2.xml"))]
    (spout
     (nextTuple []
                (if (seq @files) (emit-spout! collector [(first @files)]))
                (Thread/sleep 5000)
                (swap! files rest))
     (ack [id]))))

(defbolt parse-volpe volpe-fields [tuple collector]
  (let [features (-> tuple :xml datafile volpe/process-file)]
    (doseq [feature features]
      (let [fact (map #(get feature %) volpe-fields)]
        (emit-bolt! collector fact :anchor tuple))
      (ack! collector tuple))))

(def write-volpe-cass (CassandraBatchingBolt. "volpe" "rowid"))

(defn mk-topology []
  (topology
   {"1" (spout-spec xml-spout)}
   {"2" (bolt-spec {"1" :shuffle}
                   parse-volpe
                   :p 3)
    "3" (bolt-spec {"2" :shuffle}
                   write-volpe-cass
                   :p 4)}))

(defn run-local! []
  (let [cluster (LocalCluster.)
        topology-config {TOPOLOGY-DEBUG false
                         CassandraBolt/CASSANDRA_HOST "localhost:9160"
                         CassandraBolt/CASSANDRA_KEYSPACE "DEMO"}]
    (.submitTopology cluster "maritime" topology-config (mk-topology))
    (Thread/sleep 20000)
    (.shutdown cluster)))

(defn submit-topology! [name]
  (StormSubmitter/submitTopology
   name
   {TOPOLOGY-DEBUG true
    TOPOLOGY-WORKERS 3}
   (mk-topology)))

(defn -main
  ([]
   (run-local!))
  ([name]
   (submit-topology! name)))

  