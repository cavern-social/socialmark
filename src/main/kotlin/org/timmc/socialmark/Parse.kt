package org.timmc.socialmark

import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.timmc.socialmark.internal.ReproLexer
import org.timmc.socialmark.internal.ReproParser

object Parse {
    /** Returns the hexadecimal in an escape code. */
    fun parseEscape(input: String): String {
        val lexer = ReproLexer(CharStreams.fromString(input))
        val parser = ReproParser(CommonTokenStream(lexer))
        parser.errorHandler = BailErrorStrategy()

        return parser.escape().HEX().joinToString("") { it.text }
    }
}
