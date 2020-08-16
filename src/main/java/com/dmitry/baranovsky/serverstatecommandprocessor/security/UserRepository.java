package com.dmitry.baranovsky.serverstatecommandprocessor.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface that represents the mySQL server containing UserData.
 */
@Repository
public interface UserRepository extends JpaRepository<UserData, Integer> {
    /**
     * Finds a user by the given username.
     *
     * @param userName the name to search by.
     * @return the UserData with the given username.
     */
    UserData findByUserName(String userName);
}