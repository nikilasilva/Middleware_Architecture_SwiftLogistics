package com.swiftlogistics.esb.processor;

import com.swiftlogistics.esb.model.DeliveryOrder;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.w3c.dom.Node;

public class CmsSoapToCanonicalProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        // Try to get DOM Node or raw XML string
        Node node = exchange.getIn().getBody(Node.class);
        String xml = node == null ? exchange.getIn().getBody(String.class) : node.getTextContent();

        // Very simple XML tag extraction tailored for the mock's XML structure
        String orderId = extractTag(xml, "orderId");
        String clientId = extractTag(xml, "clientId");
        String pickup = extractTag(xml, "pickupAddress");
        String delivery = extractTag(xml, "deliveryAddress");

        DeliveryOrder d = new DeliveryOrder();
        d.setOrderId(orderId == null ? java.util.UUID.randomUUID().toString() : orderId);
        d.setClientId(clientId);
        d.setPickupAddress(pickup);
        d.setDeliveryAddress(delivery);

        exchange.getIn().setBody(d);
    }

    private String extractTag(String xml, String tag) {
        if (xml == null) return null;
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int i = xml.indexOf(open);
        int j = xml.indexOf(close);
        if (i >= 0 && j > i) {
            return xml.substring(i + open.length(), j).trim();
        }
        return null;
    }
}
