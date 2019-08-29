(ns conjure-deps.main
  "Munge dependencies for Conjure."
  (:require [clojure.string :as str]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.tools.namespace.find :as find]
            [clojure.tools.namespace.parse :as parse]
            [clojure.tools.namespace.file :as file]
            [clojure.tools.namespace.dependency :as dep]
            [mranderson.core :as ma]
            [valuehash.api :as vh]))

(def output-dir (io/file "output"))
(def munged-dir (io/file output-dir "conjure_deps"))
(def injection-orders-dir (io/file munged-dir "injection_orders"))

(defn- topo-sort
  "Sort the source paths in dependency order."
  [files]
  (let [parsed (->> files
                    (into {}
                          (map
                            (fn [file]
                              (let [decl (file/read-file-ns-decl file)]
                                [(parse/name-from-ns-decl decl)
                                 {:file file
                                  :deps (parse/deps-from-ns-decl decl)}])))))]
    (->> (vals parsed)
         (reduce
           (fn [g {:keys [deps] :as node}]
             (reduce
               (fn [g dep]
                 (if-let [target (get parsed dep)]
                   (dep/depend g node target)
                   g))
               g
               deps))
           (dep/graph))
         (dep/topo-sort)
         (mapv (fn [{:keys [file]}]
                 (-> (str file)
                     (str/replace #"^output/" "")))))))

(defn -main []
  (let [injected-deps (-> (io/resource "conjure_deps/injected_deps.edn")
                          (slurp)
                          (read-string))
        injection-order-file (io/file
                               injection-orders-dir
                               (str (vh/md5-str injected-deps) ".edn"))]
    (ma/mranderson {:clojars "https://repo.clojars.org"
                    :central "https://repo.maven.apache.org/maven2"}
                   (map #(with-meta % {:inline-dep true})
                        injected-deps)
                   {:pname "conjure-deps"
                    :pversion "0.0.0"
                    :pprefix "conjure-deps"
                    :srcdeps output-dir}
                   {:src-path output-dir
                    :parent-clj-dirs []
                    :branch []})

    (->> (with-out-str
           (pp/pprint
             {:clj (topo-sort (find/find-sources-in-dir munged-dir find/clj))
              :cljs (topo-sort (find/find-sources-in-dir munged-dir find/cljs))}))
         (spit injection-order-file))

    (println "Injection order written to:"
             (io/as-relative-path injection-order-file)))

  (shutdown-agents))
