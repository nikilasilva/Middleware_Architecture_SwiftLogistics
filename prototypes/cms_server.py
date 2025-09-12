#!/usr/bin/env python3
"""
Client Management System (CMS) Prototype
SOAP/XML API Server - Legacy System Simulation
"""

from flask import Flask, request, Response
import xml.etree.ElementTree as ET
from datetime import datetime
import uuid

NAMESPACES = {"cms": "http://swiftlogistics.lk/cms"}

app = Flask(__name__)

# Mock database - in-memory storage
clients = {
    "CLIENT001": {
        "name": "TechMart Electronics",
        "email": "orders@techmart.lk",
        "contract_status": "active",
        "billing_rate": 150.00,
    },
    "CLIENT002": {
        "name": "Fashion Hub Lanka",
        "email": "logistics@fashionhub.lk",
        "contract_status": "active",
        "billing_rate": 120.00,
    },
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
        body = root.find(".//{http://schemas.xmlsoap.org/soap/envelope/}Body")
        if body is not None and len(body) > 0:
            return body[0]  # Return first child of body
        return None
    except ET.ParseError:
        return None


@app.route("/cms/soap", methods=["POST"])
def soap_endpoint():
    """Main SOAP endpoint for CMS operations"""

    try:
        # Parse SOAP request
        soap_body = parse_soap_request(request.data.decode("utf-8"))

        if soap_body is None:
            return create_soap_fault("Invalid SOAP request"), 500

        operation = (
            soap_body.tag.split("}")[-1] if "}" in soap_body.tag else soap_body.tag
        )

        if operation == "CreateOrder":
            return handle_create_order(soap_body)
        elif operation == "GetOrderStatus":
            return handle_get_order_status(soap_body)
        elif operation == "GetClientInfo":
            return handle_get_client_info(soap_body)
        elif operation == "UpdateBilling":
            return handle_update_billing(soap_body)
        elif operation == "CancelOrder":
            return handle_cancel_order(soap_body)
        else:
            return create_soap_fault(f"Unknown operation: {operation}"), 400
    except Exception as e:
        return create_soap_fault(f"Server error: {str(e)}"), 500


def handle_cancel_order(soap_body):
    """Handle order cancellation"""
    print("Orders in system:", orders.keys())
    try:
        # Try to find OrderId with or without namespace
        order_id_elem = soap_body.find("cms:OrderId", NAMESPACES)
        if order_id_elem is None:
            order_id_elem = soap_body.find("OrderId")
        if order_id_elem is None or not order_id_elem.text:
            return create_soap_fault("Missing OrderId in CancelOrder request"), 400

        external_order_id = order_id_elem.text.strip()
        print(f"[CMS] Looking for external order ID: {external_order_id}")
        
        # Find order by external_order_id or internal order_id
        found_order_id = None
        for internal_id, order in orders.items():
            print(f"[CMS] Checking order {internal_id}, external_id: {order.get('external_order_id')}")
            if (order.get("external_order_id") == external_order_id or 
                internal_id == external_order_id):
                found_order_id = internal_id
                break

        if not found_order_id:
            print(f"[CMS] Order not found for external ID: {external_order_id}")
            return create_soap_fault(f"Order not found for ID: {external_order_id}"), 404

        # Cancel the order
        orders[found_order_id]["status"] = "CANCELLED"
        orders[found_order_id]["cancelled_at"] = datetime.now().isoformat()

        response_body = f"""
        <cms:CancelOrderResponse>
            <cms:CancelResult>{external_order_id}</cms:CancelResult>
            <cms:Status>CANCELLED</cms:Status>
            <cms:Message>Order cancelled successfully</cms:Message>
        </cms:CancelOrderResponse>"""

        response = Response(
            create_soap_response(response_body),
            mimetype="text/xml",
            headers={"SOAPAction": "CancelOrderResponse"},
        )
        print(f"[CMS] Order cancelled via SOAP: {found_order_id} (external: {external_order_id})")
        return response
    except Exception as e:
        print(f"[CMS] ERROR in handle_cancel_order: {str(e)}")
        import traceback
        traceback.print_exc()
        return create_soap_fault(f"Error cancelling order: {str(e)}"), 500

    


def handle_create_order(soap_body):
    """Handle order creation"""
    try:
        print(f"[CMS] DEBUG: Received soap_body tag: {soap_body.tag}")
        print(f"[CMS] DEBUG: soap_body children: {[child.tag for child in soap_body]}")

        # Since we can see the elements are there with the namespace, let's use them directly
        def find_element_with_namespace(parent, element_name):
            """Find element with or without namespace"""
            # Try with namespace first
            elem = parent.find(f"{{{NAMESPACES['cms']}}}{element_name}")
            if elem is not None:
                return elem
            # Try without namespace
            elem = parent.find(element_name)
            if elem is not None:
                return elem
            # Try with xpath
            elem = parent.find(f".//{{{NAMESPACES['cms']}}}{element_name}")
            if elem is not None:
                return elem
            elem = parent.find(f".//{element_name}")
            return elem

        client_id_elem = find_element_with_namespace(soap_body, "ClientId")
        recipient_name_elem = find_element_with_namespace(soap_body, "RecipientName")
        recipient_address_elem = find_element_with_namespace(
            soap_body, "RecipientAddress"
        )
        recipient_phone_elem = find_element_with_namespace(soap_body, "RecipientPhone")
        package_details_elem = find_element_with_namespace(soap_body, "PackageDetails")
        order_id_elem = find_element_with_namespace(soap_body, "OrderId")  # Get external order ID

        print(f"[CMS] DEBUG: Elements found:")
        print(
            f"  ClientId: {client_id_elem is not None} - {client_id_elem.text if client_id_elem is not None else 'None'}"
        )
        print(
            f"  RecipientName: {recipient_name_elem is not None} - {recipient_name_elem.text if recipient_name_elem is not None else 'None'}"
        )
        print(
            f"  RecipientAddress: {recipient_address_elem is not None} - {recipient_address_elem.text if recipient_address_elem is not None else 'None'}"
        )
        print(
            f"  RecipientPhone: {recipient_phone_elem is not None} - {recipient_phone_elem.text if recipient_phone_elem is not None else 'None'}"
        )
        print(
            f"  PackageDetails: {package_details_elem is not None} - {package_details_elem.text if package_details_elem is not None else 'None'}"
        )

        # Check if elements exist and have text
        missing = []
        if client_id_elem is None or not client_id_elem.text:
            missing.append("ClientId")
        if recipient_name_elem is None or not recipient_name_elem.text:
            missing.append("RecipientName")
        if recipient_address_elem is None or not recipient_address_elem.text:
            missing.append("RecipientAddress")
        if recipient_phone_elem is None or not recipient_phone_elem.text:
            missing.append("RecipientPhone")
        if package_details_elem is None or not package_details_elem.text:
            missing.append("PackageDetails")

        if missing:
            print(f"[CMS] DEBUG: Missing or empty fields: {missing}")
            return (
                create_soap_fault(
                    f"Missing required fields in CreateOrder request: {', '.join(missing)}"
                ),
                400,
            )

        client_id = client_id_elem.text.strip()
        recipient_name = recipient_name_elem.text.strip()
        recipient_address = recipient_address_elem.text.strip()
        recipient_phone = recipient_phone_elem.text.strip()
        package_details = package_details_elem.text.strip()

        print(
            f"[CMS] DEBUG: Extracted data - ClientId: '{client_id}', RecipientName: '{recipient_name}'"
        )

        # Validate client
        if client_id not in clients:
            print(
                f"[CMS] DEBUG: Client '{client_id}' not found in clients: {list(clients.keys())}"
            )
            return create_soap_fault("Invalid client ID"), 400

        # Generate internal order ID
        internal_order_id = f"ORD{datetime.now().strftime('%Y%m%d')}{len(orders)+1:04d}"
        
        # Use provided external order ID or generate one
        external_order_id = order_id_elem.text.strip() if order_id_elem is not None and order_id_elem.text else internal_order_id

        # Create order
        orders[internal_order_id] = {
            "client_id": client_id,
            "recipient_name": recipient_name,
            "recipient_address": recipient_address,
            "recipient_phone": recipient_phone,
            "package_details": package_details,
            "status": "PENDING",
            "created_at": datetime.now().isoformat(),
            "billing_amount": clients[client_id]["billing_rate"],
            "external_order_id": external_order_id,  # Store the external order ID
        }

        response_body = f"""
        <cms:CreateOrderResponse>
            <cms:OrderId>{external_order_id}</cms:OrderId>
            <cms:InternalOrderId>{internal_order_id}</cms:InternalOrderId>
            <cms:Status>SUCCESS</cms:Status>
            <cms:Message>Order created successfully</cms:Message>
            <cms:EstimatedCost>{clients[client_id]["billing_rate"]}</cms:EstimatedCost>
        </cms:CreateOrderResponse>"""

        response = Response(
            create_soap_response(response_body),
            mimetype="text/xml",
            headers={"SOAPAction": "CreateOrderResponse"},
        )

        print(f"[CMS] Order created: {internal_order_id} (external: {external_order_id}) for client {client_id}")
        return response

    except Exception as e:
        print(f"[CMS] ERROR in handle_create_order: {str(e)}")
        import traceback

        traceback.print_exc()
        return create_soap_fault(f"Error creating order: {str(e)}"), 500


def handle_get_order_status(soap_body):
    """Handle order status inquiry"""
    try:
        order_id = soap_body.find("OrderId").text

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
            mimetype="text/xml",
            headers={"SOAPAction": "GetOrderStatusResponse"},
        )

        print(f"[CMS] Status requested for order: {order_id}")
        return response

    except Exception as e:
        return create_soap_fault(f"Error getting order status: {str(e)}"), 500


def handle_get_client_info(soap_body):
    try:
        client_id_elem = soap_body.find("cms:ClientId", NAMESPACES)
        if client_id_elem is None:
            return create_soap_fault("Missing ClientId in request"), 400

        client_id = client_id_elem.text

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
            mimetype="text/xml",
            headers={"SOAPAction": "GetClientInfoResponse"},
        )

        print(f"[CMS] Client info requested for: {client_id}")
        return response

    except Exception as e:
        return create_soap_fault(f"Error getting client info: {str(e)}"), 500


def handle_update_billing(soap_body):
    """Handle billing update"""
    try:
        order_id = soap_body.find("OrderId").text
        status = soap_body.find("Status").text

        if order_id not in orders:
            return create_soap_fault("Order not found"), 404

        orders[order_id]["status"] = status
        orders[order_id]["updated_at"] = datetime.now().isoformat()

        response_body = f"""
        <cms:UpdateBillingResponse>
            <cms:OrderId>{order_id}</cms:OrderId>
            <cms:Status>SUCCESS</cms:Status>
            <cms:Message>Billing updated successfully</cms:Message>
        </cms:UpdateBillingResponse>"""

        response = Response(
            create_soap_response(response_body),
            mimetype="text/xml",
            headers={"SOAPAction": "UpdateBillingResponse"},
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


@app.route("/cms/wsdl", methods=["GET"])
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

    return Response(wsdl, mimetype="text/xml")


@app.route("/cms/health", methods=["GET"])
def health_check():
    """Health check endpoint"""
    return {
        "service": "CMS",
        "status": "healthy",
        "protocol": "SOAP/XML",
        "timestamp": datetime.now().isoformat(),
        "orders_count": len(orders),
        "clients_count": len(clients),
    }


if __name__ == "__main__":
    print("Starting CMS (Client Management System) - SOAP/XML Server")
    print("WSDL available at: http://localhost:5001/cms/wsdl")
    print("SOAP Endpoint: http://localhost:5001/cms/soap")
    print("Health Check: http://localhost:5001/cms/health")
    app.run(host="0.0.0.0", port=5001, debug=True)
