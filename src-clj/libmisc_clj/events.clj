(ns libmisc-clj.events
  (:import [java.io Serializable]))

(defprotocol GenericEventProtocol
  (getName [this])
  (getTimestamp [this])
  (getType [this])
  (getLabel [this])
  (getPayload [this]))

(deftype GenericEvent [^String name
                       ^Long ts
                       ^String type
                       ^String label
                       payload]
  Serializable
  GenericEventProtocol
  (getName [_] name)
  (getTimestamp [_] ts)
  (getType [_] type)
  (getLabel [_] label)
  (getPayload [_] payload)
  Object
  (toString [_]
    (str "{:name " name
         " :ts" ts
         " :type " type
         " :label" label
         " :payload" payload
         "}")))
