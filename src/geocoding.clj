(ns geocoding
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.set :refer [rename-keys]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [camel-snake-kebab.core :refer :all]))

(def base-url "https://maps.googleapis.com/maps/api/geocode/")

(defn keywordize-keys [m]
  (let [f (fn [[k v]] (if (string? k) [(->kebab-case-keyword k) v] [k v]))]
    (clojure.walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn get-address-components [m]
  (reduce (fn [m' ac]
            (assoc m'
                   (->kebab-case-keyword (get-in ac [:types 0]))
                   (dissoc ac :types)))
          {}
          m))

(defn format-result [{:keys [address-components types] :as m}]
  (let [address-components* (get-address-components address-components)]
    (-> (merge m address-components*)
        (update :locality :long-name)
        (dissoc :address-components))))

(defn geocode [s opts]
  (->> (http/get (str base-url "json")
                 {:query-params (assoc (transform-keys ->snake_case opts) :address s)})
       :body
       json/read-str
       keywordize-keys
       :results
       (map format-result)))
