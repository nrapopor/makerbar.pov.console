(ns makerbar.pov.game
  (:import [java.awt.event KeyEvent])
  (:require [makerbar.pov.game.stage :as stage]
            [makerbar.pov.mode :as mode :refer (UiMode)]
            [makerbar.pov.state :as s]
            [makerbar.pov.ui.processing :as p]
            [makerbar.pov.ui.draw :as d]))

(defn jaeger
  "Returns true only if all target buttons are pressed on all ddr controllers.
  See: Pacific Rim movie."
  [{:keys [ddr-a ddr-b]} button-or-buttons]
  (let [buttons (if (seq? button-or-buttons)
                  button-or-buttons
                  [button-or-buttons])]
    (reduce #(and %1 %2)
            (concat (map #(get ddr-a %) buttons)
                    (map #(get ddr-b %) buttons)))))

(def button-coords
  {:north-west [-1 -1]
   :north      [0 -1]
   :north-east [1 -1]

   :west       [-1 0]
   :east       [1 0]

   :south-west [-1 1]
   :south      [0 1]
   :south-east [1 1]})

(defn rand-pattern
  "Returns a seq of random button ids. The length of the seq will at least 1 and at most num-buttons, but may be some number in between."
  [num-buttons]
  (let [button-ids (vec (keys button-coords))]
    (distinct
      (for [i (range num-buttons)]
        (get button-ids (rand-int 8))))))

(def arrow-angles
  {:north-west (* p/TAU -0.125)
   :north      0
   :north-east (* p/TAU 0.125)

   :west       (* p/TAU -0.25)
   :east       (* p/TAU 0.25)

   :south-west (* p/TAU -0.375)
   :south      (* p/TAU 0.5)
   :south-east (* p/TAU 0.375)})

(defn draw-arrow [button-id]
  (p/with-matrix
    (p/rotate (get arrow-angles button-id))
    (p/shape [-5 10] [-5 0] [-10 0] [0 -10] [10 0] [5 0] [5 10])))

(def button-size 20)
(def button-spacing button-size)

(defn draw-buttons
  [buttons]
  (doseq [b buttons
          :let [[dx dy] (get button-coords b)]]
    (p/with-matrix
      (p/translate (* dx button-spacing) (* dy button-spacing))
      (draw-arrow b))))

(defn draw-button-border []
  (let [s (+ button-spacing (/ button-size 2) 2)]
    (p/rect (- s) (- s) (* 2 s) (* 2 s))))

;; Stages

(def game-state (atom nil))

(def initial-stage
  (reify UiMode

    (init [_]
      (reset! game-state {:ddr-a          {:feets 2
                                           :score 0}
                          :ddr-b          {:feets 2
                                           :score 0}
                          :target-pattern (rand-pattern 2)}))

    (draw [_]
      ; clear
      (p/background 0)

      (p/with-style
        (p/stroke 255 0 0)
        (p/text (pr-str (:target-pattern @game-state)) 0 20))

      (p/with-matrix
        (p/translate [(/ 224 2) (/ 102 2)])

        ; draw ddr A buttons
        (p/with-style
          (p/fill 0 0 255 128)
          (draw-buttons (map first (filter second (get-in @game-state [:ddr-a :buttons])))))

        ; draw ddr B buttons
        (p/with-style
          (p/fill 255 0 0 128)
          (draw-buttons (map first (filter second (get-in @game-state [:ddr-b :buttons])))))

        ; draw target pattern
        (p/with-style
          (p/stroke 255)
          (p/no-fill)
          (draw-buttons (get-in @game-state [:target-pattern]))
          (draw-button-border))))

    (ddr-button-pressed [_ evt]
      (when (jaeger evt :north) (s/inc-pov-offset [0 -1]))
      (when (jaeger evt :south) (s/inc-pov-offset [0 1]))
      (when (jaeger evt :east) (s/inc-pov-offset [1 0]))
      (when (jaeger evt :west) (s/inc-pov-offset [-1 0]))
      (when (jaeger evt :north-west) (s/inc-img-scale 1))
      (when (jaeger evt :north-eats) (s/inc-img-scale -1)))

    (key-pressed [_ event]
      (condp = (.getKeyCode event)

        ;; DDR A
        KeyEvent/VK_Q (swap! game-state assoc-in [:ddr-a :buttons :north-west] true)
        KeyEvent/VK_W (swap! game-state assoc-in [:ddr-a :buttons :north] true)
        KeyEvent/VK_E (swap! game-state assoc-in [:ddr-a :buttons :north-east] true)
        KeyEvent/VK_A (swap! game-state assoc-in [:ddr-a :buttons :west] true)
        KeyEvent/VK_D (swap! game-state assoc-in [:ddr-a :buttons :east] true)
        KeyEvent/VK_Z (swap! game-state assoc-in [:ddr-a :buttons :south-west] true)
        KeyEvent/VK_X (swap! game-state assoc-in [:ddr-a :buttons :south] true)
        KeyEvent/VK_C (swap! game-state assoc-in [:ddr-a :buttons :south-east] true)

        ; DDR B
        KeyEvent/VK_I (swap! game-state assoc-in [:ddr-b :buttons :north-west] true)
        KeyEvent/VK_O (swap! game-state assoc-in [:ddr-b :buttons :north] true)
        KeyEvent/VK_P (swap! game-state assoc-in [:ddr-b :buttons :north-east] true)
        KeyEvent/VK_K (swap! game-state assoc-in [:ddr-b :buttons :west] true)
        KeyEvent/VK_SEMICOLON (swap! game-state assoc-in [:ddr-b :buttons :east] true)
        KeyEvent/VK_COMMA (swap! game-state assoc-in [:ddr-b :buttons :south-west] true)
        KeyEvent/VK_PERIOD (swap! game-state assoc-in [:ddr-b :buttons :south] true)
        KeyEvent/VK_SLASH (swap! game-state assoc-in [:ddr-b :buttons :south-east] true)

        KeyEvent/VK_SPACE (swap! game-state assoc-in [:target-pattern] (rand-pattern 2))

        nil))

    (key-released [_ event]
      (condp = (.getKeyCode event)

        ;; DDR A
        KeyEvent/VK_Q (swap! game-state assoc-in [:ddr-a :buttons :north-west] false)
        KeyEvent/VK_W (swap! game-state assoc-in [:ddr-a :buttons :north] false)
        KeyEvent/VK_E (swap! game-state assoc-in [:ddr-a :buttons :north-east] false)
        KeyEvent/VK_A (swap! game-state assoc-in [:ddr-a :buttons :west] false)
        KeyEvent/VK_D (swap! game-state assoc-in [:ddr-a :buttons :east] false)
        KeyEvent/VK_Z (swap! game-state assoc-in [:ddr-a :buttons :south-west] false)
        KeyEvent/VK_X (swap! game-state assoc-in [:ddr-a :buttons :south] false)
        KeyEvent/VK_C (swap! game-state assoc-in [:ddr-a :buttons :south-east] false)

        ; DDR B
        KeyEvent/VK_I (swap! game-state assoc-in [:ddr-b :buttons :north-west] false)
        KeyEvent/VK_O (swap! game-state assoc-in [:ddr-b :buttons :north] false)
        KeyEvent/VK_P (swap! game-state assoc-in [:ddr-b :buttons :north-east] false)
        KeyEvent/VK_K (swap! game-state assoc-in [:ddr-b :buttons :west] false)
        KeyEvent/VK_SEMICOLON (swap! game-state assoc-in [:ddr-b :buttons :east] false)
        KeyEvent/VK_COMMA (swap! game-state assoc-in [:ddr-b :buttons :south-west] false)
        KeyEvent/VK_PERIOD (swap! game-state assoc-in [:ddr-b :buttons :south] false)
        KeyEvent/VK_SLASH (swap! game-state assoc-in [:ddr-b :buttons :south-east] false)

        nil))))

;; Mode

(defn mode []
  (reify UiMode

    (init [_] (stage/set-stage! initial-stage))

    (draw [_]
      ; clear
      (p/background 0)

      (d/pov-view #(mode/draw @stage/game-stage)))

    (key-pressed [_ evt] (mode/key-pressed @stage/game-stage evt))
    (key-released [_ evt] (mode/key-released @stage/game-stage evt))

    (ddr-button-pressed [_ evt] (mode/ddr-button-pressed @stage/game-stage evt))))
