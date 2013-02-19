# clj4store

A simple 4store http client for clojure.

## Usage

```clojure
; return {:status "4store status code" :body "4store body message" }

; define the 4store end-point
(def end-point (create-end-point "http://0.0.0.0:8009"))

; define a sparql prefixes string
(def prefixes-str (sparql-prefixes base-prefixes {:ex "http://example.com/"}))
; => "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ..."

; sparql query without prefixes string 
(get end-point "SELECT ?s ?p ?o WHERE {?s ?p ?o} LIMIT 5 ") 
; => {:status 200 :body {"head":{"vars":["s","p","o"]}, "results": { "bindings":[... }

; sparql query with prefixes string 
(get end-point (prefixed-query prefixes-str "SELECT ?p ?o WHERE {ex:resource1 ?p ?o} LIMIT 5 "))
; => {:status 200 :body {"head":{"vars":[p","o"]}, "results": { "bindings":[... }

; sparql update 1.1
(post end-point (prefixed-query prefixes-str "INSERT { <http://test.com/1> <http://test.com/p1> <http://test.com/2> }")) 
; => {:status 200 :body "4store body message..." }

; replace data in graph
(put end-point "<http://test.com/2> <http://test.com/p1> <http://test.com/1> ." "http://mygraph.com") 
; => {:status 201 :body "imported successfully ..." }

; replace data in graph by file data
(put-file end-point "/path/to/file.nt" "http://mygraph.com") 
; => {:status 201 :body "imported successfully ..." }

; delete graph
(delete end-point "http://mygraph.com") 
; => {:status 200 :body "200 deleted successfully ..." }

```

## License
Distributed under the Eclipse Public License, the same as Clojure.
