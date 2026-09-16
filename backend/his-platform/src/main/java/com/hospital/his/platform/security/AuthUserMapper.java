package com.hospital.his.platform.security;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AuthUserMapper {
    @Select("""
            SELECT id, username, password_hash, display_name, employee_id, enabled
            FROM sys_user WHERE username = #{username}
            """)
    Optional<AuthUserAccount> findByUsername(String username);

    @Select("""
            SELECT role.code FROM sys_role role
            JOIN sys_user_role user_role ON user_role.role_id = role.id
            WHERE user_role.user_id = #{userId} ORDER BY role.code
            """)
    List<String> findRolesByUserId(Long userId);

    @Select("""
            SELECT DISTINCT permission.code FROM sys_permission permission
            JOIN sys_role_permission role_permission ON role_permission.permission_id = permission.id
            JOIN sys_user_role user_role ON user_role.role_id = role_permission.role_id
            WHERE user_role.user_id = #{userId} ORDER BY permission.code
            """)
    List<String> findPermissionsByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM sys_user")
    long countUsers();

    @Insert("""
            INSERT INTO sys_user (username, password_hash, display_name, enabled)
            VALUES (#{username}, #{passwordHash}, #{displayName}, TRUE)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertUser(NewUser user);

    @Select("SELECT id FROM sys_role WHERE code = 'ROOT'")
    Long findRootRoleId();

    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
