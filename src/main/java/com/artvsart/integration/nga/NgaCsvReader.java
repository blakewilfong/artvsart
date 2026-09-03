package com.artvsart.integration.nga;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class NgaCsvReader {

    private final PushbackReader reader;
    private final Map<String, Integer> columnIndexes;

    NgaCsvReader(Reader reader) throws IOException {
        this.reader = new PushbackReader(reader, 1);

        List<String> headers = readRecord();

        if (headers == null) {
            throw new IOException("NGA CSV dataset is empty");
        }

        this.columnIndexes = new HashMap<>();

        for (int index = 0; index < headers.size(); index++) {
            columnIndexes.put(
                    normalizeHeader(headers.get(index)),
                    index
            );
        }
    }

    Row next() throws IOException {
        List<String> values;

        do {
            values = readRecord();
        } while (values != null
                && values.stream().allMatch(String::isBlank));

        if (values == null) {
            return null;
        }

        return new Row(columnIndexes, values);
    }

    private List<String> readRecord() throws IOException {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        boolean sawCharacter = false;

        int nextCharacter;

        while ((nextCharacter = reader.read()) != -1) {
            sawCharacter = true;
            char character = (char) nextCharacter;

            if (character == '"') {
                if (!quoted && value.isEmpty()) {
                    quoted = true;
                    continue;
                }

                if (quoted) {
                    int followingCharacter = reader.read();

                    if (followingCharacter == '"') {
                        value.append('"');
                    } else {
                        quoted = false;

                        if (followingCharacter != -1) {
                            reader.unread(followingCharacter);
                        }
                    }

                    continue;
                }
            }

            if (character == ',' && !quoted) {
                values.add(value.toString());
                value.setLength(0);
                continue;
            }

            if ((character == '\r' || character == '\n')
                    && !quoted) {
                if (character == '\r') {
                    int followingCharacter = reader.read();

                    if (followingCharacter != '\n'
                            && followingCharacter != -1) {
                        reader.unread(followingCharacter);
                    }
                }

                values.add(value.toString());
                return values;
            }

            value.append(character);
        }

        if (quoted) {
            throw new IOException("Unterminated quoted NGA CSV value");
        }

        if (!sawCharacter && values.isEmpty() && value.isEmpty()) {
            return null;
        }

        values.add(value.toString());
        return values;
    }

    private String normalizeHeader(String header) {
        return header
                .replace("\uFEFF", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    static final class Row {

        private final Map<String, Integer> columnIndexes;
        private final List<String> values;

        private Row(
                Map<String, Integer> columnIndexes,
                List<String> values
        ) {
            this.columnIndexes = columnIndexes;
            this.values = values;
        }

        String get(String columnName) {
            Integer index = columnIndexes.get(
                    columnName.toLowerCase(Locale.ROOT)
            );

            if (index == null || index >= values.size()) {
                return null;
            }

            return values.get(index);
        }
    }
}
