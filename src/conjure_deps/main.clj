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

(defn- munge!
  "Fetch and munge dependencies."
  [injected-deps]
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
                  :branch []}))

(defn- nodes->graph
  "Converts a map of namespace information into a graph."
  [nodes]
  (let [node-map (into {}
                       (map (fn [node] [(:ns-sym node) node]))
                       nodes)]
    (->> nodes
         (reduce
           (fn [g {:keys [deps] :as node}]
             (reduce
               (fn [g dep]
                 (if-let [target (get node-map dep)]
                   (dep/depend g node target)
                   g))
               g
               deps))
           (dep/graph)))))

(defn- files->nodes!
  "Convert a seq of files into a seq of namespace nodes. Requires reading
  those files from disk and parsing them."
  [files]
  (map
    (fn [file]
      (let [decl (file/read-file-ns-decl file)]
        {:file file
         :ns-sym (parse/name-from-ns-decl decl)
         :deps (parse/deps-from-ns-decl decl)}))
    files))

(defn- nodes->paths
  "Flatten a seq of nodes into injectable path strings."
  [nodes]
  (mapv
    (fn [{:keys [file]}]
      (-> (str file)
          (str/replace #"^output/" "")))
    nodes))

(defn- build-injection-order!
  "Find :clj or :cljs namespaces that are required to load the given namespace symbols in order."
  [lang ns-syms]
  (let [nodes (->> (find/find-sources-in-dir
                     munged-dir
                     (case lang
                       :clj find/clj
                       :cljs find/cljs))
                   (files->nodes!))
        full-graph (nodes->graph nodes)
        required-nodes (set
                         (filter
                           (fn [{:keys [ns-sym]}]
                             (contains? ns-syms ns-sym))
                           nodes))]
    (-> full-graph
        (dep/transitive-dependencies-set required-nodes)
        (concat required-nodes)
        (nodes->graph)
        (dep/topo-sort)
        (nodes->paths))))

(defn -main []
  (let [injected-deps (-> (io/resource "conjure_deps/injected_deps.edn")
                          (slurp)
                          (read-string))
        injection-order-file (io/file
                               injection-orders-dir
                               (str (vh/md5-str injected-deps) ".edn"))]
    (munge! injected-deps)

    (->> (with-out-str
           (pp/pprint
             {:clj (build-injection-order! :clj #{'conjure-deps.compliment.v0v3v9.compliment.core
                                                  'conjure-deps.toolsnamespace.v0v3v1.clojure.tools.namespace.repl})
              :cljs (build-injection-order! :cljs #{})}))
         (spit injection-order-file))

    (println "Injection order written to:"
             (io/as-relative-path injection-order-file)))

  (shutdown-agents))
