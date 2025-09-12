package com.swiftlogistics.esb.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.swiftlogistics.esb.model.DeliveryOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Service
public class CmsService {

    private final RestTemplate restTemplate;
    private static final String CMS_SOAP_URL = "http://localhost:5001/cms/soap";
    private static final Logger logger = LoggerFactory.getLogger(CmsService.class);

    public CmsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchClientData(String clientId) {
        try {
            logger.info("Fetching client data for: {}", clientId);
            String soapRequest = createGetClientInfoSoapRequest(clientId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.set("SOAPAction", "GetClientInfo");

            HttpEntity<String> request = new HttpEntity<>(soapRequest, headers);
            String response = restTemplate.postForObject(CMS_SOAP_URL, request, String.class);

            return extractClientInfo(response);
        } catch (Exception e) {
            logger.error("Error fetching client data: ", e);
            return "Error fetching client data: " + e.getMessage();
        }
    }

    public String validateClient(String clientId) {
        try {
            logger.info("Validating client: {}", clientId);
            String clientData = fetchClientData(clientId);

            if (clientData.contains("Error")) {
                return "Invalid client";
            }
            return "Client validated: " + clientData;
        } catch (Exception e) {
            logger.error("Error validating client: ", e);
            return "Client validation failed: " + e.getMessage();
        }
    }

    public String createOrder(DeliveryOrder order) {
        try {
            logger.info("Creating order in CMS for: {}", order.getOrderId());

            String soapRequest = createOrderSoapRequest(order);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.set("SOAPAction", "CreateOrder");

            HttpEntity<String> request = new HttpEntity<>(soapRequest, headers);
            String response = restTemplate.postForObject(CMS_SOAP_URL, request, String.class);
            logger.debug("Create Order SOAP Response: {}", response);

            return extractOrderId(response);
        } catch (Exception e) {
            logger.error("Error creating order: ", e);
            return "Error creating order: " + e.getMessage();
        }
    }

    // private helper methods
    private String createGetClientInfoSoapRequest(String clientId) {
        return String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                        "               xmlns:cms=\"http://swiftlogistics.lk/cms\">\n" +
                        "    <soap:Header/>\n" +
                        "    <soap:Body>\n" +
                        "        <cms:GetClientInfo>\n" +
                        "            <cms:ClientId>%s</cms:ClientId>\n" +
                        "        </cms:GetClientInfo>\n" +
                        "    </soap:Body>\n" +
                        "</soap:Envelope>\n",
                clientId);
    }

    private String createOrderSoapRequest(DeliveryOrder order) {
        return String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                        "               xmlns:cms=\"http://swiftlogistics.lk/cms\">\n" +
                        "    <soap:Header/>\n" +
                        "    <soap:Body>\n" +
                        "        <cms:CreateOrder>\n" +
                        "            <cms:ClientId>%s</cms:ClientId>\n" +
                        "            <cms:RecipientName>Customer</cms:RecipientName>\n" +
                        "            <cms:RecipientAddress>%s</cms:RecipientAddress>\n" +
                        "            <cms:RecipientPhone>0771234567</cms:RecipientPhone>\n" +
                        "            <cms:PackageDetails>Package for %s</cms:PackageDetails>\n" +
                        "        </cms:CreateOrder>\n" +
                        "    </soap:Body>\n" +
                        "</soap:Envelope>\n",
                order.getClientId(), order.getDeliveryAddress(), order.getOrderId());
    }

    private String extractClientInfo(String soapResponse) {
        // Simple XML parsing - extract client name
        if (soapResponse != null && soapResponse.contains("<cms:Name>")) {
            int start = soapResponse.indexOf("<cms:Name>") + 10;
            int end = soapResponse.indexOf("</cms:Name>");
            if (end > start) {
                return soapResponse.substring(start, end);
            }
        }
        return "Client data retrieved";
    }

    private String extractOrderId(String soapResponse) {
        if (soapResponse != null && soapResponse.contains("<cms:OrderId>")) {
            int start = soapResponse.indexOf("<cms:OrderId>") + 13;
            int end = soapResponse.indexOf("</cms:OrderId>");
            if (end > start) {
                return soapResponse.substring(start, end);
            }
        }
        return "ORDER_" + System.currentTimeMillis();
    }
}