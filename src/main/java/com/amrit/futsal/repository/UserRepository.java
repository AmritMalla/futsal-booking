package com.amrit.futsal.repository;

import com.amrit.futsal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

//    User findOne(Long id);
//
//    @Query("SELECT u from User u  where u.id=:id")
//    User findOne(Long id);
//
//    @Override
//    User getOne(Long id);

    @Override
    Optional<User> findById(Long id);
}
