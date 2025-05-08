package com.seneca.taskmanagement.repository;

import com.seneca.taskmanagement.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    boolean existsByUsername(String username);
    
    /**
     * Check if a username exists among both active and deleted users
     * This query ignores the @Where clause to check all users
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username")
    boolean existsByUsernameIncludingDeleted(@Param("username") String username);
}
