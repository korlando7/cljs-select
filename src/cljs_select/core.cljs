(ns cljs-select.core
  (:require
   [reagent.core :as r]
   [clojure.string :refer [starts-with?]]))

(defn scroll [ref n]
  (if (zero? n)
    (set! (-> ref .-scrollTop) 0)
    (set! (-> ref .-scrollTop) (+ (-> ref .-scrollTop) n))))

(defn update-active-idx [key-code active items menu-ref]
  (let [item (-> @menu-ref .-firstElementChild)
        height (-> item (.getBoundingClientRect) .-height)]
    (cond
      ; go up
      (= 38 key-code) (cond
                        (pos? @active) (do (reset! active (dec @active))
                                           (scroll @menu-ref (* -1 height)))
                        (zero? @active) (do (reset! active nil)
                                            (scroll @menu-ref 0)))
      ; go down
      (= 40 key-code) (cond
                        (nil? @active) (reset! active 0)
                        (< @active (dec (count items)))
                        (do (reset! active (inc @active))
                            (scroll @menu-ref height))))))

(defn input
  [{:keys [value open? on-click on-change on-key-down on-key-press]}]
  [:div.custom-select__input
   [:input.custom-select__value
    {:value value
     :on-click on-click
     :on-change on-change
     :on-key-down on-key-down
     :on-key-press on-key-press}]
   [:i.fas.custom-select__icon
    {:on-click #(swap! open? not)
     :class (str "fa-chevron-" (if @open? "up" "down"))}]])

(defn menu
  [{:keys [open? ref menu-ref items active on-click on-close]}]
  (when open?
    (r/with-let
      [click #(when-not (.contains @ref (.-target %)) (on-close))
       _ (js/document.addEventListener "click" click false)]
      [:div.custom-select__menu
       {:ref #(reset! menu-ref %)}
       (if (pos? (count items))
         (doall
          (for [[idx item] (map-indexed vector items)]
            ^{:key idx}
            [:div.custom-select__item
             {:class (when (= idx active)
                       "custom-select__item--active")
              :on-click #(do (on-click (-> % .-target .-innerText))
                             (on-close))}
             item]))
         [:div.custom-select__item "No matches found"])]
      (finally
        (js/document.removeEventListener "click" click false)))))

(defn custom-select [props]
  (let [open? (r/atom false)
        ref (r/atom nil)
        menu-ref (r/atom nil)
        active-idx (r/atom nil)]
    (fn [{:keys [value on-click items error]}]
      (let [items' (if value (filter #(starts-with? % value) items) items)
            on-close #(do (reset! open? false) (reset! active-idx nil))
            open-menu #(reset! open? true)]
        [:div.custom-select
         {:ref #(reset! ref %)}
         [:div.custom-select__select
          [input {:value value
                  :open? open?
                  :on-click open-menu
                  :on-change  #(do
                                 (on-click (-> % .-target .-value))
                                 (open-menu))
                  :on-key-down #(update-active-idx
                                 (-> % .-keyCode) active-idx items' menu-ref)
                  :on-key-press #(when-not (nil? @active-idx)
                                   (when (= "Enter" (-> % .-key))
                                     (do (on-click (nth items' @active-idx))
                                         (on-close))))}]
          [menu {:open? @open?
                 :ref ref
                 :menu-ref menu-ref
                 :items items'
                 :active @active-idx
                 :on-click on-click
                 :on-close on-close}]]
         (when (and error (not @open?)) [:div.custom-select__error error])]))))

(defn home-page []
  [:div [:h2 "Welcome to Reagent"]
   [custom-select]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
