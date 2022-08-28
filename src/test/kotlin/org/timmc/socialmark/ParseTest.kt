package org.timmc.socialmark

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ParseTest {
    /** An empty document has zero nodes -- not a single empty text node. */
    @Test fun empty() {
        assertEquals(0, Parse.parseMarkup("").nodes.size)
    }

    @Test fun justText() {
        mapOf(
            "simple" to Parse.parseMarkup("Hello, world!"),
            "start-escape" to Parse.parseMarkup("\\u000048;ello, world!"),
            "mid-escape" to Parse.parseMarkup("Hello,\\u20;world!"),
            "end-escape" to Parse.parseMarkup("Hello, world\\u0021;"),
        ).map { (which, doc) ->
            assertEquals(
                Document(listOf(TextNode("Hello, world!"))), doc,
                "Assertion failed for '$which'"
            )
        }
    }

    @Test fun invalidTextEscape() {
        // Six hex is fine
        assertEquals(TextNode("trade™mark"), Parse.parseMarkup("trade\\u002122;mark").nodes[0])
        // Seven is not
        assertFails {
            Parse.parseMarkup("trade\\u0002122;mark")
        }
    }

    @Test fun basicNesting() {
        val expected = Document(listOf(
            PairedEl("blockquote", children = listOf(
                TextNode("\n  "),
                PairedEl("cite", children = listOf(
                    PairedEl(
                        "link",
                        mapOf("url" to "https://einstein.example.com"),
                        listOf(
                            TextNode("Not Einstein"),
                        )
                    ),
                )),
                TextNode("\n  Wow, this relativity stuff is hard!\n"),
            )),
            TextNode("\nHere's a photo of a star:\n"),
            SelfClosingEl("embed", mapOf("file" to "attach:star.jpg")),
        ))

        val doc = Parse.parseMarkup("""
            <blockquote>
              <cite><link url="https://einstein.example.com">Not Einstein</link></cite>
              Wow, this relativity stuff is hard!
            </blockquote>
            Here's a photo of a star:
            <embed file="attach:star.jpg"/>
        """.trimIndent())

        assertEquals(expected, doc)
    }

    @Test fun mismatched() {
        assertFails {
            Parse.parseMarkup("<foo>bar</baz>")
        }
    }
}
