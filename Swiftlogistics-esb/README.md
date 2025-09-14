# SwiftLogistics ESB Starter

This project is a Spring Boot + Apache Camel ESB prototype that integrates with the Python mock services:

- CMS (SOAP) at http://localhost:5001
- ROS (REST) at http://localhost:5002
- WMS (TCP) at port 5003

Build:
mvn clean package

Run:
java -jar target/esb-starter-0.0.1-SNAPSHOT.jar

Or with Docker compose:
docker-compose up --build

Theesh Testing using curl
Method 3 :
curl -X GET "http://localhost:8084/orders/ORD001/status" \
 -H "Content-Type: application/json"

output :
{"orderId":"ORD001","cmsStatus":"processing","routeStatus":"route_not_found","packageStatus":"in_warehouse","timestamp":1757702152846}

Method 4 :

# Test the update endpoint

curl -X PUT "http://localhost:8084/orders/ORD001/status?status=shipped" \
 -H "Content-Type: application/json"

output :
{"rosUpdate":"ROS route for order ORD001 updated to shipped successfully","newStatus":"shipped","orderId":"ORD001","success":true,"wmsUpdate":"Unknown package update response type: 8","cmsUpdate":"CMS order ORD001 status updated to shipped successfully (mock response)","timestamp":1757735485904}

Method 5 :
$ curl -X GET "http://localhost:8084/health" -H "Content-Type: application/json" -v

Note: Unnecessary use of -X or --request,
Note: GET is already inferred.

- Host localhost:8084 was resolved.
- IPv6: ::1
- IPv4: 127.0.0.1
- Trying [::1]:8084...
- Connected to localhost (::1) port 8084
- using HTTP/1.x
  > GET /health HTTP/1.1
  > Host: localhost:8084
  > User-Agent: curl/8.15.0
  > Accept: _/_
  > Content-Type: application/json
- Request completely sent off
  < HTTP/1.1 200
  < Content-Type: application/json
  < Transfer-Encoding: chunked
  < Date: Sun, 14 Sep 2025 03:47:33 GMT
  
  {"ros":{"status":"UP"},"cms":{"status":"DOWN"},"overall":"DOWN","wms":{"status":"UP"},"timestamp":1757821652999}\* Connection #0 to host localhost left intact
