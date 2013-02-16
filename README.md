# clj4store

A simple 4store http client.


## Usage
```clojure
; define the 4store end-point
(def end-point (create-end-point "http://0.0.0.0:8009"))

; simple query 
; (print (get end-point "SELECT ?s ?p ?o WHERE {?s ?p ?o} LIMIT 500 "))
```
## License
Distributed under the Eclipse Public License, the same as Clojure.
