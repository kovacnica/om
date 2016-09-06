(set-env!
 :source-paths    #{"src/main"}
 :dependencies '[[org.clojure/clojure         "1.9.0-alpha10"  :scope "provided"]
                 [org.clojure/clojurescript   "1.9.227"        :scope "provided"
                  :classifier "aot" :exclusions [org.clojure/clojure
                                                 org.clojure/data.json]]
                 [org.clojure/data.json       "0.2.6"          :scope "provided"
                  :classifier "aot"]
                 [cljsjs/react                "15.2.1-0"]
                 [cljsjs/react-dom            "15.2.1-0"]
                 [com.cognitect/transit-clj   "0.8.288"]
                 [com.cognitect/transit-cljs  "0.8.239"]

                 [org.clojure/core.async      "0.2.385"        :scope "test"
                  :exclusions [org.clojure/tools.reader]]
                 [figwheel-sidecar            "0.5.6"          :scope "test"
                  :exclusions [org.clojure/clojurescript
                               org.clojure/tools.reader]]
                 [devcards                    "0.2.1-7"        :scope "test"
                  :exclusions [org.clojure/clojurescript]]
                 [com.cemerick/piggieback     "0.2.1"          :scope "test"
                  :exclusions [org.clojure/clojure
                               org.clojure/tools.nrepl
                               org.clojure/clojurescript]]
                 [pandeiro/boot-http          "0.7.3"          :scope "test"]
                 [adzerk/boot-cljs            "1.7.228-1"      :scope "test"]
                 [adzerk/boot-cljs-repl       "0.3.3"          :scope "test"]
                 [crisptrutski/boot-cljs-test "0.2.2-SNAPSHOT" :scope "test"]
                 [doo                         "0.1.7"          :scope "test"
                  :exclusions [org.clojure/clojurescript]]
                 [adzerk/boot-reload          "0.4.12"         :scope "test"]
                 [org.clojure/tools.nrepl     "0.2.12"         :scope "test"]
                 [weasel                      "0.7.0"          :scope "test"
                  :exclusions [org.clojure/clojure
                               org.clojure/clojurescript]]])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :as cr :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[crisptrutski.boot-cljs-test :refer [test-cljs]]
 '[pandeiro.boot-http :refer [serve]])

(deftask devcards []
  (set-env! :source-paths #(conj % "src/devcards")
            :resource-paths #{"resources/public"})
  (comp
    (serve :port 3449)
    (watch)
    (cljs-repl)
    (reload)
    (speak)
    ;; remove possible artifacts of Figwheel compilation
    (sift :include #{#"^devcards\/(out\/|main.js)"} :invert true)
    (cljs :source-map true
          :compiler-options {:devcards true
                             :main 'om.devcards.core
                             :parallel-build true}
          :ids #{"devcards/main"})
    (target)))

(deftask testing []
  (set-env! :source-paths #(conj % "src/test"))
  identity)

(ns-unmap 'boot.user 'test)

(deftask test
  [e exit?     bool  "Enable flag."]
  (let [exit? (cond-> exit?
                (nil? exit?) not)]
    (comp
      (testing)
      (test-cljs
        :js-env :node
        :namespaces #{'om.next.tests}
        :cljs-opts {:parallel-build true}
        :exit? exit?))))

(deftask auto-test []
  (comp
    (watch)
    (speak)
    (test :exit? false)))
