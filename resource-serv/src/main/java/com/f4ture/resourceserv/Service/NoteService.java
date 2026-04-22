package com.f4ture.resourceserv.Service;

import com.f4ture.resourceserv.Entity.Note;
import com.f4ture.resourceserv.Repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;

    public List<Note> getAllNotes(String ownerEmail) {
        return noteRepository.getAllByEmail(ownerEmail);
    }

    public Note getNoteById(Long id, String ownerEmail) {
        return noteRepository.findByIdAndEmail(id,ownerEmail)
                .orElseThrow(() -> new RuntimeException("Заметка не найдена"));
    }

    public Note createNote(String ownerEmail, String title, String content) {
        Note note = Note.builder()
                .ownerEmail(ownerEmail)
                .title(title)
                .content(content)
                .build();
        return noteRepository.save(note);
    }

    public Note updateNote(Long id, String ownerEmail, String title, String content) {
        Note note = getNoteById(id, ownerEmail);
        note.setTitle(title);
        note.setContent(content);
        return noteRepository.save(note);
    }

    public void deleteNoteById(Long id, String ownerEmail) {
        Note note = getNoteById(id, ownerEmail);
        noteRepository.delete(note);
    }
}
