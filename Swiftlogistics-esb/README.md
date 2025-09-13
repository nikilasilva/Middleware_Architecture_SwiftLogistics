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

