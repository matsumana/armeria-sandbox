namespace java info.matsumana.armeria.thrift

service Hello3Service {
    Hello3Response hello(1:string name)
}

struct Hello3Response {
    1: string serverName,
    2: string message,
    3: Hello4Response hello4Response,
}

struct Hello4Response {
    1: string serverName,
    2: string message,
}
