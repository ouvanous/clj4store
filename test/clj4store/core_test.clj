(ns clj4store.core-test
  (:use [clojure.test]
        [clojure.data.zip.xml]
        [clojure.java.shell :only [sh]])
  (:require [clj4store.core :as c]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.json :as json])
  )



(defn parse-xml-str [s]
    (zip/xml-zip (xml/parse (new org.xml.sax.InputSource
                            (new java.io.StringReader s)))))


(def end-point (c/create-end-point "http://0.0.0.0:8020"))
(def prefixes-str (c/sparql-prefixes c/base-prefixes {:ex "http://example.com/"}))


(defn get-json
  []
  (let [res (c/get end-point "SELECT ?s ?p ?o WHERE {?s ?p ?o} ")
        {{bindings "bindings"} "results"} (json/read-str (:body res))]
    {:status (:status res) :results bindings}))


(defn get-xml
  []
   (let [res (c/get end-point "SELECT ?s ?p ?o WHERE {?s ?p ?o} " :xml)
         x (parse-xml-str (:body res))
         results (xml-> x :results :result)]
    {:status (:status res) :results results}))


(defn empty-store 
  []
  (let [res (c/get end-point "
    SELECT ?g 
    WHERE {
      GRAPH ?g {
        OPTIONAL {
          ?s ?p ?o
        }
      }
    }")
        {{bindings "bindings"} "results"} (json/read-str (:body res))
        {{g "value"} "g"} (first bindings)
        res2 (c/delete end-point g)]
    {:status res2}))




;- ----------------------------------------------------------------------------
;- TESTS


(deftest end-point-test
  (testing "end-point URIs "
    (let [s0 (:sparql end-point)
          s1 (:update end-point)
          s2 (:data end-point)]
      (is (= s0 "http://0.0.0.0:8020/sparql/"))
      (is (= s1 "http://0.0.0.0:8020/update/"))
      (is (= s2 "http://0.0.0.0:8020/data/")))))



(deftest prefixes-test
  (testing "Prefixes"
    (let [p1 {:ex "http://example.com/"}
          p2 {:ex2 "http://example.com/2/"}
          sp1 (c/sparql-prefixes p1)
          spp (c/sparql-prefixes p1 p2)]
      (is (= sp1  "PREFIX ex: <http://example.com/> "))
      (is (= spp  "PREFIX ex2: <http://example.com/2/> \nPREFIX ex: <http://example.com/> ")))))


(deftest insert-query-test
  (testing "INSERT QUERY TEST "
    (empty-store)
    (testing "store should be empty"
      (let [res1 (get-json)
            res2 (get-xml)]
        (is (= 200 (:status res1)))
        (is (= 0 (count (:results res1))))
        (is (= 200 (:status res2)))
        (is (= 0 (count (:results res2))))))
    (testing "insert triple"
      (let [res (c/post end-point (c/prefixed-query prefixes-str "INSERT { GRAPH <http://mygraph.com> { ex:test1 ex:predicate1 ex:test2 }}"))]
        (is (= (:status res) 200))))
    (testing "store should contain 1 triple"
      (let [res1 (get-json)
            res2 (get-xml)]
        (is (= 200 (:status res1)))
        (is (= 1 (count (:results res1))))
        (is (= 200 (:status res2)))
        (is (= 1 (count (:results res2))))
        ))
    ))























