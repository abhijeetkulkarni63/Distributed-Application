Assuming you are in the directory containing this README:

## To clean:
make clean
-------------------------------------------------------------------------------
## To compile:
make
-------------------------------------------------------------------------------
## To run:
make run
-------------------------------------------------------------------------------

Implementation Description:

Used java to implement this project.
After server is started it will print out its namd and available port number
for client to send requests.
Upon receiving a request for resource; server would accept the request by
creating a socket. Server would then check if requested resource is present
in 'www' directory or not. If the requested resource is present in 'www'
directory then server would prepare a '200 OK' header and then send the header
along with contents of resource. If the resource is not present in 'www'
directory then sever would send out '404 pag not found' response.
-------------------------------------------------------------------------------

Sample Input/Output:

remote05:~/DS/akulka16-p1/MultiThreadedHttpServer> make run
ant -buildfile src/build.xml run
Buildfile: /import/linux/home1/akulka16/DS/akulka16-p1/MultiThreadedHttpServer/src/build.xml

jar:
      [jar] Building jar: /import/linux/home1/akulka16/DS/akulka16-p1/MultiThreadedHttpServer/Server.jar

run:
     [java] Host Name: remote05
     [java] Port Number: 1026
--------------------------------------------------------------------------------------------------------------
Client Input

remote04:~> wget http://remote05.cs.binghamton.edu:1026/test.html               
--2017-09-19 22:56:56--  http://remote05.cs.binghamton.edu:1026/test.html
Resolving remote05.cs.binghamton.edu (remote05.cs.binghamton.edu)... 128.226.180.167
Connecting to remote05.cs.binghamton.edu (remote05.cs.binghamton.edu)|128.226.180.167|:1026... connected.
HTTP request sent, awaiting response... 200 OK
Length: 102 [text/html]
Saving to: ‘test.html’

test.html           100%[===================>]     102  --.-KB/s    in 0s

2017-09-19 22:56:56 (494 KB/s) - ‘test.html’ saved [102/102]

---------------------------------------------------------------------------------------------------------------
Server Output

     [java] Host Name: remote05
     [java] Port Number: 1026
     [java] test.html|128.226.180.166|53070|1

---------------------------------------------------------------------------------------------------------------
Client invalid input

remote04:~> wget http://remote05.cs.binghamton.edu:1026/test4.html
--2017-09-19 22:54:08--  http://remote05.cs.binghamton.edu:1026/test4.html
Resolving remote05.cs.binghamton.edu (remote05.cs.binghamton.edu)... 128.226.180.167
Connecting to remote05.cs.binghamton.edu (remote05.cs.binghamton.edu)|128.226.180.167|:1026... connected.
HTTP request sent, awaiting response... 404 Not Found
2017-09-19 22:54:08 ERROR 404: Not Found.
