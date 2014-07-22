(defproject ezglib "0.1.4-SNAPSHOT"
  :description "An easy game library for ClojureScript."
  :url "https://github.com/bakpakin/ezglib"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:name "git"
         :url "https://github.com/bakpakin/ezglib"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2268"]]
  :plugins [[lein-cljsbuild "1.0.3"]]
  :hooks [leiningen.cljsbuild]
  :source-paths ["src"]
  :cljsbuild {
    :builds {
      :src {
        :jar true
        :source-paths ["src"]
        :incremental? true}
      :examples {
        :incremental? true
        :source-paths ["src" "examples/src"]
        :compiler {:output-to "examples/js/cljs.js"
                   :output-dir "examples/js"
                   :optimizations :none
                   :pretty-print true
                   :source-map "examples/js/cljs.js.map"}}}})
