# test-akka-integration

This is a testbed to try and realise use of actors to contain our optimisation models.

The data is distributed across a number of services. I think this makes perfect sense and fits well with the DDD approach.

However, I need to create routing "Models" which are effectively calculation/optimisation/validation engines that receive data from external events (e.g. a driver arrives at a customer for a delivery) or from user interaction "put this load on this vehicle". At any point there could be multiple models floating around the system - corresponding to different users and/or physical operations.

A model consists of a lot of data drawn from the various entities. It is non-persistent. A model can be recreated by fetching the associated data from elsewhere.

A model must be unique within the system - so if Lagom/external cluster manager decides we need 2 or more Model services running on different nodes at any one time they should not be creating 2 instances.

The whole reactive process flow suggests that the model should directly access various lagom services to obtain and send data as well as receiving inputs directly from it's "owner" service(s)

The computationally intensive nature of the model suggests that parallel operations should be carried out by the model and knowing Actors (non-clustered) reasonably well suggests to me that an actor based implementation would be great.

So all the above leads me to think that I could do it by making my models either Cluster Singletons or use Cluster Sharding for them. And I'd also need to pass into the actors the required services for them to obtain/update data.

So to my problem: I really don't know enough about Akka Clustering or how or what is going on with it in Lagom to figure out how to create a Cluster Sharding / Singleton actor that has one or more services passed into it.

The Lagom guys have given me some pointers (code in com.lightbend.lagom.interna.persistennce.cluster.ClusterDistribution.scala) but it's completely incomprehensible to me with my current knowledge level, and I have no idea how I would wire the services into the props in any case.

So I think what I need is a version of my really simple code example that does it by cluster sharding. What that code does currently is the service creates a local actor with the target service passed in directly through the props. It's a modified subset of the Hello lagom start-up example code.

# In this project
The persistent service is a substitute for one or more real services that will hold the different entities. At the moment it is a slightly modified version of the g8 lagom hello world example.

The akka-model is intended to set up different ways of using actors as described above. So far the simple-model creates a local actor per person and caches the greetings message while also checking it begins with h before letting the message through to the persistent entity.

What I want is an alternate version of simple-service that uses a Cluster Shareded actor instead of the current locally defined one. It needs the persistent service still so it should be an implementation of the base ModelActor trait.

This will be sufficient for me to figure out where everything else is going to go.

tests all work.
If you run the service with runAll, then the sample curl script in ./scripts will change message for user Lutz. Tested and working



