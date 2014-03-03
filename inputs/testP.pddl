(define (problem problem-instance)
 (:domain numericInstance)
 (:objects
     o1 - dtype)
 (:init 
	(= (dToDest o1) 0)
	(= (dToSrc o1) 3)
	(= (toDeliver o1) 3)
	(= (fuel o1) 0)
	(= (inTruck o1) 0)
	(= (truckCapacity o1) 1)
 )
 (:goal (and (= (toDeliver o1) 0) ))

)