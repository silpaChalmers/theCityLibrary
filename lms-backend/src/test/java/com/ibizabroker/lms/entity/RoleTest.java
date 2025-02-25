package com.ibizabroker.lms.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void testRoleEntity() {
        // 创建 Role 实例
        Role role = new Role();
        role.setRoleId(1);
        role.setRoleName("ROLE_USER");

        // 验证属性是否正确赋值
        assertEquals(1, role.getRoleId());
        assertEquals("ROLE_USER", role.getRoleName());
    }

    @Test
    void testRoleEqualityAndHashCode() {
        Role role1 = new Role();
        role1.setRoleId(1);
        role1.setRoleName("ROLE_USER");

        Role role2 = new Role();
        role2.setRoleId(1);
        role2.setRoleName("ROLE_USER");

        Role role3 = new Role();
        role3.setRoleId(2);
        role3.setRoleName("ROLE_ADMIN");

        // `@Data` 生成的 equals() 和 hashCode()，基于所有字段
        assertEquals(role1, role2);
        assertNotEquals(role1, role3);
        assertEquals(role1.hashCode(), role2.hashCode());
        assertNotEquals(role1.hashCode(), role3.hashCode());
    }

    @Test
    void testToString() {
        Role role = new Role();
        role.setRoleId(1);
        role.setRoleName("ROLE_USER");

        String roleString = role.toString();
        assertTrue(roleString.contains("ROLE_USER"));
        assertTrue(roleString.contains("1"));
    }

    @Test
    void testCanEqual() {
        Role role1 = new Role();
        Role role2 = new Role();
        assertTrue(role1.canEqual(role2)); // 两个 Role 实例应返回 true
        assertFalse(role1.canEqual(new Object())); // Role 不应等于其他对象类型
    }

    @Test
    void testEqualsAlwaysReturnsTrue() {
        Role role = new Role();
        assertTrue(role.equals(role)); // 自反性测试
    }

    @Test
    void testEqualsWithNullAndDifferentTypes() {
        Role role = new Role();
        role.setRoleId(1);
        role.setRoleName("ROLE_USER");

        assertNotEquals(role, null); // 与 null 不相等
        assertNotEquals(role, new Object()); // 与不同类型不相等
    }

    @Test
    void testHashCodeConsistency() {
        Role role1 = new Role();
        role1.setRoleId(1);
        role1.setRoleName("ROLE_USER");

        Role role2 = new Role();
        role2.setRoleId(1);
        role2.setRoleName("ROLE_USER");

        assertEquals(role1.hashCode(), role2.hashCode());
    }

    @Test
    void testNegatedConditionals() {
        Role role = new Role();
        role.setRoleId(1);
        role.setRoleName("ROLE_USER");

        assertFalse(role.equals(null)); // 确保 null 检查有效
        assertFalse(role.equals(new Object())); // 确保不同类型检查有效
    }

    @Test
    void testIntegerMutations() {
        Role role = new Role();
        role.setRoleId(10);
        assertEquals(10, role.getRoleId()); // 确保整数运算未被变异影响
    }

    @Test
    void testStringReturnMutations() {
        Role role = new Role();
        role.setRoleName("ROLE_ADMIN");
        assertEquals("ROLE_ADMIN", role.getRoleName()); // 确保字符串返回值未被变异影响
    }
}
