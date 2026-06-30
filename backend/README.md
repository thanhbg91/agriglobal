# AgriGlobal AI - Backend API System

Hệ thống Backend API được xây dựng bằng **Node.js (Express)** để tiếp nhận, đồng bộ dữ liệu giao dịch và thông tin đăng tin nông sản từ ứng dụng di động AgriGlobal AI, đồng thời hỗ trợ lưu trữ linh hoạt vào nhiều hệ quản trị cơ sở dữ liệu khác nhau.

## ✨ Tính Năng Nổi Bật

- **Đa Cơ Sở Dữ Liệu (Multi-Database Support):** Chỉ cần đổi cấu hình trong `.env`, hệ thống sẽ tự động khởi tạo kết nối và bảng/collection tương ứng:
  - **MongoDB:** Định dạng tài liệu JSON linh hoạt (NoSQL).
  - **PostgreSQL:** Định dạng bảng chuẩn quan hệ (SQL) có ràng buộc chặt chẽ.
  - **MySQL:** Định dạng bảng chuẩn quan hệ (SQL) phổ biến.
  - **File-Based JSON (Mặc định):** Chạy ngay lập tức không cần cài đặt bất kỳ hệ quản trị cơ sở dữ liệu nào, dữ liệu lưu trực tiếp vào file JSON cục bộ cục bộ, thích hợp cho việc kiểm thử nhanh (Zero-Config).
- **CORS Enabled:** Cho phép ứng dụng Android chạy trên thiết bị vật lý hoặc trình giả lập (Emulator) gọi API trực tiếp.
- **Tự động đồng bộ:** Khởi tạo bảng tự động (PostgreSQL/MySQL) khi kết nối đầu tiên.

---

## 🛠️ Hướng Dẫn Cài Đặt & Khởi Chạy

### 1. Yêu cầu hệ thống
- Đã cài đặt **Node.js** (Phiên bản `>= 14.0.0`)
- **NPM** (Đi kèm khi cài đặt Node.js)

### 2. Cài đặt các thư viện phụ thuộc
Di chuyển vào thư mục `/backend` và chạy lệnh cài đặt:
```bash
cd backend
npm install
```

### 3. Cấu hình biến môi trường
Tạo file `.env` bằng cách sao chép file `.env.example`:
```bash
cp .env.example .env
```
Mở file `.env` và chọn loại cơ sở dữ liệu bạn mong muốn bằng cách chỉnh sửa biến `DATABASE_TYPE`:

```env
# Chọn một trong các giá trị: 'file', 'mongodb', 'postgres', 'mysql'
DATABASE_TYPE=file
```

---

## 💾 Thiết Lập Các Hệ Quản Trị Cơ Sở Dữ Liệu

### Option A: MongoDB (Dạng tài liệu JSON)
1. Đảm bảo dịch vụ MongoDB đang chạy cục bộ (Cổng mặc định `27017`) hoặc có một URI MongoDB Atlas.
2. Thiết lập trong file `.env`:
   ```env
   DATABASE_TYPE=mongodb
   MONGODB_URI=mongodb://localhost:27017/agriglobal
   ```

### Option B: PostgreSQL (Dạng bảng quan hệ)
1. Tạo một cơ sở dữ liệu trống có tên `agriglobal` trong PostgreSQL.
2. Thiết lập trong file `.env`:
   ```env
   DATABASE_TYPE=postgres
   PG_URI=postgresql://postgres:password@localhost:5432/agriglobal
   ```

### Option C: MySQL (Dạng bảng quan hệ)
1. Tạo một cơ sở dữ liệu trống có tên `agriglobal` trong MySQL.
2. Thiết lập trong file `.env`:
   ```env
   DATABASE_TYPE=mysql
   MYSQL_HOST=localhost
   MYSQL_USER=root
   MYSQL_PASSWORD=mypassword
   MYSQL_DATABASE=agriglobal
   MYSQL_PORT=3306
   ```

---

## 🏃 Chạy Server API

Chạy server ở chế độ Production:
```bash
npm start
```

Chạy server ở chế độ Development (Tự động tải lại code khi sửa đổi nếu có nodemon):
```bash
npm run dev
```

Khi chạy thành công, màn hình console sẽ hiển thị:
```text
=============================================================
🚀 SERVER RUNNING AT http://localhost:5000
📊 ENDPOINT TRẠNG THÁI: http://localhost:5000/api/status
=============================================================
```

---

## 📋 Danh Sách API Endpoints

### 1. Kiểm tra trạng thái hệ thống
- **Method:** `GET`
- **URL:** `/api/status`
- **Phản hồi mẫu:**
  ```json
  {
    "status": "online",
    "message": "AgriGlobal AI Backend API kết nối thành công!",
    "databaseType": "FILE",
    "timestamp": 1719730592312
  }
  ```

### 2. Đồng bộ danh sách sản phẩm đăng tin
- **Kiểm tra (Lấy danh sách):** `GET /api/products`
- **Tải lên (Đăng tin mới):** `POST /api/products`
- **JSON Body gửi lên mẫu:**
  ```json
  {
    "id": 102,
    "name": "Hạt Ca cao Hữu cơ Đắk Lắk S1",
    "category": "Cà phê & Ca cao",
    "priceUsd": 4.5,
    "availableQuantity": 15,
    "unit": "Tấn",
    "origin": "Việt Nam",
    "description": "Mô tả chất lượng cao...",
    "imageUrl": "https://...",
    "sellerName": "Hợp tác xã Ea Kmat",
    "sellerContact": "contact@eakmat.coop.vn",
    "isUserListing": true
  }
  ```

### 3. Đồng bộ danh sách đơn hàng giao thương
- **Kiểm tra (Lấy danh sách):** `GET /api/orders`
- **Tải lên (Ghi nhận giao dịch):** `POST /api/orders`
- **JSON Body gửi lên mẫu:**
  ```json
  {
    "id": 12,
    "productId": 3,
    "productName": "Gạo Thơm Jasmine Thái Lan",
    "quantity": 10,
    "priceUsd": 0.85,
    "unit": "Tấn",
    "imageUrl": "https://...",
    "sellerName": "Siam Rice Trading",
    "status": "Đã gửi Yêu cầu",
    "trackingNumber": "DHL123456789",
    "shippingProvider": "DHL Express",
    "shippingStatus": "Đang chuẩn bị hàng",
    "shippingHistory": "2026-06-30 10:00 - Hợp tác xã xác nhận đơn hàng thành công"
  }
  ```

---

## 📱 Kết Nối Với Ứng Dụng Android AgriGlobal AI

Hệ thống Android đã được tích hợp sẵn chức năng cấu hình Server đám mây. Bạn có thể thiết lập như sau:
1. Vào mục **Không Gian Doanh Nghiệp (Profile)** trên ứng dụng di động.
2. Nhấn vào biểu tượng **Răng cưa (Cài đặt)** ở góc trên bên phải.
3. Nhập URL của API server:
   - Nếu chạy trên giả lập **Android Emulator**: Nhập `http://10.0.2.2:5000` (Địa chỉ loopback đặc biệt để giả lập kết nối tới máy tính host).
   - Nếu chạy trên **Thiết bị thật**: Nhập địa chỉ IP mạng nội bộ Wi-Fi của máy tính đang chạy Node.js (Ví dụ: `http://192.168.1.15:5000`).
4. Nhấn **Kiểm Tra Kết Nối** để xác nhận kết nối thành công.
5. Nhấn **ĐỒNG BỘ TOÀN BỘ ĐÁM MÂY** để đẩy tất cả tin đăng sản phẩm nông sản tự tạo và lịch sử giao dịch địa phương lên hệ thống cơ sở dữ liệu MySQL, PostgreSQL hoặc MongoDB của bạn!
