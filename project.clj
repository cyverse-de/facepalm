;; IMPORTANT NOTE: Both an RPM and a tarball are generated for this project.
;; Because the release number is not recorded anywhere in the tarball, minor
;; changes need to be recorded in the version number.  Please increment the
;; minor version number rather than the release number for minor changes.
(use '[clojure.java.shell :only (sh)])
(require '[clojure.string :as string])

(defn git-ref
  []
  (or (System/getenv "GIT_COMMIT")
      (string/trim (:out (sh "git" "rev-parse" "HEAD")))
      ""))

(defproject org.cyverse/facepalm "2.10.0-SNAPSHOT"
  :description "Command-line utility for DE database managment."
  :url "https://github.com/cyverse-de/facepalm"
  :license {:name "BSD"
            :url "http://iplantcollaborative.org/sites/default/files/iPLANT-LICENSE.txt"}
  :manifest {"Git-Ref" ~(git-ref)}
  :uberjar-name "facepalm-standalone.jar"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "1.0.194"]
                 [org.clojure/tools.logging "1.1.0"]
                 [cheshire "5.10.0"]
                 [clj-commons/pomegranate "1.2.0"]
                 [fleet "0.10.2"]
                 [com.mchange/c3p0 "0.9.5.5"]
                 [korma "0.4.3"
                  :exclusions [c3p0]]
                 [me.raynes/fs "1.4.6"]
                 [log4j "1.2.17"]
                 [org.cyverse/clojure-commons "3.0.5"]
                 [org.cyverse/kameleon "3.0.4"]
                 [org.postgresql/postgresql "42.2.14"]
                 [slingshot "0.12.1"]
                 [clj-http "2.0.0"]]
  :plugins [[lein-ancient "0.6.15"]
            [lein-marginalia "0.7.1"]
            [test2junit "1.2.2"]]
  :aot :all
  :main facepalm.core)
