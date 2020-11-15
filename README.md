# Fuse QE Camel Netty Proxy project

This is a camel proxy route for integration test purposes against Fuse products.

It could be used as service with different specified requisites:
       
      - Fuse on Openshift (let's start with spring-boot)
      - local service (on the same OCP)
      - external service (on a different OCP)
      - http
      - https

## How install on Openshift 4.x 

First of all let create a specific namespace: 

`oc new-project fuse-integration`

Then install and run:

`oc new-app https://github.com/smongiar/camel-netty.git#master --strategy=docker`

If we want to expose externally, let create routes for caml-netty service:

`oc expose svc/camel-netty`

## How install locally

Local build:

`mvn clean package`

Then, run process as:

`java -jar target/fuse-camel-proxy-com.redhat.camelProxy.jar` 

Run as image in a docker container:

Build image:
`docker build -t camel-netty-proxy .`

Run with http proxy mapping:

`docker run -d --rm -p 8080:8080 camel-netty-proxy`

Run with https proxy mapping:

`docker run -d --rm -p 8443:8443 camel-netty-proxy`

Debug and logging:

`docker logs <container_id>`


## Test services

HTTP proxy test:

`curl http://md5.jsontest.com/?text=example_text -x localhost:8080`

HTTPS proxy test:

`curl https://api.joind.in -x https://localhost:8443` (import valid CA certificate from server)

External service in OCP

