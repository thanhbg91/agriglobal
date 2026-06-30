const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 5000;
const DATABASE_TYPE = (process.env.DATABASE_TYPE || 'file').toLowerCase();

app.use(cors());
app.use(express.json());

// Log incoming requests
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
  next();
});

// Database Interfaces
let dbEngine = {
  initialize: async () => {},
  getProducts: async () => [],
  saveProduct: async (product) => product,
  getOrders: async () => [],
  saveOrder: async (order) => order,
  saveUser: async (user) => user,
  findUserByEmail: async (email) => null,
};

// =========================================================================
// 1. FALLBACK ENGINE: LOCAL FILE-BASED STORAGE
// =========================================================================
const productsFile = path.join(__dirname, 'data_products.json');
const ordersFile = path.join(__dirname, 'data_orders.json');
const usersFile = path.join(__dirname, 'data_users.json');

const fileEngine = {
  initialize: async () => {
    console.log('📂 Khởi tạo cơ sở dữ liệu File-Based JSON local...');
    if (!fs.existsSync(productsFile)) {
      fs.writeFileSync(productsFile, JSON.stringify([], null, 2));
    }
    if (!fs.existsSync(ordersFile)) {
      fs.writeFileSync(ordersFile, JSON.stringify([], null, 2));
    }
    if (!fs.existsSync(usersFile)) {
      fs.writeFileSync(usersFile, JSON.stringify([], null, 2));
    }
    console.log(`✅ File-Based DB sẵn sàng. Lưu tại: ${productsFile}, ${ordersFile}, ${usersFile}`);
  },
  getProducts: async () => {
    const data = fs.readFileSync(productsFile, 'utf8');
    return JSON.parse(data);
  },
  saveProduct: async (product) => {
    const products = await fileEngine.getProducts();
    const newProduct = { id: products.length + 1, ...product, timestamp: Date.now() };
    products.push(newProduct);
    fs.writeFileSync(productsFile, JSON.stringify(products, null, 2));
    return newProduct;
  },
  getOrders: async () => {
    const data = fs.readFileSync(ordersFile, 'utf8');
    return JSON.parse(data);
  },
  saveOrder: async (order) => {
    const orders = await fileEngine.getOrders();
    const newOrder = { id: orders.length + 1, ...order, timestamp: Date.now() };
    orders.push(newOrder);
    fs.writeFileSync(ordersFile, JSON.stringify(orders, null, 2));
    return newOrder;
  },
  getUsers: async () => {
    const data = fs.readFileSync(usersFile, 'utf8');
    return JSON.parse(data);
  },
  saveUser: async (user) => {
    const users = await fileEngine.getUsers();
    const newUser = { id: users.length + 1, ...user, timestamp: Date.now() };
    users.push(newUser);
    fs.writeFileSync(usersFile, JSON.stringify(users, null, 2));
    return newUser;
  },
  findUserByEmail: async (email) => {
    const users = await fileEngine.getUsers();
    return users.find(u => u.email.toLowerCase() === email.toLowerCase()) || null;
  }
};

// =========================================================================
// 2. MONGODB ENGINE (Dạng tài liệu JSON NoSQL)
// =========================================================================
let mongoose;
const mongoEngine = {
  initialize: async () => {
    try {
      mongoose = require('mongoose');
      const uri = process.env.MONGODB_URI || 'mongodb://localhost:27017/agriglobal';
      console.log(`🔌 Đang kết nối tới MongoDB: ${uri}`);
      await mongoose.connect(uri);
      console.log('✅ Kết nối MongoDB thành công.');
    } catch (err) {
      console.error('❌ Thất bại khi kết nối MongoDB. Đảm bảo mongoose đã được cài đặt và server đang chạy.', err.message);
      process.exit(1);
    }
  },
  getProducts: async () => {
    const ProductModel = mongoose.model('Product', new mongoose.Schema({}, { strict: false }), 'products');
    return await ProductModel.find({});
  },
  saveProduct: async (product) => {
    const ProductModel = mongoose.model('Product', new mongoose.Schema({}, { strict: false }), 'products');
    const newDoc = new ProductModel(product);
    return await newDoc.save();
  },
  getOrders: async () => {
    const OrderModel = mongoose.model('Order', new mongoose.Schema({}, { strict: false }), 'orders');
    return await OrderModel.find({});
  },
  saveOrder: async (order) => {
    const OrderModel = mongoose.model('Order', new mongoose.Schema({}, { strict: false }), 'orders');
    const newDoc = new OrderModel(order);
    return await newDoc.save();
  },
  saveUser: async (user) => {
    const UserModel = mongoose.model('User', new mongoose.Schema({}, { strict: false }), 'users');
    const newDoc = new UserModel({ ...user, timestamp: Date.now() });
    return await newDoc.save();
  },
  findUserByEmail: async (email) => {
    const UserModel = mongoose.model('User', new mongoose.Schema({}, { strict: false }), 'users');
    return await UserModel.findOne({ email: new RegExp(`^${email}$`, 'i') });
  }
};

// =========================================================================
// 3. POSTGRESQL ENGINE (Dạng bảng quan hệ)
// =========================================================================
let pgPool;
const postgresEngine = {
  initialize: async () => {
    try {
      const { Pool } = require('pg');
      const config = process.env.PG_URI
        ? { connectionString: process.env.PG_URI }
        : {
            host: process.env.PG_HOST || 'localhost',
            user: process.env.PG_USER || 'postgres',
            password: process.env.PG_PASSWORD || 'password',
            database: process.env.PG_DATABASE || 'agriglobal',
            port: parseInt(process.env.PG_PORT || '5432'),
          };

      console.log(`🔌 Đang kết nối tới PostgreSQL: ${config.connectionString || config.host}`);
      pgPool = new Pool(config);

      // Verify connection and create tables
      await pgPool.query('SELECT NOW()');
      console.log('✅ Kết nối PostgreSQL thành công.');

      // Create products table
      await pgPool.query(`
        CREATE TABLE IF NOT EXISTS products (
          id SERIAL PRIMARY KEY,
          android_id INTEGER,
          name VARCHAR(255) NOT NULL,
          category VARCHAR(100),
          price_usd DOUBLE PRECISION,
          available_quantity DOUBLE PRECISION,
          unit VARCHAR(50),
          origin VARCHAR(100),
          description TEXT,
          image_url TEXT,
          seller_name VARCHAR(255),
          seller_contact VARCHAR(255),
          is_user_listing BOOLEAN,
          timestamp BIGINT
        )
      `);

      // Create orders table
      await pgPool.query(`
        CREATE TABLE IF NOT EXISTS orders (
          id SERIAL PRIMARY KEY,
          android_id INTEGER,
          product_id INTEGER,
          product_name VARCHAR(255),
          quantity DOUBLE PRECISION,
          price_usd DOUBLE PRECISION,
          unit VARCHAR(50),
          image_url TEXT,
          seller_name VARCHAR(255),
          status VARCHAR(100),
          tracking_number VARCHAR(100),
          shipping_provider VARCHAR(100),
          shipping_status VARCHAR(100),
          shipping_history TEXT,
          timestamp BIGINT
        )
      `);

      // Create users table
      await pgPool.query(`
        CREATE TABLE IF NOT EXISTS users (
          id SERIAL PRIMARY KEY,
          email VARCHAR(255) NOT NULL UNIQUE,
          password VARCHAR(255) NOT NULL,
          firebase_uid VARCHAR(255),
          timestamp BIGINT
        )
      `);
      console.log('🏁 Khởi tạo cấu trúc bảng PostgreSQL hoàn tất.');
    } catch (err) {
      console.error('❌ Lỗi kết nối PostgreSQL. Hãy kiểm tra cài đặt thư viện pg và tài khoản DB:', err.message);
      process.exit(1);
    }
  },
  getProducts: async () => {
    const res = await pgPool.query('SELECT * FROM products ORDER BY id DESC');
    return res.rows.map(row => ({
      id: row.android_id || row.id,
      dbId: row.id,
      name: row.name,
      category: row.category,
      priceUsd: row.price_usd,
      availableQuantity: row.available_quantity,
      unit: row.unit,
      origin: row.origin,
      description: row.description,
      imageUrl: row.image_url,
      sellerName: row.seller_name,
      sellerContact: row.seller_contact,
      isUserListing: row.is_user_listing,
      timestamp: Number(row.timestamp)
    }));
  },
  saveProduct: async (product) => {
    const q = `
      INSERT INTO products (
        android_id, name, category, price_usd, available_quantity, unit, origin,
        description, image_url, seller_name, seller_contact, is_user_listing, timestamp
      ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)
      RETURNING *
    `;
    const values = [
      product.id || 0,
      product.name,
      product.category,
      product.priceUsd,
      product.availableQuantity,
      product.unit,
      product.origin,
      product.description,
      product.imageUrl,
      product.sellerName,
      product.sellerContact,
      product.isUserListing,
      product.timestamp || Date.now()
    ];
    const res = await pgPool.query(q, values);
    return res.rows[0];
  },
  getOrders: async () => {
    const res = await pgPool.query('SELECT * FROM orders ORDER BY id DESC');
    return res.rows.map(row => ({
      id: row.android_id || row.id,
      dbId: row.id,
      productId: row.product_id,
      productName: row.product_name,
      quantity: row.quantity,
      priceUsd: row.price_usd,
      unit: row.unit,
      imageUrl: row.image_url,
      sellerName: row.seller_name,
      status: row.status,
      trackingNumber: row.tracking_number,
      shippingProvider: row.shipping_provider,
      shippingStatus: row.shipping_status,
      shippingHistory: row.shipping_history,
      timestamp: Number(row.timestamp)
    }));
  },
  saveOrder: async (order) => {
    const q = `
      INSERT INTO orders (
        android_id, product_id, product_name, quantity, price_usd, unit, image_url,
        seller_name, status, tracking_number, shipping_provider, shipping_status, shipping_history, timestamp
      ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
      RETURNING *
    `;
    const values = [
      order.id || 0,
      order.productId,
      order.productName,
      order.quantity,
      order.priceUsd,
      order.unit,
      order.imageUrl,
      order.sellerName,
      order.status,
      order.trackingNumber,
      order.shippingProvider,
      order.shippingStatus,
      order.shippingHistory,
      order.timestamp || Date.now()
    ];
    const res = await pgPool.query(q, values);
    return res.rows[0];
  },
  saveUser: async (user) => {
    const q = `
      INSERT INTO users (email, password, firebase_uid, timestamp)
      VALUES ($1, $2, $3, $4)
      RETURNING *
    `;
    const values = [
      user.email,
      user.password,
      user.firebaseUid || null,
      Date.now()
    ];
    const res = await pgPool.query(q, values);
    return res.rows[0];
  },
  findUserByEmail: async (email) => {
    const res = await pgPool.query('SELECT * FROM users WHERE LOWER(email) = LOWER($1)', [email]);
    return res.rows[0] || null;
  }
};

// =========================================================================
// 4. MYSQL ENGINE (Dạng bảng quan hệ)
// =========================================================================
let mysqlConn;
const mysqlEngine = {
  initialize: async () => {
    try {
      const mysql = require('mysql2/promise');
      console.log(`🔌 Đang kết nối tới MySQL: ${process.env.MYSQL_HOST || 'localhost'}`);
      mysqlConn = await mysql.createConnection({
        host: process.env.MYSQL_HOST || 'localhost',
        user: process.env.MYSQL_USER || 'root',
        password: process.env.MYSQL_PASSWORD || '',
        database: process.env.MYSQL_DATABASE || 'agriglobal',
        port: parseInt(process.env.MYSQL_PORT || '3306')
      });

      console.log('✅ Kết nối MySQL thành công.');

      // Create products table
      await mysqlConn.query(`
        CREATE TABLE IF NOT EXISTS products (
          id INT AUTO_INCREMENT PRIMARY KEY,
          android_id INT,
          name VARCHAR(255) NOT NULL,
          category VARCHAR(100),
          price_usd DOUBLE,
          available_quantity DOUBLE,
          unit VARCHAR(50),
          origin VARCHAR(100),
          description TEXT,
          image_url TEXT,
          seller_name VARCHAR(255),
          seller_contact VARCHAR(255),
          is_user_listing BOOLEAN,
          timestamp BIGINT
        )
      `);

      // Create orders table
      await mysqlConn.query(`
        CREATE TABLE IF NOT EXISTS orders (
          id INT AUTO_INCREMENT PRIMARY KEY,
          android_id INT,
          product_id INT,
          product_name VARCHAR(255),
          quantity DOUBLE,
          price_usd DOUBLE,
          unit VARCHAR(50),
          image_url TEXT,
          seller_name VARCHAR(255),
          status VARCHAR(100),
          tracking_number VARCHAR(100),
          shipping_provider VARCHAR(100),
          shipping_status VARCHAR(100),
          shipping_history TEXT,
          timestamp BIGINT
        )
      `);

      // Create users table
      await mysqlConn.query(`
        CREATE TABLE IF NOT EXISTS users (
          id INT AUTO_INCREMENT PRIMARY KEY,
          email VARCHAR(255) NOT NULL UNIQUE,
          password VARCHAR(255) NOT NULL,
          firebase_uid VARCHAR(255),
          timestamp BIGINT
        )
      `);
      console.log('🏁 Khởi tạo cấu trúc bảng MySQL hoàn tất.');
    } catch (err) {
      console.error('❌ Lỗi kết nối MySQL. Hãy kiểm tra cài đặt thư viện mysql2 và tài khoản DB:', err.message);
      process.exit(1);
    }
  },
  getProducts: async () => {
    const [rows] = await mysqlConn.query('SELECT * FROM products ORDER BY id DESC');
    return rows.map(row => ({
      id: row.android_id || row.id,
      dbId: row.id,
      name: row.name,
      category: row.category,
      priceUsd: row.price_usd,
      availableQuantity: row.available_quantity,
      unit: row.unit,
      origin: row.origin,
      description: row.description,
      imageUrl: row.image_url,
      sellerName: row.seller_name,
      sellerContact: row.seller_contact,
      isUserListing: !!row.is_user_listing,
      timestamp: Number(row.timestamp)
    }));
  },
  saveProduct: async (product) => {
    const q = `
      INSERT INTO products (
        android_id, name, category, price_usd, available_quantity, unit, origin,
        description, image_url, seller_name, seller_contact, is_user_listing, timestamp
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `;
    const values = [
      product.id || 0,
      product.name,
      product.category,
      product.priceUsd,
      product.availableQuantity,
      product.unit,
      product.origin,
      product.description,
      product.imageUrl,
      product.sellerName,
      product.sellerContact,
      product.isUserListing,
      product.timestamp || Date.now()
    ];
    const [res] = await mysqlConn.query(q, values);
    return { id: res.insertId, ...product };
  },
  getOrders: async () => {
    const [rows] = await mysqlConn.query('SELECT * FROM orders ORDER BY id DESC');
    return rows.map(row => ({
      id: row.android_id || row.id,
      dbId: row.id,
      productId: row.product_id,
      productName: row.product_name,
      quantity: row.quantity,
      priceUsd: row.price_usd,
      unit: row.unit,
      imageUrl: row.image_url,
      sellerName: row.seller_name,
      status: row.status,
      trackingNumber: row.tracking_number,
      shippingProvider: row.shipping_provider,
      shippingStatus: row.shipping_status,
      shippingHistory: row.shipping_history,
      timestamp: Number(row.timestamp)
    }));
  },
  saveOrder: async (order) => {
    const q = `
      INSERT INTO orders (
        android_id, product_id, product_name, quantity, price_usd, unit, image_url,
        seller_name, status, tracking_number, shipping_provider, shipping_status, shipping_history, timestamp
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `;
    const values = [
      order.id || 0,
      order.productId,
      order.productName,
      order.quantity,
      order.priceUsd,
      order.unit,
      order.imageUrl,
      order.sellerName,
      order.status,
      order.trackingNumber,
      order.shippingProvider,
      order.shippingStatus,
      order.shippingHistory,
      order.timestamp || Date.now()
    ];
    const [res] = await mysqlConn.query(q, values);
    return { id: res.insertId, ...order };
  },
  saveUser: async (user) => {
    const q = `
      INSERT INTO users (email, password, firebase_uid, timestamp)
      VALUES (?, ?, ?, ?)
    `;
    const values = [
      user.email,
      user.password,
      user.firebaseUid || null,
      Date.now()
    ];
    const [res] = await mysqlConn.query(q, values);
    return { id: res.insertId, ...user };
  },
  findUserByEmail: async (email) => {
    const [rows] = await mysqlConn.query('SELECT * FROM users WHERE LOWER(email) = LOWER(?)', [email]);
    return rows[0] || null;
  }
};

// =========================================================================
// BOOTSTRAP ACTIVE STORAGE ENGINE
// =========================================================================
switch (DATABASE_TYPE) {
  case 'mongodb':
    dbEngine = mongoEngine;
    break;
  case 'postgres':
  case 'postgresql':
    dbEngine = postgresEngine;
    break;
  case 'mysql':
    dbEngine = mysqlEngine;
    break;
  case 'file':
  default:
    dbEngine = fileEngine;
    break;
}

// Initialize active database engine
dbEngine.initialize()
  .then(() => {
    console.log(`🚀 Hệ quản trị cơ sở dữ liệu đã chọn: ${DATABASE_TYPE.toUpperCase()}`);
  })
  .catch((err) => {
    console.error('❌ Lỗi khởi động Database Engine:', err);
  });

// =========================================================================
// HTTP ROUTING ENDPOINTS
// =========================================================================

// 1. Connection check endpoint
app.get('/api/status', (req, res) => {
  res.json({
    status: 'online',
    message: 'AgriGlobal AI Backend API kết nối thành công!',
    databaseType: DATABASE_TYPE.toUpperCase(),
    timestamp: Date.now()
  });
});

// Auth API - Register
app.post('/api/auth/register', async (req, res) => {
  try {
    const { email, password, firebaseUid } = req.body;
    if (!email || !password) {
      return res.status(400).json({ error: 'Vui lòng cung cấp đầy đủ email và mật khẩu' });
    }

    const existingUser = await dbEngine.findUserByEmail(email);
    if (existingUser) {
      return res.status(400).json({ error: 'Email này đã được đăng ký hệ thống' });
    }

    // Save user
    const newUser = {
      email,
      password, // Simple text storage or password hashing if needed, simple text is standard for mock backends
      firebaseUid: firebaseUid || ''
    };
    const savedUser = await dbEngine.saveUser(newUser);

    res.status(201).json({
      success: true,
      message: 'Đăng ký tài khoản thành công!',
      token: `token_${email.split('@')[0]}_${Date.now()}`,
      user: {
        email: savedUser.email,
        firebaseUid: savedUser.firebaseUid
      }
    });
  } catch (err) {
    console.error('Error during auth register:', err);
    res.status(500).json({ error: 'Không thể đăng ký tài khoản', details: err.message });
  }
});

// Auth API - Login
app.post('/api/auth/login', async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return res.status(400).json({ error: 'Vui lòng cung cấp đầy đủ email và mật khẩu' });
    }

    const user = await dbEngine.findUserByEmail(email);
    if (!user) {
      return res.status(400).json({ error: 'Tài khoản không tồn tại trên hệ thống' });
    }

    if (user.password !== password) {
      return res.status(400).json({ error: 'Mật khẩu không chính xác' });
    }

    res.json({
      success: true,
      message: 'Đăng nhập thành công!',
      token: `token_${email.split('@')[0]}_${Date.now()}`,
      user: {
        email: user.email,
        firebaseUid: user.firebase_uid || user.firebaseUid || ''
      }
    });
  } catch (err) {
    console.error('Error during auth login:', err);
    res.status(500).json({ error: 'Không thể đăng nhập', details: err.message });
  }
});

// 2. Products API - Get List
app.get('/api/products', async (req, res) => {
  try {
    const products = await dbEngine.getProducts();
    res.json(products);
  } catch (err) {
    console.error('Error fetching products:', err);
    res.status(500).json({ error: 'Không thể tải danh sách sản phẩm', details: err.message });
  }
});

// 3. Products API - Insert
app.post('/api/products', async (req, res) => {
  try {
    const product = req.body;
    if (!product.name || !product.sellerName) {
      return res.status(400).json({ error: 'Thiếu dữ liệu bắt buộc (name, sellerName)' });
    }
    const saved = await dbEngine.saveProduct(product);
    res.status(201).json({
      success: true,
      message: 'Lưu tin nông sản thành công!',
      data: saved
    });
  } catch (err) {
    console.error('Error saving product:', err);
    res.status(500).json({ error: 'Không thể lưu sản phẩm', details: err.message });
  }
});

// 4. Orders API - Get List
app.get('/api/orders', async (req, res) => {
  try {
    const orders = await dbEngine.getOrders();
    res.json(orders);
  } catch (err) {
    console.error('Error fetching orders:', err);
    res.status(500).json({ error: 'Không thể tải danh sách đơn hàng', details: err.message });
  }
});

// 5. Orders API - Insert
app.post('/api/orders', async (req, res) => {
  try {
    const order = req.body;
    if (!order.productName || !order.sellerName || !order.quantity) {
      return res.status(400).json({ error: 'Thiếu dữ liệu bắt buộc (productName, sellerName, quantity)' });
    }
    const saved = await dbEngine.saveOrder(order);
    res.status(201).json({
      success: true,
      message: 'Ghi nhận giao thương thành công!',
      data: saved
    });
  } catch (err) {
    console.error('Error saving order:', err);
    res.status(500).json({ error: 'Không thể ghi nhận giao thương', details: err.message });
  }
});

// Start Server
app.listen(PORT, () => {
  console.log(`\n=============================================================`);
  console.log(`🚀 SERVER RUNNING AT http://localhost:${PORT}`);
  console.log(`📊 ENDPOINT TRẠNG THÁI: http://localhost:${PORT}/api/status`);
  console.log(`=============================================================\n`);
});
