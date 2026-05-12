-- FinFlow - Initialize all service databases
CREATE DATABASE IF NOT EXISTS finflow_auth;
CREATE DATABASE IF NOT EXISTS loan_db;
CREATE DATABASE IF NOT EXISTS document_db;
CREATE DATABASE IF NOT EXISTS admin_db;
CREATE DATABASE IF NOT EXISTS notification_db;
CREATE DATABASE IF NOT EXISTS payment_db;

GRANT ALL PRIVILEGES ON finflow_auth.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON loan_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON document_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON admin_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON notification_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON payment_db.* TO 'root'@'%';
FLUSH PRIVILEGES;
