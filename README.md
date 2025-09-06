python test_servers.py
🚀 SWIFTLOGISTICS SYSTEM PROTOTYPE TESTING
Testing all three heterogeneous systems...

Please make sure to install flask before hand :
pip install flask

## 🚀 Running & Testing SwiftLogistics Prototype

### Prerequisites

- Python 3.7+
- [Flask](https://flask.palletsprojects.com/) (`pip install flask`)
- All servers and the test script should be run in separate terminals.

---

### 1. Start All Prototype Servers

Open three separate terminals and run each server:

**CMS (SOAP/XML) Server**

```sh
python cms_server.py
```

- Runs on port **5001**

**ROS (REST/JSON) Server**

```sh
python ros_server.py
```

- Runs on port **5002**

**WMS (TCP/IP) Server**

```sh
python wms_server.py
```

- Runs on port **5003**

---

### 2. Run the System Test Script

Open a new terminal and run:

```sh
python test_servers.py
```

This script will:

- Test each system individually (CMS, ROS, WMS)
- Run a full integration flow across all systems

---

### 3. Interpreting the Output

- The script prints clear section headers for each system and integration test.
- Look for ✅ for successful operations and ❌ for errors.
- At the end, you’ll see a summary and suggested next steps.

---

### 4. Troubleshooting

- Ensure all servers are running before executing `test_servers.py`.
- If you see connection errors, check that the correct ports are open and not blocked by firewalls.
- For SOAP/XML errors, ensure the request format matches the expected WSDL.

---

### 5. Next Steps

- Build middleware/ESB for integration
- Add message queues (e.g., RabbitMQ)
- Implement API Gateway and real-time features

---

**Tip:** For best readability, view this README on GitHub with code blocks and headings as shown above.
