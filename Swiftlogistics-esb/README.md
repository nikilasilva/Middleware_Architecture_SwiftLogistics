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
