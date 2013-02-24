(ns maritime.volpe-parse
  (:use [clojure.data.xml])
  (:require [clojure.java.io :as io]))

(defn dive [keyword element]
  (first (filter #(= keyword (:tag %)) (:content element))))
(defn grab [keyword element]
  (->> element (dive keyword) :content first))

(defn process-feature [feature]
  (let [vessel-data (dive :VesselData feature)
        track (->> vessel-data (dive :VesselTrack) (dive :VesselMovingObjectStatus))
        id (-> vessel-data :attrs :vesselSourceID)
        timestamp (->> track (grab :timestamp))]
    {"rowid" (str id timestamp)
     "id" id
     "name" (->> vessel-data (grab :sailedAsName))
     "mmsi" (->> vessel-data (grab :sailedAsMmsi))
     "destPort" (->> vessel-data (grab :destPort))
     "eta" (->> vessel-data (grab :eta))
     "maxDraft" (->> vessel-data (grab :maxDraft))
     "type" (->> vessel-data (grab :vesselTypeCode))
     "length" (->> vessel-data (grab :sailedAsLength))
     "timestamp" timestamp
     "coords" (->> track (dive :location) (dive :Point) (grab :coordinates))
     "speed" (->> track (grab :speed))
     "trueHeading" (->> track (grab :trueHeading))
     "navStatus" (->> track (grab :navigationStatus))
     "courseOverGround" (->> track (grab :courseoverGround))
     "receivingStation" (->> track (grab :receivingStation))
     "rateOfTurn" (->> track (grab :rateofTurn))
     "receiverClass" (->> track (grab :receiverClass))
     "messageType" (->> track (grab :aisMessageType))}))

(defn process-file [filename]
  (let [data (-> filename io/input-stream parse)
        features (->> data 
                      :content 
                      (filter #(= :featureMember (:tag %))) 
                      (map process-feature))]
    features))