# Distributed-Bitcoin-Mining-System

See Readme.pdf for detailed description.

There are two separate parts of code in the project, server (boss) in the
ServerBitminer directory and client (worker) in LocalBitminer directory.
On the server-side, the inputs specify the number of leading 0s of the bitcoin
and the number of workers (independent of client) working with server. Once
having received the inputs, a master actor in the server will distribute the work
through a RoundRobinPool and supervise all the workers until one of them finds
the required bitcoin. Meanwhile, the server also listens on its hosting IP address
for any remote client to join if available. Once one of the workers, no matter
server or client side, finds a required bitcoin, the message is sent to the master
and every actor terminates.
On the client-side, it starts a local worker to look up the master actor on the
server-side by its hosting IP address and participate into mining.
