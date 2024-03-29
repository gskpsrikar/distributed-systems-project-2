# Distributed Systems Project-2: Roucairol and Carvalho's Distributed Mutual Exclusion Protocol
This project implements a distributed system consisting of n nodes, numbered 0 to n − 1, arranged in an arbitrary topology. The topology and information about other parameters are provided in a configuration file.

The nodes are setup on the UTD's Computer Science Department cluster.

## Workflow
- ```Node.java``` contains (but not limited to) the following methods.
  - runApplication(): Loop(while) until all the CS requests are satisfied. The csEnter() of Mutex service is called here.

- ```MutualExclusion.java``` contains (but not limited to) the following details.
   - A Queue to handle requests.
   - csEnter(): Send key-requests to all required processes. Then wait until all keys are received.
   - csLeave(): 
   - receiveReply(): Accepts rept messages

- ```Main.java``` - This is the file we enter using the launcher script.
  - In *public static void main* method, create the following objects - Node (node), MutualExclusion (mutex)
  - In a new thread, create a Server (to lister to messages) and pass the mutex object to it.
  - In a new thread, call node.runApplication(mutex)

- ```Server.java```
  - Runs on a separate thread.
  - Handle incoming messages. Requires the mutex object. Send received reply to mutex.receiveReply().


## Roucairol-Carvalho Protocol
Pi on receiving a critical section REQUEST message from Pj with timestamp Cj.
- If there is no outstanding request at Pi, REPLY to Pj.
- If Pi is in critical section, add to queue Pj's request to Pi's queue.
- If Pi has an outstanding request and Pi is not in CS:
   - If Ci < Cj, then add to queue (defer).
   - Else, REPLY to Pj. Then, send REQUEST to Pj.

Pi on receiving a critical section REPLY message from Pj.
- If all keys from Pi's neighbors are present with Pi, then enter critical section.


Pi on csExit(), do:
- If there is another process request on top of queue, send REPLY.
  - While doing this, if there is an outstanding request send a REQUEST message too.

## Output File format
<pid, csRequestTime, csEnterTime, csExitTime, messageCount>


## Frequently used Linux commands
#### SSH into control node from MobaXterm:
```
ssh xxxxxxxxx@csjaws.utdallas.edu
```
(Use PuTTy if you are doing it from Windows)


#### Cloning git repository
- Go to 'Settings->Developer Settings->Personal access tokens->Fine-grained tokens'
- Generate a READ-ONLY access key and give permission to this repository.
- Copy the token in the command below and clone the repository.

```
git clone https://xxx_ACCESS_KEY_HERE_xxx@github.com/gskpsrikar/distributed-systems-project-2.git
```

#### SSH into dcxx machines
```
ssh xxxxxxxxx@dc01.utdallas.edu
```

#### "cd" into the project repository in the terminal
*Note: Doing this because the autofill on the csjaws machine is hanging up*

```
cd distributed-systems-project-2
```
#### SCP
```
scp -r ./distributed-systems-project-2/ xxxxxxxxx@csjaws.utdallas.edu:.
```
#### Testing
```
- cd OneDrive/Documents/Coursework/Fall2023/CS6378 Advanced Operating Systems/
- conda activate py310-nlp
- jupyter notebook
```
## References
- [Cloning Private Repository from Github Using Personal Token](https://www.youtube.com/watch?v=rzgtnS04MXE)
  - Refer this but use 'fine grained access' feature that is provided in GitHub developer settings.
