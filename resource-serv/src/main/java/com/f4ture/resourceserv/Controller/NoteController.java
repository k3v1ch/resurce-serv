package com.f4ture.resourceserv.Controller;
import com.f4ture.resourceserv.Entity.Note;
import com.f4ture.resourceserv.Repository.NoteRepository;
import com.f4ture.resourceserv.Service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notes")
public class NoteController {
    public final NoteService noteService;

    @GetMapping
    public List<Note> getAllNotes() {
        String email = getEmail();
        return noteService.getAllNotes(email);
    }

    @GetMapping("/{id}")
    public Note getNoteById(@PathVariable Long id) {
        String email = getEmail();
        return noteService.getNoteById(id, email);
    }


    @PostMapping
    public Note createNote(@RequestParam String title, @RequestParam String content) {
        String email = getEmail();
        return noteService.createNote(email,title,content);
    }

    @PutMapping("/{id}")
    public Note updateNote(@PathVariable Long id,
                           @RequestParam String title,
                           @RequestParam String content) {
        String email = getEmail();
        return noteService.updateNote(id,email,title,content);

    }

    @DeleteMapping("/{id}")
    public void deleteNote(@PathVariable Long id) {
        String email = getEmail();
        noteService.deleteNoteById(id, email);
    }
    private String getEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (String) authentication.getPrincipal();
    }

}
