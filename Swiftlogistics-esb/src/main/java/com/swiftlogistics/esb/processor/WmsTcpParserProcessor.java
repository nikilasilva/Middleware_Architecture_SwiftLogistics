package com.swiftlogistics.esb.processor;

import com.swiftlogistics.esb.model.DeliveryOrder;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class WmsTcpParserProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String raw = exchange.getIn().getBody(String.class);
        if (raw == null) return;

        // Protocol example: KEY1|val1;KEY2|val2;\n
        Map<String,String> parts = Arrays.stream(raw.split(";"))
                .map(s -> s.split("\\|", 2))
                .filter(a -> a.length == 2)
                .collect(Collectors.toMap(a -> a[0].trim(), a -> a[1].trim()));

        DeliveryOrder d = new DeliveryOrder();
        d.setOrderId(parts.getOrDefault("ORDER_ID", java.util.UUID.randomUUID().toString()));
        d.setPickupAddress(parts.get("PICKUP"));
        d.setDeliveryAddress(parts.get("DELIVERY"));
        d.setClientId(parts.get("CLIENT_ID"));

        exchange.getIn().setBody(d);
    }
}
