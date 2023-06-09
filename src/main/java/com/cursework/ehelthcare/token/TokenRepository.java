package com.cursework.ehelthcare.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * The interface Token repository.
 */
public interface TokenRepository extends JpaRepository<Token, Integer> {

    /**
     * Find all valid token by user list.
     *
     * @param id the id
     * @return the list
     */
    @Query(value = """
      select t from Token t inner join User u\s
      on t.user.id = u.id\s
      where u.id = :id and (t.expired = false or t.revoked = false)\s
      """)
  List<Token> findAllValidTokenByUser(Integer id);

    /**
     * Find by token optional.
     *
     * @param token the token
     * @return the optional
     */
    Optional<Token> findByToken(String token);
}
