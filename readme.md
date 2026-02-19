# Modul 4 : Wallet & Transaction (json-payment-service)

Microservice untuk mengelola **Modul Wallet & Transaksi** pada platform JaStip Online Nasional (JSON).

## Tanggung Jawab Modul

Modul ini berfungsi sebagai pusat pengelolaan keuangan digital dalam ekosistem JSON, bertanggung jawab untuk menangani aliran masuk (top-up) dan keluar (withdrawal) dana, serta menjaga integritas saldo pengguna agar tidak terjadi
kesalahan perhitungan sekecil apapun.

---

## Yang Dilakukan Modul Ini

### TODO
- TODO
  ```
  TODO
  ```

### TODO
- TODO


---


## Tech Stack

- **Java** + **Spring Boot** — web framework
- **Hibernate** — ORM
- **PostgreSQL** (Neon DB) — database


---



## Database Schema

### Custom Types

#### `type`
| Value       |
|-------------|
| TOPUP       |
| PAYMENT     |
| REFUND      |
| EARNING     |
| WITHDRAWAL  |
| ADJUSTMENT  |

### `direction`
| Value    |
|----------|
| CREDIT   |
| DEBIT    |

### `status`
| Value        |
|--------------|
| PENDING      |
| SUCCESS      |
| FAILED       |
| CANCELLED    |

### `reference_type`
| Value        |
|--------------|
| ORDER        |
| TOPUP        |
| WITHDRAWAL   |
| CANCELLED    |

---

### Object: Wallet

Entitas ini menyimpan informasi saldo utama, saldo escrow (dana tertahan), serta statistik akumulasi transaksi pengguna.

| Field                     | Type                | Nullable | Key |
|---------------------------|---------------------|----------|-----|
| wallet_id                 | VARCHAR (36)        | NOT NULL | PK  |
| user_id                   | VARCHAR (36)        | NOT NULL | UK  |
| balance                   | LONG                | NOT NULL |     |
| escrow_balance            | LONG                | NOT NULL |     |
| total_topup_lifetime      | LONG                | NOT NULL |     |
| total_withdrawal_lifetime | LONG                | NOT NULL |     |
| created_at                | DATETIME (ISO 8601) | NOT NULL |     |
| updated_at                | DATETIME (ISO 8601) | NOT NULL |     |

**PK:** wallet_id

**UK:** user_id (Satu user hanya boleh memiliki satu wallet).

**Notes:**
- **Lifetime Fields**: `total_topup_lifetime` dan `total_withdrawal_lifetime` digunakan untuk melacak total historis transaksi pengguna tanpa harus menghitung ulang seluruh tabel transaksi.
- **Audit Trail**: `created_at` dan `updated_at` diisi secara otomatis oleh sistem.

---

### Object: Transaction

Entitas ini merepresentasikan data transaksi finansial dalam sistem, mencakup mutasi saldo, referensi pembayaran, dan audit trail.

| Field             | Type                | Nullable | Key |
|-------------------|---------------------|----------|-----|
| transaction_id    | VARCHAR (36)        | NOT NULL | PK  |
| wallet_id         | VARCHAR (36)        | NOT NULL |     |
| user_id           | VARCHAR (36)        | NOT NULL |     |
| type              | *type*              | NOT NULL |     |
| direction         | *direction*         | NOT NULL |     |
| amount            | LONG                | NOT NULL |     |
| status            | *status*            | NOT NULL |     |
| description       | VARCHAR             | NOT NULL |     |
| balance_before    | LONG                | NOT NULL |     |
| balance_after     | LONG                | NULL     |     |
| reference_id      | VARCHAR (36)        | NULL     |     |
| reference_type    | *reference_type*    | NULL     |     |
| payment_method    | VARCHAR             | NULL     |     |
| payment_reference | VARCHAR             | NULL     |     |
| idempotency_key   | VARCHAR             | NULL     | UK  |
| confirmed_by      | VARCHAR             | NULL     |     |
| expired_at        | DATETIME (ISO 8601) | NULL     |     |
| created_at        | DATETIME (ISO 8601) | NOT NULL |     |
| updated_at        | DATETIME (ISO 8601) | NOT NULL |     |

**PK:** transaction_id

**UK:** idempotency_key

**Notes:**
- Field **balance_after** bersifat nullable karena nilainya mungkin baru diisi setelah transaksi berhasil dikonfirmasi/selesai.
- Field unik **idempotency_key** berfungsi untuk mencegah pemrosesan transaksi ganda pada request yang sama.
- Kolom `created_at` dan `updated_at` diatur secara otomatis oleh sistem saat data pertama kali disimpan atau diubah.

---

### Object: BankAccount

Entitas ini menyimpan informasi rekening bank pengguna yang digunakan untuk proses verifikasi identitas dan tujuan penarikan dana (withdrawal).

| Field                  | Type                | Nullable | Key |
|------------------------|---------------------|----------|-----|
| bank_account_id        | VARCHAR (36)        | NOT NULL | PK  |
| user_id                | VARCHAR (36)        | NOT NULL |     |
| bank_code              | VARCHAR             | NOT NULL |     |
| bank_name              | VARCHAR             | NOT NULL |     |
| account_number         | VARCHAR             | NOT NULL |     |
| account_name           | VARCHAR             | NOT NULL |     |
| is_verified            | BOOLEAN             | NOT NULL |     |
| is_primary             | BOOLEAN             | NOT NULL |     |
| verified_by            | VARCHAR (36)        | NULL     |     |
| verified_at            | DATETIME (ISO 8601) | NULL     |     |
| created_at             | DATETIME (ISO 8601) | NOT NULL |     |

**PK:** bank_account_id

**Notes:**
- **is_primary**: Flag untuk menentukan rekening utama jika user memiliki lebih dari satu rekening.
- **is_verified**: Status apakah rekening telah divalidasi oleh sistem atau admin.
- **Audit Trace**: `verified_by` menyimpan user_id admin yang melakukan verifikasi rekening tersebut.
