# Distributed Systems Project-2: Roucairol and Carvalho's Distributed Mutual Exclusion Protocol

The nodes are setup on the UTD's Computer Science Department cluster.

## Frequently used Linux commands
#### SSH into control node from MobaXterm:
```
ssh sxs210570@csjaws.utdallas.edu
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
ssh sxs210570@dc01.utdallas.edu
```

#### "cd" into the project repository in the terminal
*Note: Doing this because the autofill on the csjaws machine is hanging up*

```
cd distributed-systems-project-2
```
## References
- [Cloning Private Repository from Github Using Personal Token](https://www.youtube.com/watch?v=rzgtnS04MXE)
  - Refer this but use 'fine grained access' feature that is provided in GitHub developer settings.