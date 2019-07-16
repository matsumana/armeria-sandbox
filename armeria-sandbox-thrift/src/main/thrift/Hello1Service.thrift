namespace java info.matsumana.armeria.thrift

service Hello1Service {
    Hello1Response hello(1:string name)
}

struct Hello1Response {
    1: string serverName,
    2: string message,
}
