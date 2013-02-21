(ns clj4store.core
  (:use [clojure.string :only (join)])
  (:require [http.async.client :as http]
            [clojure.java.io :as io]))







;- ----------------------------------------------------------------------------
;- SPARQL MIME-TYPES enum
 
(def sparql-mime-types {:json "application/sparql-results+json" 
                           :xml "application/sparql-results+xml" ; when using CONSTRUCT 4store will return XML too even if this type is not valid
                           :rdf-xml "application/rdf+xml" ; used to put rdf xml data
                           :turtle "text/turtle" ; used only for construct / describe 
                           })





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
; ;- HTTP METHODS



(defn prefixed-query 
  "add a prefixes string to the sparql query"
  [prefixes query]
  (str prefixes "\n" query ))



(defn get 
  "run sparql query and return {:status statusCode :body content}
  statusCode: http status code retruns by 4store
  content: body return by 4store (json, xml, text, ...)"
  ([end-point query] (get end-point query :json))
  ([end-point query mime-type]
  (with-open [client (http/create-client)]
    (let [res (http/GET client (str end-point "/sparql/") 
                        :query {:query query}
                        :headers {:accept (mime-type sparql-mime-types)})]
      (http/await res)
      {:status (:code (http/status res))
       :body (http/string res)}))))
           

        
(defn post 
  "run a sparql update and return {:status statusCode :body content}"
  [end-point query] 
  (with-open [client (http/create-client)]
    (let [res (http/POST client (str end-point "/update/") 
                        :body {:update query}
                        :headers {:mime-type "application/x-www-form-urlencoded"})]
      (http/await res)
    	{:status (:code (http/status res)) 
       :body (http/string res)})))



(defn put 
  "replace data"
  ([end-point data graph] (put end-point data graph :turtle))
  ([end-point data graph mime-type] 
  (with-open [client (http/create-client)]
    (let [res (http/PUT client (str end-point "" graph)
                        :body data
                        :headers {:content-type (mime-type sparql-mime-types)})]
    	(http/await res)
      {:status (:code (http/status res)) 
       :body (http/string res)}))))



(defn put-file 
  "replace data by file data"
  ([end-point file graph] put-file end-point file graph :turtle)
  ([end-point file graph mime-type] 
  (let [ff (io/file file)]
    (put end-point ff graph))))



(defn delete 
  "delete graph"
  [end-point graph] 
  (with-open [client (http/create-client)]
    (let [res (http/DELETE client (str end-point "/data/" graph))]
      (http/await res)
    	{:status (:code (http/status res)) 
       :body (http/string res)})))







;- ----------------------------------------------------------------------------
;- EXAMPLES

; define the 4store end-point
; (def end-point "http://0.0.0.0:8020")

; define a sparql prefixes string
; (def prefixes-str (sparql-prefixes base-prefixes {:ex "http://test.com"}))

; sparql query without prefixes
; (get end-point "SELECT ?s ?p ?o WHERE  {?s ?p ?o} LIMIT 10")  ; => {:status 200 :body {"head":{"vars":["s","p","o"]}, "results": { "bindings":[... }

; sparql query with prefixes
; (get end-point (prefixed-query prefixes-str "SELECT ?s ?p ?o WHERE {?s ?p ?o} LIMIT 5 ")) ; => {:status 200 :body {"head":{"vars":["s","p","o"]}, "results": { "bindings":[... }

; sparql update 1.1
; (post end-point (prefixed-query prefixes-str "INSERT { ex:test1 <http://test.com/p1> <http://test.com/2> }")) ; => {:status 200 :body "4store body message..." }

; replace turtle/ntriples data in graph
; (put end-point "<http://test.com/1> <http://test.com/p1> <http://test.com/2> ." "http://mygraph.com") ; => {:status 201 :body "imported successfully ..." }

; replace data in graph by file data
; (put-file end-point "/path/to/file.nt" "http://mygraph.com") ; => {:status 201 :body "imported successfully ..." }

; delete graph
; (delete end-point "http://mygraph.com") ; => {:status 200 :body "200 deleted successfully ..." }















