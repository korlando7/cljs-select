(ns ^:figwheel-no-load cljs-select.dev
  (:require
    [cljs-select.core :as core]
    [devtools.core :as devtools]))


(enable-console-print!)

(devtools/install!)

(core/init!)
