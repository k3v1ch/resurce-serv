package com.f4ture.resourceserv.Repository;

import com.f4ture.resourceserv.Entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    @Query("SELECT n FROM Note n WHERE n.ownerEmail = :email")
    List<Note> getAllByEmail(@Param("email") String email);

    @Query("SELECT n FROM Note n WHERE n.id = :id AND n.ownerEmail = :email")
    Optional<Note> findByIdAndEmail(long id, String email);
}