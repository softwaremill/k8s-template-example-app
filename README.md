# An example app for k8s-template

Exposes an API endpoint:

```
curl http://localhost:8080/api/hello/john
```

Logs INFO and ERROR logs. ERRORs when the name is `adam`, INFOs for every other request:

```
curl http://localhost:8080/api/hello/adam
```

Exposes metrics, JVM + a custom request count:

```
curl http://localhost:8080/metrics
```

Exposes a status endpoint, which returns 200/503:

```
curl http://localhost:8080/status
curl -X POST http://localhost:8080/api/kill
```

Build as a docker image (using `sbt docker:publish`) as [softwaremill/k8s-template-example-app](https://hub.docker.com/r/softwaremill/k8s-template-example-app).