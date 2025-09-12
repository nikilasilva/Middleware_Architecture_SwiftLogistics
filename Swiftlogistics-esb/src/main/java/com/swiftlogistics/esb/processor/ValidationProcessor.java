package com.swiftlogistics.esb.processor;

import com.swiftlogistics.esb.model.DeliveryOrder;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class ValidationProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        DeliveryOrder d = exchange.getIn().getBody(DeliveryOrder.class);
        boolean valid = d != null && d.getOrderId() != null && d.getDeliveryAddress() != null;
        exchange.getIn().setHeader("valid", valid);
        if (!valid) {
            exchange.getIn().setHeader("validation.error", "Missing required fields");
        }
    }
}
