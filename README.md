## Compiling program w/o gradle
```
./compile
```

## Compiling project w/ gradle
```
./gradle_compile
```

## Running project w/o gradle

#### Master
```
./server master
```

Note: 
1. For master you need to have a _master.properties_ file in 
_config_ folder in the directory where _server_ is triggered.
2. Directory where _server_ is triggered should have the _sqlite-jdbc_ folder


#### Replica
```
./server replica <master-ip>

    master-ip : IP Address of m/c where master is running
```

#### Client
```
./server client <master-ip>

    master-ip : IP Address of m/c where master is running
```

## Running project w/ gradle

#### Master
```
./server_gradle master
```

Note: 
1. For master you need to have a _master.properties_ file in 
_config_ folder in the directory where _server_ is triggered.

#### Replica
```
./server_gradle replica <master-ip>

    master-ip : IP Address of m/c where master is running
```

#### Client
```
./server_gradle client <master-ip>

    master-ip : IP Address of m/c where master is running
```
