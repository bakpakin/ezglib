(defproject ezglib "0.1.7"
  :description "Make WebGL games in ClojureScript."
  :url "https://github.com/bakpakin/ezglib"
  :dependencies [[tailrecursion/cljs-priority-map "1.1.0"]]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:name "git"
        :url "https://github.com/bakpakin/ezglib"}
  :profiles {:dev {:dependencies [[org.clojure/clojurescript "0.0-2280"]
                                  [org.clojure/clojure "1.6.0"]]
                   :plugins [[codox "0.8.10"]
                             [lein-cljsbuild "1.0.3"]
                             [org.bodil/cljs-noderepl "0.1.11"]]}}
  :cljsbuild {
              :builds {
                       :dev {
                             :source-paths ["src" "examples/src"]
                             :compiler {:output-to "examples/js/cljs.js"
                                        :output-dir "examples/js"
                                        :optimizations :none
                                        :pretty-print true
                                        :source-map "examples/js/cljs.js.map"}}}}
  :codox {:language :clojurescript
          :include [ezglib.core ezglib.gl ezglib.util ezglib.math ezglib.protocol]
          :src-dir-uri "http://github.com/bakpakin/ezglib/blob/master/"
          :src-linenum-anchor-prefix "L"})
