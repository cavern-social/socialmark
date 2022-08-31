package org.timmc.socialmark

import org.antlr.v4.runtime.misc.ParseCancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class ParseTest {
    @Test fun normal() {
        assertEquals(Parse.parseEscape("\\u2022;"), "2022")
    }

    @Test fun earlyEOF() {
        val e = assertFails {
            Parse.parseEscape("\\u")
        }
        assertTrue(e is ParseCancellationException)
    }

    @Test fun spuriousCharacter() {
        val e = assertFails {
            Parse.parseEscape("\\u01x34;")
        }
    }
}
