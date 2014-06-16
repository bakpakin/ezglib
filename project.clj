(defproject ezglib "0.1.2"
  :description "An easy game library for ClojureScript."
  :url "https://github.com/bakpakin/ezglib"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:name "git"
         :url "https://github.com/bakpakin/ezglib"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]]
  :plugins [[lein-cljsbuild "1.0.3"]]
  :cljsbuild {
    :builds {
      :examples {
        :source-paths ["src" "examples/src"]
        :compiler {:output-to "examples/js/cljs.js"
        :output-dir "examples/js"
        :optimizations :none
        :pretty-print true
        :source-map "examples/js/cljs.js.map"}}}})
