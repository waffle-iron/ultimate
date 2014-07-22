(set-option :produce-models true)
(set-logic QF_UFLIRA)
(declare-fun f (Int) Real)
(declare-fun a () Int)
(declare-fun b () Int)
(declare-fun c () Int)
(declare-fun x () Real)
(declare-fun y () Real)
(assert (= x (f a)))
(assert (= (f b) (+ (to_real c) (/ 1.0 2.0))))
(assert (= a b))
(assert (= (to_real a) x))
(check-sat)
(get-model)
(exit)
