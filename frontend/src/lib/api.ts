import axios from 'axios';

const API_BASE = {
  CMS: 'http://localhost:5001',
  ROS: 'http://localhost:5002/api/v1',
  WMS: 'http://localhost:5003'
};

export const cmsApi = {
  async healthCheck() {
    const response = await axios.get(`${API_BASE.CMS}/cms/health`);
    return response.data;
  },

  async createOrder(orderData: {
    clientId: string;
    recipientName: string;
    recipientAddress: string;
    recipientPhone: string;
    packageDetails: string;
  }) {
    const soapRequest = `<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <CreateOrder>
            <ClientId>${orderData.clientId}</ClientId>
            <RecipientName>${orderData.recipientName}</RecipientName>
            <RecipientAddress>${orderData.recipientAddress}</RecipientAddress>
            <RecipientPhone>${orderData.recipientPhone}</RecipientPhone>
            <PackageDetails>${orderData.packageDetails}</PackageDetails>
        </CreateOrder>
    </soap:Body>
</soap:Envelope>`;

    const response = await axios.post(`${API_BASE.CMS}/cms/soap`, soapRequest, {
      headers: {
        'Content-Type': 'text/xml',
        'SOAPAction': 'CreateOrder'
      }
    });
    return response.data;
  }
};

export const rosApi = {
  async healthCheck() {
    const response = await axios.get(`${API_BASE.ROS}/health`);
    return response.data;
  },

  async getVehicles() {
    const response = await axios.get(`${API_BASE.ROS}/vehicles`);
    return response.data;
  },

  async optimizeRoute(routeData: {
    vehicle_id: string;
    delivery_addresses: Array<{
      address: string;
      lat: number;
      lng: number;
      order_id: string;
    }>;
  }) {
    const response = await axios.post(`${API_BASE.ROS}/routes/optimize`, routeData);
    return response.data;
  }
};