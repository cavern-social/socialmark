package org.timmc.socialmark

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
                Document(listOf(Node.TextNode("Hello, world!"))), doc,
                "Assertion failed for '$which'"
            )
        }
    }

//        Parse.parseMarkup("<strong>Hello, world!</strong>")
//        Parse.parseMarkup("<strong>Hello</strong>, world!")
}
