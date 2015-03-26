(defproject snap-spot "0.1.0-SNAPSHOT"
  :description "snapchat for geo"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.taoensso/carmine "2.9.0"]
                 [compojure "1.3.2"]
                 [ring/ring-devel "1.3.2"]
                 [ring/ring-core "1.3.2"]
                 [javax.servlet/servlet-api "2.5"]
                 [http-kit "2.1.18"]
                 [me.shenfeng/mustache "1.1"]]
  :main ^:skip-aot snap-spot.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
