(ns om-tutorial.parsing-cards
  (:require-macros
    [cljs.test :refer [is]]
    [devcards.core :as dc]
    )
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [om-tutorial.parsing :as p]
            [om-tutorial.core :as core]
            [om-tutorial.ui :as ui]
            [devcards.core :as dc]
            ))

(let [env {:db-path []}
      env' (p/descend env :a)
      env'' (p/descend env' :b)
      path (fn [e] (:db-path e))]
  (dc/deftest descend-tests
              "The descend function tracks which node the parser is on in state by appending the given key to 
              :db-path in the given environment.
              
              The path starts out empty"
              (is (= (path env) []))
              "Adding an element works"
              (is (= (path env') [:a]))
              "Additional elements go on the end"
              (is (= (path env'') [:a :b]))
              ))

(dc/deftest parsing-utility-tests
            "follow-ref"
            (let [env {:state (atom {:db/id {1 {:db/id 1 :person/name "Joe"}}})}]
              (is (= {:db/id 1 :person/name "Joe"} (p/follow-ref env [:db/id 1]))))
            )

(let [initial-state {:last-error "Some Error" :new-person "Sally"
                     :widget     {
                                  :people
                                  [{:db/id 1 :person/name "Tony" :garbage 1 :person/mate {:db/id 2 :a 5 :person/name "Jane"}}
                                   {:db/id 2 :person/name "Jane" :garbage 2 :person/mate {:db/id 1 :b 2 :person/name "Tony"}}]
                                  }
                     }
      normalized-state (om/tree->db ui/Root initial-state true)
      env {:state (atom normalized-state)}]
  (dc/deftest local-read-tests
              "Will retrieve last-error"
              (is (=
                    {:last-error "Some Error"}
                    (core/parser env '[:last-error])
                    ))
              "Will retrieve new-person"
              (is (=
                    {:new-person "Sally"}
                    (core/parser env '[:new-person])
                    ))
              "Will retrieve recursive person"
              (is (=
                    {:widget {
                              :people
                              [{:db/id 1 :person/name "Tony" :person/mate {:db/id 2 :person/name "Jane"}}
                               {:db/id 2 :person/name "Jane" :person/mate {:db/id 1 :person/name "Tony"}}]
                              }}
                    (core/parser env '[{:widget [{:people [:db/id :person/name {:person/mate ...}]}]}])
                    ))
              ))
