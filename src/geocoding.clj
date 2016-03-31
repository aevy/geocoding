(ns geocoding
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [camel-snake-kebab.core :refer :all]))

(def api-key "AIzaSyDowYmjIt7u1eA0VD375Mxa1iS0cl1SkPM")

(def base-url "https://maps.googleapis.com/maps/api/geocode/")

(defn keywordize-keys [m]
  (let [f (fn [[k v]] (if (string? k) [(->kebab-case-keyword k) v] [k v]))]
    (clojure.walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn geocode [s opts]
  (-> (http/get (str base-url "json")
                {:query-params (assoc (transform-keys ->snake_case opts) :address s)})
      :body
      json/read-str
      keywordize-keys
      :results))
