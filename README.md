# 🚀 Mini-Redis Java Clone

A high-performance, thread-safe, in-memory NoSQL database built entirely from scratch in Java. This project replicates core Redis functionalities, focusing on low-level networking, concurrency, and data persistence without relying on external database frameworks.

## ✨ Core Features

*   **Custom TCP Server**: Built using native Java `ServerSocket` and `Socket` I/O streams.
*   **High Concurrency**: Handles multiple simultaneous client connections using an `ExecutorService` (Fixed Thread Pool).
*   **Thread-Safe Storage**: Utilizes `ConcurrentHashMap` to guarantee data integrity during concurrent read/write operations.
*   **Data Persistence (AOF)**: Implements an Append-Only File (`database.aof`) for durable storage and automatic crash recovery on startup.
*   **Lazy TTL Expiration**: Memory-efficient key expiration mechanism that cleans up stale data dynamically.
*   **Graceful Shutdown**: Intercepts OS signals (SIGINT) to close connections safely and prevent data corruption.
*   **Automated Testing**: Comprehensive unit and network-level integration tests using JUnit 5.

## 🛠️ Supported Commands

| Command | Syntax | Description |
| :--- | :--- | :--- |
| **PING** | `PING` | Tests the server connection. Returns `+PONG`. |
| **SET** | `SET <key> <value>` | Stores a string value. |
| **GET** | `GET <key>` | Retrieves a string value. |
| **DEL** | `DEL <key>` | Deletes a key from memory and AOF. |
| **EXISTS**| `EXISTS <key>` | Returns `1` if key exists, `0` otherwise. |
| **EXPIRE**| `EXPIRE <key> <seconds>` | Sets a timeout on a key. |
| **LPUSH** | `LPUSH <key> <value>` | Prepends a value to a list. |
| **LRANGE**| `LRANGE <key>` | Retrieves all elements in a list. |

## 🚀 Performance & Benchmarks

The server was benchmarked using a custom multi-threaded testing suite simulating **100,000 requests** sent over **50 concurrent permanent TCP connections**.

| Command | Throughput (OPS) | Total Time | Bottleneck Note |
|---------|------------------|------------|-----------------|
| `GET`   | **33,355 req/s** | 2,998 ms   | Bound by CPU & Memory access (instant) |
| `SET`   | **2,637 req/s**  | 37,912 ms  | Bound by Disk I/O (AOF synchronization) |

## 💻 How to Run

1. Clone the repository and compile the Java files.
2. Run the `Main.java` class. The server will bind to port `6379`.
3. Connect using standard network tools:
   ```bash
   telnet localhost 6379
   # or using netcat
   echo "PING" | nc localhost 6379

 ## 🐳 Run with Docker

You can easily run the server using Docker, completely isolated and without needing Java installed on your machine. Data persistence is ensured via Docker volumes.

1. Build the image:
   ```bash
   docker build -t mini-redis-java .
