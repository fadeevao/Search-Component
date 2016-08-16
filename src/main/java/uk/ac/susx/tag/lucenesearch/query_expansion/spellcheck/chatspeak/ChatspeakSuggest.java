package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck.chatspeak;


import java.util.List;
import java.util.Map;

public class ChatspeakSuggest {
    private Map<String, List<String>> chatspeakMap;

    public ChatspeakSuggest() {
        chatspeakMap = new ChatspeakMapBuilder().getChatspeakMap();
    }

    public List<String> getChatspeakSuggestions(String query) {
        return chatspeakMap.get(query);
    }
}
