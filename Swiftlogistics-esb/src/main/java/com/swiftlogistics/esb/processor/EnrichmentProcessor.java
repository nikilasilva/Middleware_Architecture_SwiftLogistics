package com.swiftlogistics.esb.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swiftlogistics.esb.model.DeliveryOrder;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.http.HttpMethods;
import org.apache.http.entity.ContentType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

public class EnrichmentProcessor implements Processor {
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(EnrichmentProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        DeliveryOrder d = exchange.getIn().getBody(DeliveryOrder.class);
        if (d == null) return;

        // Build JSON request expected by ROS mock
        ObjectNode req = mapper.createObjectNode();
        req.put("orderId", d.getOrderId());
        req.put("pickup", d.getPickupAddress() == null ? "" : d.getPickupAddress());
        req.put("delivery", d.getDeliveryAddress());

        // Call ROS mock synchronously (http4 component)
        exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
        exchange.getIn().setHeader(Exchange.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        exchange.getIn().setBody(req.toString());

        // call ROS - note: this will perform an HTTP call because the route uses direct:callRos
        Exchange rosResponse = exchange.getContext().createProducerTemplate()
                .send("http4://localhost:5002/optimize", exchange1 -> {
                    exchange1.getIn().setBody(req.toString());
                    exchange1.getIn().setHeader(Exchange.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
                    exchange1.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
                });

        if (rosResponse != null && rosResponse.getIn() != null) {
            String body = rosResponse.getIn().getBody(String.class);
            try {
                Map<String, Object> resp = mapper.readValue(body, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                // Attach route info into metadata
                Map<String,Object> meta = d.getMetadata() == null ? new HashMap<>() : d.getMetadata();
                meta.put("route", resp.containsKey("route") ? resp.get("route") : "");
                d.setMetadata(meta);
            } catch (Exception e) {
                // ignore enrichment failure but log
                LOG.warn("Failed to parse ROS response", e);
            }
        }

        exchange.getIn().setBody(d);
    }
}
