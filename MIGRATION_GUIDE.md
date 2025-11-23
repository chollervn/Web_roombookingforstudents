Để chạy migration SQL, bạn có 2 cách:

## Cách 1: Spring Boot tự động chạy (KHUYẾN NGHỊ)

Thêm dòng này vào `application.properties`:
```properties
spring.jpa.hibernate.ddl-auto=update
```

Bạn đã có dòng này rồi (dòng 6), nên khi bạn **restart ứng dụng**, Hibernate sẽ TỰ ĐỘNG thêm 2 cột mới (`room_status`, `room_leader_id`) vào bảng `room`.

**Chạy lại ứng dụng:**
```bash
mvn spring-boot:run
```

## Cách 2: Dùng pgAdmin hoặc DBeaver

1. Mở pgAdmin hoặc DBeaver
2. Kết nối đến database `ecommerce_db`
3. Mở Query Tool
4. Copy nội dung từ file `src/main/resources/db/migration/V1__add_room_status.sql`
5. Paste vào Query Tool và Execute

## Sau Khi Migration Xong

Truy cập: `http://localhost:8081/admin/rooms`

Bạn sẽ thấy danh sách phòng hiển thị bình thường!
