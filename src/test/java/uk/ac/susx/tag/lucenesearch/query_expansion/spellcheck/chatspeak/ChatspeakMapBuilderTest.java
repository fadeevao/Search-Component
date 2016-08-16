package uk.ac.susx.tag.lucenesearch.query_expansion.spellcheck.chatspeak;


import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class ChatspeakMapBuilderTest {

    @Test
    public void testMapBuilder() {
        Map<String, List<String>> map = new ChatspeakMapBuilder().getChatspeakMap();
        assertTrue(map.size() > 0);
    }
}
