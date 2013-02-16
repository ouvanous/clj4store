# clj4store

A simple 4store http client.

## Usage

```clojure
; return {:status "4store status code" :body "4store body message" }

; define the 4store end-point
(def end-point (create-end-point "http://0.0.0.0:8009"))

; sparql query 
(get end-point "SELECT ?s ?p ?o WHERE {?s ?p ?o} LIMIT 5 ") ; => {:status 200 :body {"head":{"vars":["s","p","o"]}, "results": { "bindings":[... }

; sparql update 1.1
(post end-point "INSERT { <http://test.com/1> <http://test.com/p1> <http://test.com/2> }") ; => {:status 200 :body "4store body message..." }

; replace data in graph
(put end-point "<http://test.com/2> <http://test.com/p1> <http://test.com/1> ." "http://mygraph.com") ; => {:status 201 :body "imported successfully ..." }

; replace data in graph by file data
(put-file end-point "/path/to/file.nt" "http://mygraph.com") ; => {:status 201 :body "imported successfully ..." }

; delete graph
(delete end-point "http://mygraph.com") ; => {:status 200 :body "200 deleted successfully ..." }

```
## License
Distributed under the Eclipse Public License, the same as Clojure.
