package com.swiftlogistics.esb.route;

import com.swiftlogistics.esb.processor.CmsSoapToCanonicalProcessor;
import com.swiftlogistics.esb.processor.EnrichmentProcessor;
import com.swiftlogistics.esb.processor.ValidationProcessor;
import com.swiftlogistics.esb.processor.WmsTcpParserProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class EsbRoutes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        
        // Main integration route
        from("rabbitmq:esb.orders.incoming")
            .routeId("main-integration-route")
            .log("Processing incoming order: ${body}")
            .process(new ValidationProcessor())
            .choice()
                .when(header("valid").isEqualTo(true))
                    .to("direct:processOrder")
                .otherwise()
                    .log("Invalid order: ${header.validation.error}")
                    .to("rabbitmq:esb.orders.failed")
            .end();

        // Order processing route
        from("direct:processOrder")
            .routeId("order-processing-route")
            .log("Processing valid order")
            .multicast()
                .to("direct:enrichWithRos")
                .to("direct:notifyWms")
                .to("direct:updateCms")
            .end()
            .to("rabbitmq:esb.orders.processed");

        // ROS enrichment route
        from("direct:enrichWithRos")
            .routeId("ros-enrichment-route")
            .log("Enriching with ROS data")
            .process(new EnrichmentProcessor())
            .log("ROS enrichment completed");

        // WMS notification route
        from("direct:notifyWms")
            .routeId("wms-notification-route")
            .log("Notifying WMS")
            .process(new WmsTcpParserProcessor())
            .log("WMS notification sent");

        // CMS update route
        from("direct:updateCms")
            .routeId("cms-update-route")
            .log("Updating CMS")
            .process(new CmsSoapToCanonicalProcessor())
            .log("CMS update completed");

        // Health check route
        from("timer:health-check?period=30000")
            .routeId("health-check-route")
            .log("Performing health check")
            .to("direct:checkSystemHealth");

        from("direct:checkSystemHealth")
            .routeId("system-health-route")
            .multicast()
                .to("http://localhost:5001/cms/health")
                .to("http://localhost:5002/api/v1/health")
            .end()
            .log("Health check completed");
    }
}