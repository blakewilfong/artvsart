package com.artvsart.integration.nga;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NgaCsvReaderTest {

    @Test
    void readsBomCaseInsensitiveHeadersAndQuotedCommas() throws Exception {
        NgaCsvReader reader = new NgaCsvReader(
                new StringReader(
                        "\uFEFFobjectID,title\r\n"
                                + "1,\"A painting, restored\"\r\n"
                )
        );

        NgaCsvReader.Row row = reader.next();

        assertEquals("1", row.get("objectid"));
        assertEquals("A painting, restored", row.get("TITLE"));
        assertNull(reader.next());
    }

    @Test
    void readsEscapedQuotesAndNewlinesInsideValues() throws Exception {
        NgaCsvReader reader = new NgaCsvReader(
                new StringReader(
                        "objectID,title\n"
                                + "1,\"First line\nSecond \"\"line\"\"\"\n"
                )
        );

        assertEquals(
                "First line\nSecond \"line\"",
                reader.next().get("title")
        );
    }
}
