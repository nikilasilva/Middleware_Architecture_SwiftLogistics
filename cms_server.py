#!/usr/bin/env python3
"""
Client Management System (CMS) Prototype
SOAP/XML API Server - Legacy System Simulation
"""

from flask import Flask, request, Response
import xml.etree.ElementTree as ET
from datetime import datetime
import uuid

app = Flask(__name__)

# Mock database - in-memory storage
clients = {
    "CLIENT001": {
        "name": "TechMart Electronics",
        "email": "orders@techmart.lk",
        "contract_status": "active",
        "billing_rate": 150.00
    },
    "CLIENT002": {
        "name": "Fashion Hub Lanka",
        "email": "logistics@fashionhub.lk", 
        "contract_status": "active",
        "billing_rate": 120.00
    }
}

orders = {}

def create_soap_response(body_content):
    """Create SOAP envelope response"""
    soap_response = f"""<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:cms="http://swiftlogistics.lk/cms">
    <soap:Header/>
    <soap:Body>
        {body_content}
    </soap:Body>
</soap:Envelope>"""
    return soap_response

def parse_soap_request(xml_data):
    """Parse SOAP request and extract body content"""
    try:
        root = ET.fromstring(xml_data)
        # Find the body element
        body = root.find('.//{http://schemas.xmlsoap.org/soap/envelope/}Body')
        if body is not None and len(body) > 0:
            return body[0]  # Return first child of body
        return None
    except ET.ParseError:
        return None

@app.route('/cms/soap', methods=['POST'])
def soap_endpoint():
    """Main SOAP endpoint for CMS operations"""
    
    try:
        # Parse SOAP request
        soap_body = parse_soap_request(request.data.decode('utf-8'))
        
        if soap_body is None:
            return create_soap_fault("Invalid SOAP request"), 500
        
        operation = soap_body.tag.split('}')[-1] if '}' in soap_body.tag else soap_body.tag
        
        if operation == "CreateOrder":
            return handle_create_order(soap_body)
        elif operation == "GetOrderStatus":
            return handle_get_order_status(soap_body)
        elif operation == "GetClientInfo":
            return handle_get_client_info(soap_body)
        elif operation == "UpdateBilling":
            return handle_update_billing(soap_body)
        else:
            return create_soap_fault(f"Unknown operation: {operation}"), 400
            
    except Exception as e:
        return create_soap_fault(f"Server error: {str(e)}"), 500

def handle_create_order(soap_body):
    """Handle order creation"""
    try:
        client_id = soap_body.find('ClientId').text
        recipient_name = soap_body.find('RecipientName').text
        recipient_address = soap_body.find('RecipientAddress').text
        recipient_phone = soap_body.find('RecipientPhone').text
        package_details = soap_body.find('PackageDetails').text
        
        # Validate client
        if client_id not in clients:
            return create_soap_fault("Invalid client ID"), 400
        
        # Generate order ID
        order_id = f"ORD{datetime.now().strftime('%Y%m%d')}{len(orders)+1:04d}"
        
        # Create order
        orders[order_id] = {
            "client_id": client_id,
            "recipient_name": recipient_name,
            "recipient_address": recipient_address,
            "recipient_phone": recipient_phone,
            "package_details": package_details,
            "status": "PENDING",
            "created_at": datetime.now().isoformat(),
            "billing_amount": clients[client_id]["billing_rate"]
        }
        
        response_body = f"""
        <cms:CreateOrderResponse>
            <cms:OrderId>{order_id}</cms:OrderId>
            <cms:Status>SUCCESS</cms:Status>
            <cms:Message>Order created successfully</cms:Message>
            <cms:EstimatedCost>{clients[client_id]["billing_rate"]}</cms:EstimatedCost>
        </cms:CreateOrderResponse>"""
        
        response = Response(
            create_soap_response(response_body),
            mimetype='text/xml',
            headers={'SOAPAction': 'CreateOrderResponse'}
        )
        
        print(f"[CMS] Order created: {order_id} for client {client_id}")
        return response
        
    except Exception as e:
        return create_soap_fault(f"Error creating order: {str(e)}"), 500

def handle_get_order_status(soap_body):
    """Handle order status inquiry"""
    try:
        order_id = soap_body.find('OrderId').text
        
        if order_id not in orders:
            return create_soap_fault("Order not found"), 404
        
        order = orders[order_id]
        
        response_body = f"""
        <cms:GetOrderStatusResponse>
            <cms:OrderId>{order_id}</cms:OrderId>
            <cms:ClientId>{order['client_id']}</cms:ClientId>
            <cms:Status>{order['status']}</cms:Status>
            <cms:CreatedAt>{order['created_at']}</cms:CreatedAt>
            <cms:BillingAmount>{order['billing_amount']}</cms:BillingAmount>
            <cms:RecipientDetails>
                <cms:Name>{order['recipient_name']}</cms:Name>
                <cms:Address>{order['recipient_address']}</cms:Address>
                <cms:Phone>{order['recipient_phone']}</cms:Phone>
            </cms:RecipientDetails>
        </cms:GetOrderStatusResponse>"""
        
        response = Response(
            create_soap_response(response_body),
            mimetype='text/xml',
            headers={'SOAPAction': 'GetOrderStatusResponse'}
        )
        
        print(f"[CMS] Status requested for order: {order_id}")
        return response
        
    except Exception as e:
        return create_soap_fault(f"Error getting order status: {str(e)}"), 500

def handle_get_client_info(soap_body):
    """Handle client information request"""
    try:
        client_id = soap_body.find('ClientId').text
        
        if client_id not in clients:
            return create_soap_fault("Client not found"), 404
        
        client = clients[client_id]
        
        response_body = f"""
        <cms:GetClientInfoResponse>
            <cms:ClientId>{client_id}</cms:ClientId>
            <cms:Name>{client['name']}</cms:Name>
            <cms:Email>{client['email']}</cms:Email>
            <cms:ContractStatus>{client['contract_status']}</cms:ContractStatus>
            <cms:BillingRate>{client['billing_rate']}</cms:BillingRate>
        </cms:GetClientInfoResponse>"""
        
        response = Response(
            create_soap_response(response_body),
            mimetype='text/xml',
            headers={'SOAPAction': 'GetClientInfoResponse'}
        )
        
        print(f"[CMS] Client info requested for: {client_id}")
        return response
        
    except Exception as e:
        return create_soap_fault(f"Error getting client info: {str(e)}"), 500

def handle_update_billing(soap_body):
    """Handle billing update"""
    try:
        order_id = soap_body.find('OrderId').text
        status = soap_body.find('Status').text
        
        if order_id not in orders:
            return create_soap_fault("Order not found"), 404
        
        orders[order_id]['status'] = status
        orders[order_id]['updated_at'] = datetime.now().isoformat()
        
        response_body = f"""
        <cms:UpdateBillingResponse>
            <cms:OrderId>{order_id}</cms:OrderId>
            <cms:Status>SUCCESS</cms:Status>
            <cms:Message>Billing updated successfully</cms:Message>
        </cms:UpdateBillingResponse>"""
        
        response = Response(
            create_soap_response(response_body),
            mimetype='text/xml',
            headers={'SOAPAction': 'UpdateBillingResponse'}
        )
        
        print(f"[CMS] Billing updated for order: {order_id} to {status}")
        return response
        
    except Exception as e:
        return create_soap_fault(f"Error updating billing: {str(e)}"), 500

def create_soap_fault(error_message):
    """Create SOAP fault response"""
    fault_body = f"""
    <soap:Fault>
        <faultcode>Server</faultcode>
        <faultstring>{error_message}</faultstring>
    </soap:Fault>"""
    
    return create_soap_response(fault_body)

@app.route('/cms/wsdl', methods=['GET'])
def get_wsdl():
    """Provide WSDL definition"""
    wsdl = """<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://schemas.xmlsoap.org/wsdl/"
             xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
             xmlns:cms="http://swiftlogistics.lk/cms"
             targetNamespace="http://swiftlogistics.lk/cms">
    
    <message name="CreateOrderRequest">
        <part name="ClientId" type="xsd:string"/>
        <part name="RecipientName" type="xsd:string"/>
        <part name="RecipientAddress" type="xsd:string"/>
        <part name="RecipientPhone" type="xsd:string"/>
        <part name="PackageDetails" type="xsd:string"/>
    </message>
    
    <message name="CreateOrderResponse">
        <part name="OrderId" type="xsd:string"/>
        <part name="Status" type="xsd:string"/>
        <part name="Message" type="xsd:string"/>
        <part name="EstimatedCost" type="xsd:float"/>
    </message>
    
    <portType name="CMSPortType">
        <operation name="CreateOrder">
            <input message="cms:CreateOrderRequest"/>
            <output message="cms:CreateOrderResponse"/>
        </operation>
    </portType>
    
    <binding name="CMSBinding" type="cms:CMSPortType">
        <soap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/>
        <operation name="CreateOrder">
            <soap:operation soapAction="CreateOrder"/>
            <input><soap:body use="literal"/></input>
            <output><soap:body use="literal"/></output>
        </operation>
    </binding>
    
    <service name="CMSService">
        <port name="CMSPort" binding="cms:CMSBinding">
            <soap:address location="http://localhost:5001/cms/soap"/>
        </port>
    </service>
</definitions>"""
    
    return Response(wsdl, mimetype='text/xml')

@app.route('/cms/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return {
        "service": "CMS",
        "status": "healthy",
        "protocol": "SOAP/XML",
        "timestamp": datetime.now().isoformat(),
        "orders_count": len(orders),
        "clients_count": len(clients)
    }

if __name__ == '__main__':
    print("Starting CMS (Client Management System) - SOAP/XML Server")
    print("WSDL available at: http://localhost:5001/cms/wsdl")
    print("SOAP Endpoint: http://localhost:5001/cms/soap")
    print("Health Check: http://localhost:5001/cms/health")
    app.run(host='0.0.0.0', port=5001, debug=True)