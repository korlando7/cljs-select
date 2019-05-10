(ns cljs-select.prod
  (:require
    [cljs-select.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
