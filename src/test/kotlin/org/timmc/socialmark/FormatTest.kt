package org.timmc.socialmark

import org.junit.Test
import kotlin.test.assertEquals

class FormatTest {
    @Test fun formatDocument() {
        val input = Document(listOf(
            TextNode("Trade<mark>™ "),
            PairedEl("link", sortedMapOf(
                "title" to "some\nwhere‽",
                "url" to "https://example.com",
            ), children = listOf(
                TextNode("click\n"),
                SelfClosingEl("br"),
                TextNode("here"),
            ))
        ))
        val expected = """Trade\u3c;mark>™ <link title="some\ua;where‽" url="https://example.com">click""" +
            "\n<br/>here</link>"
        assertEquals(expected, input.format())
    }
}
