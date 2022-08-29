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
        // Slash must be followed by 'u'
        assertFails { Parse.parseMarkup("trade\\x2122;mark") }

        // Six hex is fine
        assertEquals(TextNode("trade™mark"), Parse.parseMarkup("trade\\u002122;mark").nodes[0])
        // Seven is not
        assertFails { Parse.parseMarkup("trade\\u0002122;mark") }
    }

    @Test fun escapesUpperAndLower() {
        // Uppercase is allowed
        assertEquals(TextNode("<3"), Parse.parseMarkup("\\u3C;3").nodes[0])
        // Lowercase is not
        assertFails { Parse.parseMarkup("\\u3c;3") }
    }

    @Test fun escapesInAttrs() {
        val node = Parse.parseMarkup("<foo bar=\"\\u22;\"/>").nodes[0] as SelfClosingEl
        assertEquals("\"", node.attrs["bar"])

        // Newline not allowed in there
        assertFails {
            Parse.parseMarkup("<foo bar=\"\n\"/>")
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

    @Test fun mismatchedPairs() {
        assertFails {
            Parse.parseMarkup("<foo> <bar> </foo> </bar>")
        }
    }

    @Test fun duplicateAttrs() {
        assertFails {
            Parse.parseMarkup("""<foo bar="1" bar="2">stuff</foo>""")
        }
    }

    @Test fun allowedWhitespace() {
        Parse.parseMarkup("<foo  \r \n  \t bar=\"baz\" \r \n \t />")
        Parse.parseMarkup("<foo  \r \n  \t bar=\"baz\" \r \n \t ></foo \r \t \n >")
    }
}
