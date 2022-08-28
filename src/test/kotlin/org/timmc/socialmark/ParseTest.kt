package org.timmc.socialmark

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ParseTest {
    @Test fun empty() {
        assertEquals(0, Parse.parseMarkup("").nodes.size)
    }

    @Test fun justText() {
        mapOf(
            "simple" to Parse.parseMarkup("Hello, world!"),
            "escape" to Parse.parseMarkup("Hello,\\u20;world!"),
        ).map { (which, doc) ->
            assertEquals(
                Document(listOf(Node.Text("Hello, world!"))), doc,
                "Assertion failed for '$which'"
            )
        }
    }

    @Test fun basicNesting() {
        val expected = Document(listOf(
            Node.Paired("blockquote", emptyList(), listOf(
                Node.Text("\n  "),
                Node.Paired("cite", emptyList(), listOf(
                    Node.Paired(
                        "link",
                        listOf(Attr("url", "https://einstein.example.com")),
                        listOf(
                            Node.Text("Not Einstein"),
                        )
                    ),
                )),
                Node.Text("\n  Wow, this relativity stuff is hard!\n"),
            )),
            Node.Text("\nHere's a photo of a star:\n"),
            Node.SelfClose(
                "embed",
                listOf(Attr("file", "attach:star.jpg"))
            ),
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
