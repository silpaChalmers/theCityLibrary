package com.ibizabroker.lms.entity;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class UsersTest {

    @Test
    void testUsersEntity() {
        // 创建 Role 实例
        Role role = new Role();
        role.setRoleId(1);
        role.setRoleName("ROLE_USER");

        // 创建 Users 实例
        Users user = new Users();
        user.setUserId(1001);
        user.setUsername("testUser");
        user.setName("Test User");
        user.setPassword("password123");
        user.setRole(new HashSet<>(Collections.singleton(role)));

        // 验证属性是否正确赋值
        assertEquals(1001, user.getUserId());
        assertEquals("testUser", user.getUsername());
        assertEquals("Test User", user.getName());
        assertEquals("password123", user.getPassword());
        assertNotNull(user.getRole());
        assertEquals(1, user.getRole().size());
        assertTrue(user.getRole().contains(role));

        // 测试 toString() 生成的字符串是否包含关键字段（如果 Lombok @Data 生成了 toString 方法）
        String userString = user.toString();
        assertTrue(userString.contains("testUser"));
        assertTrue(userString.contains("Test User"));
    }

    @Test
    void testUsersEqualityAndHashCode() {
        Users user1 = new Users();
        user1.setUserId(1);
        user1.setUsername("Alice");

        Users user2 = new Users();
        user2.setUserId(1);
        user2.setUsername("Alice");

        Users user3 = new Users();
        user3.setUserId(2);
        user3.setUsername("Bob");

        // `@Data` 生成了 equals() 和 hashCode()，默认会基于所有字段
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    void testCanEqual() {
        Users user1 = new Users();
        Users user2 = new Users();
        assertTrue(user1.canEqual(user2)); // 两个 Users 实例应返回 true
        assertFalse(user1.canEqual(new Object())); // 与不同类型对象应返回 false
    }

    @Test
    void testEqualsWithDifferentRoles() {
        Users user1 = new Users();
        user1.setUserId(1);
        user1.setUsername("Alice");

        Users user2 = new Users();
        user2.setUserId(1);
        user2.setUsername("Alice");

        // 给 user1 添加角色
        Role role = new Role();
        role.setRoleId(1);
        role.setRoleName("ROLE_USER");
        user1.setRole(new HashSet<>(Collections.singleton(role)));

        assertNotEquals(user1, user2); // user2 没有角色，应不相等

        // 给 user2 添加相同角色
        user2.setRole(new HashSet<>(Collections.singleton(role)));
        assertEquals(user1, user2); // 现在应该相等
    }

    @Test
    void testEqualsAlwaysReturnsTrue() {
        Users user = new Users();
        assertTrue(user.equals(user)); // 自反性测试
    }
    
    @Test
    void testEqualsWithNullAndDifferentTypes() {
        Users user = new Users();
        user.setUserId(1);
        user.setUsername("Alice");

        assertNotEquals(user, null); // 与 null 不相等
        assertNotEquals(user, new Object()); // 与不同类型不相等
    }

    @Test
    void testToString() {
        Users user = new Users();
        user.setUserId(1);
        user.setUsername("Alice");

        String userString = user.toString();
        assertTrue(userString.contains("Alice"));
        assertTrue(userString.contains("1"));
    }

    @Test
    void testUsersWithEmptyRoles() {
        Users user = new Users();
        user.setRole(Collections.emptySet());
        assertNotNull(user.getRole());
        assertEquals(0, user.getRole().size());
    }

    @Test
    void testHashCodeWithDifferentRoles() {
        Users user1 = new Users();
        user1.setUserId(1);
        user1.setUsername("Alice");

        Users user2 = new Users();
        user2.setUserId(1);
        user2.setUsername("Alice");

        assertEquals(user1.hashCode(), user2.hashCode());

        // 添加不同角色后，hashCode 应该不同
        Role role = new Role();
        role.setRoleId(1);
        role.setRoleName("ROLE_USER");
        user1.setRole(new HashSet<>(Collections.singleton(role)));

        assertNotEquals(user1.hashCode(), user2.hashCode());

        // 让 user2 也有相同角色
        user2.setRole(new HashSet<>(Collections.singleton(role)));
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testCanEqualWithDifferentObjects() {
        Users user = new Users();
        Role role = new Role();
        assertFalse(user.canEqual(role)); // Users 不应该和 Role 互相匹配
    }

    @Test
    void testUserIdMutations() {
        Users user = new Users();
        user.setUserId(10);
        assertEquals(10, user.getUserId()); // 确保整数操作正确
    }
}
