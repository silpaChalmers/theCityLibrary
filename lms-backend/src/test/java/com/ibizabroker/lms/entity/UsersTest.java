package com.ibizabroker.lms.entity;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

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

        // 验证属性
        assertEquals(1001, user.getUserId());
        assertEquals("testUser", user.getUsername());
        assertEquals("Test User", user.getName());
        assertEquals("password123", user.getPassword());
        assertNotNull(user.getRole());
        assertEquals(1, user.getRole().size());
        assertTrue(user.getRole().contains(role));

        // 测试 toString() 生成的字符串是否包含关键字段
        String userString = user.toString();
        assertTrue(userString.contains("testUser"));
        assertTrue(userString.contains("Test User"));
    }

    @Test
    void testUsersEqualityAndHashCode() {
        Users user1 = new Users();
        user1.setUserId(1);
        user1.setUsername("Alice");
        user1.setName("AliceName");
        user1.setPassword("pwdAlice");

        Users user2 = new Users();
        user2.setUserId(1);
        user2.setUsername("Alice");
        user2.setName("AliceName");
        user2.setPassword("pwdAlice");

        Users user3 = new Users();
        user3.setUserId(2);
        user3.setUsername("Bob");
        user3.setName("BobName");
        user3.setPassword("pwdBob");

        // user1 与 user2 相等
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());

        // user1 与 user3 不相等
        assertNotEquals(user1, user3);
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    void testCanEqual() {
        Users user1 = new Users();
        Users user2 = new Users();
        assertTrue(user1.canEqual(user2), "两个 Users 实例应返回 true");
        assertFalse(user1.canEqual(new Object()), "与不同类型对象应返回 false");
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

        assertNotEquals(user1, user2, "user2 没有角色，应不相等");

        // 给 user2 添加相同角色
        user2.setRole(new HashSet<>(Collections.singleton(role)));
        assertEquals(user1, user2, "现在应该相等");
    }

    @Test
    void testEqualsAlwaysReturnsTrue() {
        Users user = new Users();
        // 与自己比较，必须相等（自反性）
        assertTrue(user.equals(user));
    }

    @Test
    void testEqualsWithNullAndDifferentTypes() {
        Users user = new Users();
        user.setUserId(1);
        user.setUsername("Alice");

        // 与 null 不相等
        assertNotEquals(user, null);
        // 与不同类型不相等
        assertNotEquals(user, new Object());
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

        // hashCode 一开始相等
        assertEquals(user1.hashCode(), user2.hashCode());

        // 添加角色后，hashCode 应该改变
        Role role = new Role();
        role.setRoleId(1);
        role.setRoleName("ROLE_USER");
        user1.setRole(new HashSet<>(Collections.singleton(role)));

        // 现在 user1 与 user2 的 hashCode 不应相等
        assertNotEquals(user1.hashCode(), user2.hashCode());

        // 让 user2 也有相同角色
        user2.setRole(new HashSet<>(Collections.singleton(role)));
        // 又相等
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testCanEqualWithDifferentObjects() {
        Users user = new Users();
        Role role = new Role();
        assertFalse(user.canEqual(role), "Users 不应该和 Role 互相匹配");
    }

    @Test
    void testUserIdMutations() {
        Users user = new Users();
        user.setUserId(10);
        assertEquals(10, user.getUserId());
    }

    // ====================== 新增：专门针对 hashCode 的多字段差异测试 ======================
    @Test
    void testHashCodeDistinctWhenMultipleFieldsDiffer() {
        // 用大量字段差异，力求在“乘法改除法”或“加法改减法”时，出现碰撞从而断言失败
        Users userA = new Users();
        userA.setUserId(1234);
        userA.setUsername("AAAAAA");
        userA.setName("NameA");
        userA.setPassword("PassA");

        Role roleA = new Role();
        roleA.setRoleId(101);
        roleA.setRoleName("ROLE_101");
        userA.setRole(Collections.singleton(roleA));

        Users userB = new Users();
        userB.setUserId(4321);
        userB.setUsername("BBBBBB");
        userB.setName("NameB");
        userB.setPassword("PassB");

        Role roleB = new Role();
        roleB.setRoleId(202);
        roleB.setRoleName("ROLE_202");
        userB.setRole(Collections.singleton(roleB));

        int hashA = userA.hashCode();
        int hashB = userB.hashCode();
        // 如果“乘法改除法”或“加法改减法”使 hashCode 变得错误，极可能让这两个对象的哈希值相等
        // 在这里我们强行断言必须不同，从而在变异后测试失败，杀死变异
        assertNotEquals(hashA, hashB,
            "不同对象 (字段差异较大) 应有不同 hashCode");
    }

    // ====================== 新增：测试 equals 分支 - null 字段 / 不同字段 ======================
    @Test
    void testEqualsWithNullFields() {
        // user1 部分字段为 null
        Users user1 = new Users();
        user1.setUserId(1);
        user1.setUsername(null);  // 故意设为 null
        user1.setName(null);
        user1.setPassword("pwd");

        // user2 对应字段也为 null
        Users user2 = new Users();
        user2.setUserId(1);
        user2.setUsername(null);
        user2.setName(null);
        user2.setPassword("pwd");

        assertEquals(user1, user2, "两个字段都为 null 时仍可相等");
        assertEquals(user1.hashCode(), user2.hashCode());

        // user3 只有一个字段不同（name 为非 null）
        Users user3 = new Users();
        user3.setUserId(1);
        user3.setUsername(null);
        user3.setName("something");
        user3.setPassword("pwd");

        assertNotEquals(user1, user3, "name 不同时不相等");
    }

    // ====================== 新增：测试 equals 分支 - 同一个 userId, 不同 username ======================
    @Test
    void testEqualsSameIdDifferentUsername() {
        Users user1 = new Users();
        user1.setUserId(10);
        user1.setUsername("Alice");

        Users user2 = new Users();
        user2.setUserId(10);
        user2.setUsername("Bob");

        // 即使 userId 相同，但 username 不同，通常 equals() 也应当不同
        // 具体逻辑视 Lombok 生成策略或手动 equals 而定
        assertNotEquals(user1, user2, "同一个 userId, 不同 username 应该不相等");
    }
}
