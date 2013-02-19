(ns clj4store.core
  (:use [clojure.string :only (join)])
  (:require [http.async.client :as http]
            [clojure.java.io :as io]))
 






;- ----------------------------------------------------------------------------
;- PREFIXES

(def base-prefixes {:rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                    :rdfs "http://www.w3.org/2000/01/rdf-schema#"
                    :dc "http://purl.org/dc/terms/":dc11 "http://purl.org/dc/elements/1.1/"
                    :skos "http://www.w3.org/2004/02/skos/core#"
                    :geonames "http://www.geonames.org/ontology#"
                    :wgs84_pos "http://www.w3.org/2003/01/geo/wgs84_pos#"
                    :foaf "http://xmlns.com/foaf/0.1/"
                    :owl "http://www.w3.org/2002/07/owl#"
                    :schema "http://schema.org/"})



(defn sparql-prefixes
  "merge hashes of prefixes and return a sparql prefixes string
  (sparql-prefixes base-prefixes {:ex \"http://example.com/\"})"
  [& prefixes]
  (let [p (reduce #(merge % %2) {} prefixes)]
    (join "\n" (map (fn [[k v]] (str "PREFIX " (name k) ": <" v "> ")) p))))


  
; ;- ----------------------------------------------------------------------------
; ;- query sparql endpoint



(defn create-end-point 
  "return uri's to connect to the 4store end-point." 
  [end-point] 
  {:sparql (str end-point "/sparql/")
   :update (str end-point "/update/")
   :data 	 (str end-point "/data/")})



(defn prefixed-query 
  "add a prefixes string to the sparql query "
  [prefixes query]
  (str prefixes "\n" query ))



(defn get 
  "sparql query"
  [end-point query]
  (with-open [client (http/create-client)]
    (let [res (http/GET client (:sparql end-point) 
                        :query {:query query}
                        :headers {:accept "application/sparql-results+json"})]
      (http/await res)
      {:status (:code (http/status res))
       :body (http/string res)})))
           

        
(defn post 
  "sparql update"
  [end-point query] 
  (with-open [client (http/create-client)]
    (let [res (http/POST client (:update end-point) 
                        :body {:update query}
                        :headers {:content-type "application/x-www-form-urlencoded"})]
      (http/await res)
    	{:status (:code (http/status res)) 
       :body (http/string res)})))



(defn put 
  "replace data"
  [end-point data graph] 
  (with-open [client (http/create-client)]
    (let [res (http/PUT client (str (:data end-point) graph)
                        :body data
                        :headers {:content-type "application/x-turtle"})]
    	(http/await res)
      {:status (:code (http/status res)) 
       :body (http/string res)})))



(defn put-file 
  "replace data by file data"
  [end-point file graph] 
  (let [ff (io/file file)]
    (put end-point ff graph)))




(defn delete 
  "delete graph"
  [end-point graph] 
  (with-open [client (http/create-client)]
    (let [res (http/DELETE client (str (:data end-point)graph))]
      (http/await res)
    	{:status (:code (http/status res)) 
       :body (http/string res)})))







; examples 

; define the 4store end-point
; (def end-point (create-end-point "http://0.0.0.0:8009"))

; define a sparql prefixes string
; (def prefixes-str (sparql-prefixes base-prefixes {:ex "http://test.com"}))

; sparql query without prefixes
; (get end-point "SELECT ?s ?p ?o WHERE {?s ?p ?o} LIMIT 5 ") ; => {:status 200 :body {"head":{"vars":["s","p","o"]}, "results": { "bindings":[... }

; sparql query with prefixes
; (get end-point (prefixed-query prefixes-str "SELECT ?s ?p ?o WHERE {?s ?p ?o} LIMIT 5 ")) ; => {:status 200 :body {"head":{"vars":["s","p","o"]}, "results": { "bindings":[... }

; sparql update 1.1
; (post end-point (prefixed-query prefixes-str "INSERT { ex:test1 <http://test.com/p1> <http://test.com/2> }")) ; => {:status 200 :body "4store body message..." }

; replace turtle/ntriples data in graph
; (put end-point "<http://test.com/2> <http://test.com/p1> <http://test.com/1> ." "http://mygraph.com") ; => {:status 201 :body "imported successfully ..." }

; replace data in graph by file data
; (put-file end-point "/Users/ouvasam/Dropbox/ouvanous/projets/actifs/clojure/clj4store/src/clj4store/test.nt" "http://mygraph.com") ; => {:status 201 :body "imported successfully ..." }

; delete graph
; (delete end-point "http://mygraph.com") ; => {:status 200 :body "200 deleted successfully ..." }















