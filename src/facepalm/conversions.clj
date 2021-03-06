(ns facepalm.conversions
  (:use [clojure.java.io :only [file reader]]
        [cemerick.pomegranate :only [add-dependencies]])
  (:require [clojure.string :as string]
            [clojure-commons.file-utils :as fu]
            [me.raynes.fs :as fs])
  (:import [clojure.lang DynamicClassLoader]
           [java.io PushbackReader]))

(def ^:private dependency-filename
  "dependencies.clj")

(def ^:private default-repositories
  {"central" "https://repo1.maven.org/maven2"
   "clojars" "https://clojars.org/repo/"})

;; This function was copied from https://github.com/amperity/riemann/.
(defn- ensure-dynamic-classloader
  []
  (let [thread (Thread/currentThread)
        context-class-loader (.getContextClassLoader thread)
        compiler-class-loader (.getClassLoader clojure.lang.Compiler)]
    (when-not (instance? DynamicClassLoader context-class-loader)
      (.setContextClassLoader
       thread (DynamicClassLoader. (or context-class-loader
                                       compiler-class-loader))))))

(defn- drop-extension
  [fname]
  (first (string/split fname #"\.")))

(defn- split-on-last-underscore
  [fname]
  ; regex: underscore + negative lookahead specifying that no underscores follow it
  ; limit of 2 returned strings
  (string/split fname #"_(?!.*_)" 2))

(defn- dotize
  [vstr]
  (cond
    (re-find #"_" vstr)
    (string/replace vstr #"_" ".")

    (= (count vstr) 3)
    (string/join "." (into [] vstr))

    :else
    (throw (Exception. "Version string must either be three digits or specify dot placement with underscores"))))

(defn- fmt-version
  [[version-str date-str]]
  [(-> version-str
       (string/replace #"^c" "")
       dotize)
   date-str])

(defn- fmt-date-str
  [date-str]
  (let [date-vec (into [] date-str)]
    (str
     (string/join (take 8 date-vec)) "." (string/join (take-last 2 date-vec)))))

(defn- fmt-date
  [[vstr date-str]]
  [vstr (fmt-date-str date-str)])

(defn- db-version
  [parts]
  (string/join ":" parts))

(defn- fname->db-version
  [fname]
  (-> fname
      fu/basename
      drop-extension
      split-on-last-underscore
      fmt-version
      fmt-date
      db-version))

(defn- fname->ns-str
  [fname]
  (-> (str "facepalm." fname)
      (string/replace #"\.clj$" "")
      (string/replace #"_" "-")))

(defn- ns-str->cv-str
  [ns-str]
  (str ns-str "/convert"))

(defn- fname->cv-ref
  [fname]
  (-> fname
      fu/basename
      fname->ns-str
      ns-str->cv-str
      symbol
      eval))

(defn- list-conversions
  []
  (map str (fs/find-files "conversions" #"^c.*_[0-9]{10}\.clj$")))

(defn- load-conversions
  [cv-list]
  (doseq [cv cv-list]
    (load-file cv)))

(defn- load-dependency-file
  [f]
  (with-open [r (PushbackReader. (reader f))]
    (binding [*read-eval* false]
      (read r))))

(defn- build-proxy-config
  [opts]
  (when (:proxy-host opts)
    {:host (:proxy-host opts)
     :port (:proxy-port opts)}))

(defn load-dependencies
  [opts]
  (ensure-dynamic-classloader)
  (let [f (fs/file "conversions" dependency-filename)]
    (when (.isFile f)
      (let [{:keys [dependencies repositories]} (load-dependency-file f)]
        (add-dependencies :coordinates   dependencies
                          :repositories (merge default-repositories repositories)
                          :proxy        (build-proxy-config opts))))))

(defn conversion-map
  [opts]
  (load-dependencies opts)
  (let [conversions (list-conversions)]
    (load-conversions conversions)
    (into {} (map #(vector (fname->db-version %) (fname->cv-ref %)) conversions))))
