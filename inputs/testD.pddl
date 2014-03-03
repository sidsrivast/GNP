(define (domain numericInstance)
	(:requirements :typing :fluents)
	(:types dtype)
	(:functions
		(dToDest ?o - dtype)
		(dToSrc ?o - dtype)
		(toDeliver ?o - dtype)
		(fuel ?o - dtype)
		(inTruck ?o - dtype)
		(truckCapacity ?o - dtype))
 (:action A3
	:parameters (?o - dtype)
	:precondition (and (= (dToDest ?o) 0) (>= (inTruck ?o) 1) )
	:effect (and 
		(decrease (toDeliver ?o) 1)
		(decrease (inTruck ?o) 1)
		(increase (truckCapacity ?o) 1)
		)
 )

 (:action A0
	:parameters (?o - dtype)
	:precondition (and (= (dToSrc ?o) 0) (>= (toDeliver ?o) 1) (>= (truckCapacity ?o) 1) )
	:effect (and 
		(decrease (truckCapacity ?o) 1)
		(increase (inTruck ?o) 1)
		)
 )

 (:action A2
	:parameters (?o - dtype)
	:precondition (and (= (fuel ?o) 0) )
	:effect (and 
		(increase (fuel ?o) 1)
		)
 )

 (:action A4
	:parameters (?o - dtype)
	:precondition (and (>= (dToSrc ?o) 1) )
	:effect (and 
		(decrease (dToSrc ?o) 1)
		(decrease (fuel ?o) 1)
		(increase (dToDest ?o) 1)
		)
 )

 (:action A1
	:parameters (?o - dtype)
	:precondition (and (>= (dToDest ?o) 1) (>= (fuel ?o) 1) )
	:effect (and 
		(decrease (dToDest ?o) 1)
		(decrease (fuel ?o) 1)
		(increase (dToSrc ?o) 1)
		)
 )

)
