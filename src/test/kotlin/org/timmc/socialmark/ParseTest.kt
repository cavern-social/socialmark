package org.timmc.socialmark

import kotlin.test.Test
import kotlin.test.assertTrue

class ParseTest {
    @Test fun empty() {
        Parse.parseMarkup("")
    }

    @Test fun justText() {
        println(Parse.parseMarkup("Hello, world!"))
    }

//        Parse.parseMarkup("<strong>Hello, world!</strong>")
//        Parse.parseMarkup("<strong>Hello</strong>, world!")
}
