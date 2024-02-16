This project spins up a dummy work updater in DDB with some default configurations

In the test configuration, it used DDBEmbedded as data store, and Cloud DDB in prod configuration.
Default configurations:
 1. Spins up 10 threads for updating WorkStatus for WorkItems in 1000 key spaces with each thread responsible for updating its own keyspace.
 2. WorkStatus can go from any state to any state,  PENDING, IN_PROGRESS, HOLD, COMPLETED, where the workItem value captures the state transistions.


Setting up credentials, use ada cli to get temperory aws credentials for running the project in non-test env.

The tool also provices a WorkItem Inspector, where you can enter any valid int from the KeySpace to inspect payload for the work item.

Execution command
./gradlew run

in case you get expired token, run the following commands
1. mwinit -o
2. ada credentials update, refer https://w.amazon.com/bin/view/DevAccount/Docs#HExamples
3. ./gradlew run


Pending items:
1. Validating log configurations, currently i am using Syso to generate console output as log files are not getting created.
