package be.itassist.doubles;

import be.itassist.domain.commun.Contexte;
import be.itassist.domain.commun.Journal;
import java.util.ArrayList;
import java.util.List;

public final class JournalMuet implements Journal {

    private final List<String> messages = new ArrayList<>();

    public List<String> messages() {
        return messages;
    }

    @Override
    public void info(String message, Contexte... contexte) {
        messages.add(message);
    }

    @Override
    public void avertissement(String message, Contexte... contexte) {
        messages.add(message);
    }

    @Override
    public void erreur(String message, Throwable exception, Contexte... contexte) {
        messages.add(message);
    }
}
