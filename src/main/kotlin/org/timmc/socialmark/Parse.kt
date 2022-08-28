package org.timmc.socialmark

import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.timmc.socialmark.internal.SMLexer
import org.timmc.socialmark.internal.SMParser

object Parse {
    fun parseMarkup(sml: String) {
        val lexer = SMLexer(CharStreams.fromString(sml))
        val parser = SMParser(CommonTokenStream(lexer))
        parser.errorHandler = BailErrorStrategy()
    }
}
