(defproject snap-spot "0.1.0-SNAPSHOT"
  :description "snapchat for geo"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [http-kit "2.1.18"]
                 [com.taoensso/carmine "2.9.1"]
                 [com.taoensso/timbre "3.4.0"] 
                 [compojure "1.3.2"]
                 [ring/ring-devel "1.3.2"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-core "1.3.2"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-mock "0.2.0"]
                 [javax.servlet/servlet-api "2.5"]
                 [org.clojure/data.json "0.2.6"]
                 [bouncer "0.3.3"]
                 [clj-time "0.9.0"]
                 [me.shenfeng/mustache "1.1"]]
  :main ^:skip-aot snap-spot.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
