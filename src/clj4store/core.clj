(ns clj4store.core
  (:use [clojure.string :only (join)])
  (:require [http.async.client :as http]
            [clojure.java.io :as io]))
 






;- ----------------------------------------------------------------------------
;- PREFIXES


(def prefixes 
  "defines base prefixes to add to a sparql query"
  (atom {:rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         :rdfs "http://www.w3.org/2000/01/rdf-schema#"
         :dc "http://purl.org/dc/terms/"
         :dc11 "http://purl.org/dc/elements/1.1/"
         :skos "http://www.w3.org/2004/02/skos/core#"
         :geonames "http://www.geonames.org/ontology#"
         :wgs84_pos "http://www.w3.org/2003/01/geo/wgs84_pos#"
         :foaf "http://xmlns.com/foaf/0.1/"
         :owl "http://www.w3.org/2002/07/owl#"
         :schema "http://schema.org/"}))




(defn register-prefix 
  "register a new prefix
  (register-prefix :test \"http://test.com\")"
  [prefix uri]
  (swap! prefixes conj {prefix uri}))



(defn register-prefixes 
  "register a hash of prefixes
  (register-prefixes {:test1 \"http://test1.com\"
                      :test2 \"http://test2.com\"})"
  [new-prefixes]
  (swap! prefixes conj new-prefixes))



(defn prefixes-string 
  "return a string of PREFIX to use in the SPARQL query"
  []
  (join "\n"
    (map (fn [[prefix uri]] (str "PREFIX " (name prefix) ": <" uri ">")) @prefixes)))








; ;- ----------------------------------------------------------------------------
; ;- query sparql endpoint



(defn create-end-point 
  "return uri's to connect to the 4store end-point." 
  [end-point] 
  {:sparql (str end-point "/sparql/")
   :update (str end-point "/update/")
   :data 	 (str end-point "/data/")})



(defn prefixed-query 
  "add prefixes to the sparql query "
  [query]
  (str (prefixes-string) "\n" query ))



(defn get 
  [end-point query]
  (with-open [client (http/create-client)]
    (let [res (http/GET client (:sparql end-point) 
                        :query {:query (prefixed-query query)
                                "soft-limit" 0}
                        :headers {:accept "application/sparql-results+json"})]
      (http/await res)
      {:status (:code (http/status res))
       :body (http/string res)})))
           

        
(defn post 
  [end-point query] 
  (with-open [client (http/create-client)]
    (let [res (http/POST client (:update end-point) 
                        :body {:update (prefixed-query query)}
                        :headers {:content-type "application/x-www-form-urlencoded"} :timeout -1)]
      (http/await res)
    	{:status (:code (http/status res)) 
       :body (http/string res)})))



(defn put 
  [end-point data graph] 
  (with-open [client (http/create-client)]
    (let [res (http/PUT client (str (:data end-point) graph)
                        :body data
                        :headers {:content-type "application/x-turtle"})]
    	(http/await res)
      {:status (:code (http/status res)) 
       :body (http/string res)})))



(defn put-file 
  [end-point file graph] 
  (let [ff (io/file file)]
    (put end-point ff graph)))




(defn delete 
  [end-point graph] 
  (with-open [client (http/create-client)]
    (let [res (http/DELETE client (str (:data end-point)graph))]
      (http/await res)
    	{:status (:code (http/status res)) 
       :body (http/string res)})))







; tests 

; define the 4store end-point
(def end-point (create-end-point "http://0.0.0.0:8009"))

; simple query 
; (print (get end-point "SELECT ?s ?p ?o WHERE {?s ?p ?o} LIMIT 500 "))

; simple insert 
; (println (post end-point "INSERT { <http://test.com/1> <http://test.com/p3> <http://test.com/1> }"))
; (println (put end-point "<http://test.com/1> <http://test.com/p4> <http://test.com/1> ." "http://sam.com"))
; (println (put-file end-point "/Users/ouvasam/Dropbox/ouvanous/projets/actifs/clojure/clj4store/src/clj4store/test.nt" "http://sam.com"))
; (println (delete end-point "http://sam.com"))















