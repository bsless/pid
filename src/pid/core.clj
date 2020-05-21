(ns pid.core)

(defprotocol Stepper
  (-step [this in]))

(defprotocol Controller
  (-state [this])
  (-target [this])
  (-error [this]))

(deftype PController
    [target
     ^:unsynchronized-mutable state
     ^:unsynchronized-mutable error
     p]
  Stepper
  (-step [this in]
    (let [err (- target in)
          out (* err p)]
      (set! state out)
      (set! error err)
      out))
  Controller
  (-state [this] state)
  (-target [this] target)
  (-error [this] error)
  Object
  (toString [this]
    (str "#controller " {:p p})))

(defn p-controller
  [target p]
  (->PController target 0 0 p))

(deftype PIController
    [target
     ^:unsynchronized-mutable state
     ^:unsynchronized-mutable error
     ^:unsynchronized-mutable integral
     dt
     p
     i]
  Stepper
  (-step [this in]
    (let [err (- target in)
          integral' (+ integral (* err dt))
          out (+ (* err p) (* integral' i))]
      (set! error err)
      (set! integral integral')
      (set! state out)
      out))
  Controller
  (-state [this] state)
  (-target [this] target)
  (-error [this] error)
  Object
  (toString [this]
    (str "#controller " {:p p :i i :dt dt})))

(defn pi-controller
  [target dt p i]
  (->PIController target 0 0 0 dt p i))

(deftype PDController
    [target
     ^:unsynchronized-mutable state
     ^:unsynchronized-mutable error
     dt
     p
     d]
  Stepper
  (-step [this in]
    (let [err (- target in)
          derivative (/ (- err error) dt)
          out (+ (* p err) (* d derivative))]
      (set! error err)
      (set! state out)
      out))
  Controller
  (-state [this] state)
  (-target [this] target)
  (-error [this] error)
  Object
  (toString [this]
    (str "#controller " {:p p :d d :dt dt})))

(defn pd-controller
  [target dt p d]
  (->PDController target 0 0 dt p d))

(deftype PIDController
    [target
     ^:unsynchronized-mutable state
     ^:unsynchronized-mutable error
     ^:unsynchronized-mutable integral
     dt
     p
     i
     d]
  Stepper
  (-step [this in]
    (let [err (- target in)
          integral' (+ integral (* err dt))
          derivative (/ (- err error) dt)
          out (+ (* p err) (* i integral') (* d derivative))]
      (set! error err)
      (set! integral integral')
      (set! state out)
      out))
  Controller
  (-state [this] state)
  (-target [this] target)
  (-error [this] error)
  Object
  (toString [this]
    (str "#controller " {:p p :i i :d d :dt dt})))

(defn pid-controller
  [target dt p i d]
  (->PIDController target 0 0 0 dt p i d))

(defn simulate
  [c sys tolerance steps start]
  (let [log (transient [])]
    (loop [step 0
           in start]
      (-step c in)
      (cond
        (and (< step steps)
             (< tolerance (Math/abs (-error c))))
        (do
          (conj! log {:in in :out (-state c)})
          (recur (inc step) (sys (-state c))))
        (= step steps) (println 'exhausted)
        (< (Math/abs (- (-target c) (-state c))) tolerance)
        (println 'controlled 'in step 'steps)))
    (persistent! log)))

(comment
  (def c (pi-controller 0.5 0.01 0.2 0.1))
  (def log (simulate c identity 0.001 10000 1))
  (def c (pid-controller 0.5 0.01 0.1 0.1 0.004))
  (def log (simulate c identity 0.001 10000 1))
  (last log)
  )
